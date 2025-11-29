package com.server.eventi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.eventi.config.EventProperties;
import com.server.eventi.dto.RestaurantSuggestion;
import com.server.eventi.model.EventEntity;
import com.server.eventi.model.ParticipantEntity;
import com.server.eventi.repository.EventRepository;
import com.server.eventi.repository.ParticipantRepository;

@Service
public class EventiService {

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final EventProperties properties;

    public EventiService(EventRepository eventRepository, ParticipantRepository participantRepository, EventProperties properties) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.properties = properties;
    }

    @Transactional
    public EventEntity createEvent(String name, String dateTime, String location, BigDecimal budget, String notes) {
        EventEntity entity = new EventEntity();
        entity.setName(name);
        entity.setDateTime(dateTime);
        entity.setLocation(location);
        entity.setBudget(budget);
        entity.setCurrency(properties.getDefaultCurrency());
        entity.setNotes(notes);
        return eventRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public EventEntity getEvent(UUID eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Evento non trovato: " + eventId));
    }

    @Transactional
    public ParticipantEntity addParticipant(UUID eventId, String name, List<String> intolerances, List<String> preferences, Double weight) {
        EventEntity event = getEvent(eventId);
        ParticipantEntity p = new ParticipantEntity();
        p.setName(name);
        p.setIntolerances(String.join(",", normalize(intolerances)));
        p.setPreferences(String.join(",", normalize(preferences)));
        p.setWeight(weight != null && weight > 0 ? weight : 1.0);
        p.setEvent(event);
        return participantRepository.save(p);
    }

    @Transactional
    public ParticipantEntity updateParticipant(UUID eventId, UUID participantId, List<String> intolerances, List<String> preferences, Double weight) {
        getEvent(eventId); // validazione esistenza evento
        ParticipantEntity p = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("Partecipante non trovato: " + participantId));
        if (intolerances != null) {
            p.setIntolerances(String.join(",", normalize(intolerances)));
        }
        if (preferences != null) {
            p.setPreferences(String.join(",", normalize(preferences)));
        }
        if (weight != null && weight > 0) {
            p.setWeight(weight);
        }
        return participantRepository.save(p);
    }

    @Transactional(readOnly = true)
    public List<ParticipantEntity> listParticipants(UUID eventId) {
        return participantRepository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public List<RestaurantSuggestion> suggestRestaurants(UUID eventId, Integer limit) {
        EventEntity event = getEvent(eventId);
        List<ParticipantEntity> participants = listParticipants(eventId);

        var intolerances = new ArrayList<String>();
        var preferences = new ArrayList<String>();
        for (ParticipantEntity p : participants) {
            intolerances.addAll(splitCsv(p.getIntolerances()));
            preferences.addAll(splitCsv(p.getPreferences()));
        }
        List<RestaurantSuggestion> pool = BaseSuggestions.forLocation(event.getLocation());
        List<RestaurantSuggestion> filtered = new ArrayList<>();
        for (RestaurantSuggestion s : pool) {
            boolean okIntolerances = intolerances.isEmpty() || intolerances.stream().allMatch(i -> s.supports().toLowerCase(Locale.ROOT).contains(i));
            boolean okPrefs = preferences.isEmpty() || preferences.stream().anyMatch(pref -> s.cuisine().toLowerCase(Locale.ROOT).contains(pref));
            if (okIntolerances && okPrefs) {
                filtered.add(s);
            }
        }
        int lim = limit != null ? limit : properties.getSuggestionLimit();
        List<RestaurantSuggestion> source = filtered.isEmpty() ? pool : filtered;
        return source.subList(0, Math.min(lim, source.size()));
    }

    @Transactional(readOnly = true)
    public String splitBill(UUID eventId, double totalAmount, String mode) {
        EventEntity event = getEvent(eventId);
        List<ParticipantEntity> participants = listParticipants(eventId);
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("Nessun partecipante per questo evento.");
        }
        if (totalAmount < 0) {
            throw new IllegalArgumentException("L'importo totale deve essere non negativo.");
        }
        String currency = event.getCurrency() != null ? event.getCurrency() : properties.getDefaultCurrency();
        StringBuilder sb = new StringBuilder();
        switch (mode) {
            case "equal" -> {
                double quota = Math.round((totalAmount / participants.size()) * 100.0) / 100.0;
                for (ParticipantEntity p : participants) {
                    sb.append(p.getName()).append(": ").append(String.format("%.2f", quota)).append(" ").append(currency).append("\n");
                }
            }
            case "weighted" -> {
                double totalWeight = participants.stream().mapToDouble(ParticipantEntity::getWeight).sum();
                if (totalWeight <= 0) {
                    throw new IllegalArgumentException("Somma pesi pari a zero.");
                }
                for (ParticipantEntity p : participants) {
                    double share = Math.round((totalAmount * (p.getWeight() / totalWeight)) * 100.0) / 100.0;
                    sb.append(p.getName()).append(": ").append(String.format("%.2f", share)).append(" ").append(currency).append("\n");
                }
            }
            default -> throw new IllegalArgumentException("Modalita' di split non supportata (equal|weighted).");
        }
        return sb.toString().trim();
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();
    }

    private List<String> normalize(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().map(v -> v.trim().toLowerCase(Locale.ROOT)).filter(v -> !v.isEmpty()).toList();
    }
}

class BaseSuggestions {
    private static final List<RestaurantSuggestion> DEFAULT = List.of(
            new RestaurantSuggestion("Bistro Locale", "fusion", "€€", "vegetariano,senzalattosio", "N/D"),
            new RestaurantSuggestion("Grill House", "carne", "€€", "senzalattosio", "N/D"));

    private static final List<RestaurantSuggestion> MILANO = List.of(
            new RestaurantSuggestion("Trattoria Verde", "italiana", "€€", "gluten-free,vegetariano", "Milano"),
            new RestaurantSuggestion("Sushi Line", "giapponese", "€€€", "gluten-free,pesce", "Milano"),
            new RestaurantSuggestion("Veggie Mood", "vegetariana", "€€", "vegano,gluten-free", "Milano"));

    private static final List<RestaurantSuggestion> ROMA = List.of(
            new RestaurantSuggestion("Osteria Centro", "italiana", "€€", "gluten-free,vegetariano", "Roma"),
            new RestaurantSuggestion("Taverna Bio", "mediterranea", "€€", "vegano,bio", "Roma"));

    static List<RestaurantSuggestion> forLocation(String location) {
        if (location == null) {
            return DEFAULT;
        }
        String key = location.toLowerCase(Locale.ROOT);
        if (key.contains("milano")) {
            return MILANO;
        }
        if (key.contains("roma")) {
            return ROMA;
        }
        return DEFAULT;
    }
}
