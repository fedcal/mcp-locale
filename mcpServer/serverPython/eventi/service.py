"""Logica di servizio per eventi tra amici."""

from __future__ import annotations

from collections import defaultdict
from typing import Iterable

from .config import EventConfig
from .exceptions import EventNotFound, ParticipantNotFound
from .models import Event, Participant, RestaurantSuggestion, new_event, new_participant


class EventService:
    """Gestisce eventi, partecipanti e suggerimenti ristoranti."""

    def __init__(self, config: EventConfig) -> None:
        self.config = config
        self._events: dict[str, Event] = {}

    def create_event(self, name: str, date: str, location: str, budget: float | None, notes: str | None) -> Event:
        event = new_event(name=name, date=date, location=location, currency=self.config.default_currency, budget=budget, notes=notes)
        self._events[event.id] = event
        return event

    def get_event(self, event_id: str) -> Event:
        if event_id not in self._events:
            raise EventNotFound(f"Evento {event_id} non trovato.")
        return self._events[event_id]

    def add_participant(
        self,
        event_id: str,
        name: str,
        intolerances: Iterable[str] | None = None,
        preferences: Iterable[str] | None = None,
        weight: float | None = None,
    ) -> Participant:
        event = self.get_event(event_id)
        participant = new_participant(
            name=name,
            intolerances=list(intolerances or []),
            preferences=list(preferences or []),
            weight=weight,
        )
        event.participants.append(participant)
        return participant

    def update_preferences(
        self,
        event_id: str,
        participant_id: str,
        intolerances: Iterable[str] | None = None,
        preferences: Iterable[str] | None = None,
        weight: float | None = None,
    ) -> Participant:
        event = self.get_event(event_id)
        participant = next((p for p in event.participants if p.id == participant_id), None)
        if not participant:
            raise ParticipantNotFound(f"Partecipante {participant_id} non trovato per evento {event_id}.")
        if intolerances is not None:
            participant.intolerances = list(intolerances)
        if preferences is not None:
            participant.preferences = list(preferences)
        if weight is not None and weight > 0:
            participant.weight = weight
        return participant

    def suggest_restaurants(self, event_id: str, limit: int | None = None) -> list[RestaurantSuggestion]:
        event = self.get_event(event_id)
        limit = limit or self.config.suggestion_limit
        intolerances = {item.lower() for p in event.participants for item in p.intolerances}
        preferences = {item.lower() for p in event.participants for item in p.preferences}

        suggestions = _BASE_SUGGESTIONS.get(event.location.lower(), _BASE_SUGGESTIONS.get("default", []))
        filtered: list[RestaurantSuggestion] = []
        for suggestion in suggestions:
            # Se il locale supporta tutte le intolleranze richieste, teniamolo.
            if intolerances and not intolerances.issubset({s.lower() for s in suggestion.supports}):
                continue
            # Se ci sono preferenze (es. cucina), prova a matchare almeno una.
            if preferences and not preferences.intersection({suggestion.cuisine.lower()} | {s.lower() for s in suggestion.supports}):
                continue
            filtered.append(suggestion)

        return (filtered or suggestions)[:limit]

    def split_bill(self, event_id: str, total_amount: float, mode: str = "equal") -> dict[str, float]:
        event = self.get_event(event_id)
        if not event.participants:
            raise ParticipantNotFound("Nessun partecipante registrato per questo evento.")
        if total_amount < 0:
            raise ValueError("L'importo totale deve essere non negativo.")

        if mode == "equal":
            quota = round(total_amount / len(event.participants), 2)
            return {p.name: quota for p in event.participants}
        if mode == "weighted":
            total_weight = sum(p.weight for p in event.participants if p.weight > 0)
            if total_weight == 0:
                raise ValueError("Somma pesi pari a zero.")
            shares: dict[str, float] = {}
            for p in event.participants:
                shares[p.name] = round(total_amount * (p.weight / total_weight), 2)
            return shares
        raise ValueError("Modalita' di split non supportata (usa 'equal' o 'weighted').")

    def event_summary(self, event_id: str) -> str:
        event = self.get_event(event_id)
        participants = "\n".join(
            f"- {p.name} (intolleranze: {', '.join(p.intolerances) or 'nessuna'}, preferenze: {', '.join(p.preferences) or 'nessuna'}, peso: {p.weight})"
            for p in event.participants
        ) or "Nessun partecipante."
        budget_text = f"{event.budget} {event.currency}" if event.budget is not None else "n.d."
        return (
            f"Evento: {event.name}\n"
            f"Data: {event.date}\n"
            f"Luogo: {event.location}\n"
            f"Budget: {budget_text}\n"
            f"Note: {event.notes or '-'}\n"
            f"Partecipanti:\n{participants}"
        )


_BASE_SUGGESTIONS: dict[str, list[RestaurantSuggestion]] = {
    "milano": [
        RestaurantSuggestion("Trattoria Verde", "italiana", "€€", ["gluten-free", "vegetariano"], "Milano"),
        RestaurantSuggestion("Sushi Line", "giapponese", "€€€", ["gluten-free", "pesce"], "Milano"),
        RestaurantSuggestion("Veggie Mood", "vegetariana", "€€", ["vegano", "gluten-free"], "Milano"),
    ],
    "roma": [
        RestaurantSuggestion("Osteria Centro", "italiana", "€€", ["gluten-free", "vegetariano"], "Roma"),
        RestaurantSuggestion("Taverna Bio", "mediterranea", "€€", ["vegano", "bio"], "Roma"),
    ],
    "default": [
        RestaurantSuggestion("Bistro Locale", "fusion", "€€", ["vegetariano"], "N/D"),
        RestaurantSuggestion("Grill House", "carne", "€€", ["senza-lattosio"], "N/D"),
    ],
}
