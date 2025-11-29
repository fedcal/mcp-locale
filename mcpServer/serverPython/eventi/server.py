"""Server MCP per la gestione di eventi tra amici (ristoranti, split spese)."""

from __future__ import annotations

from typing import Annotated

from mcp.server.fastmcp import FastMCP

from .config import EventConfig
from .exceptions import EventError
from .service import EventService

config = EventConfig.from_env()
service = EventService(config)
mcp = FastMCP("eventi-amici")


@mcp.tool()
async def create_event(
    name: Annotated[str, "Titolo dell'evento (es. Cena di squadra)"],
    date: Annotated[str, "Data/ora testuale (es. 2025-03-10 20:00)"],
    location: Annotated[str, "Citta' o indirizzo di riferimento"],
    budget: Annotated[float | None, "Budget totale previsto (opzionale)"] = None,
    notes: Annotated[str | None, "Note aggiuntive"] = None,
) -> str:
    """Crea un nuovo evento."""
    event = service.create_event(name=name, date=date, location=location, budget=budget, notes=notes)
    return f"Evento creato: {event.name} (id={event.id}) a {event.location} il {event.date}. Budget: {event.budget or 'n.d.'} {event.currency}"


@mcp.tool()
async def add_participant(
    event_id: Annotated[str, "ID evento"],
    name: Annotated[str, "Nome partecipante"],
    intolerances: Annotated[list[str] | None, "Intolleranze/allergie (es. glutine, lattosio)"] = None,
    preferences: Annotated[list[str] | None, "Preferenze cucina (es. vegetariano, giapponese)"] = None,
    weight: Annotated[float | None, "Peso per split spese (default 1)"] = None,
) -> str:
    """Aggiunge un partecipante con preferenze."""
    try:
        participant = service.add_participant(event_id, name, intolerances, preferences, weight)
        return f"Aggiunto {participant.name} (id={participant.id}) a evento {event_id}."
    except EventError as exc:
        return f"Errore: {exc}"


@mcp.tool()
async def update_preferences(
    event_id: Annotated[str, "ID evento"],
    participant_id: Annotated[str, "ID partecipante"],
    intolerances: Annotated[list[str] | None, "Intolleranze/allergie"] = None,
    preferences: Annotated[list[str] | None, "Preferenze cucina"] = None,
    weight: Annotated[float | None, "Peso per split spese"] = None,
) -> str:
    """Aggiorna preferenze/intolleranze di un partecipante."""
    try:
        participant = service.update_preferences(event_id, participant_id, intolerances, preferences, weight)
        return f"Aggiornato {participant.name} per evento {event_id}."
    except EventError as exc:
        return f"Errore: {exc}"


@mcp.tool()
async def event_summary(event_id: Annotated[str, "ID evento"]) -> str:
    """Riepilogo completo dell'evento e partecipanti."""
    try:
        return service.event_summary(event_id)
    except EventError as exc:
        return f"Errore: {exc}"


@mcp.tool()
async def suggest_restaurants(
    event_id: Annotated[str, "ID evento"],
    limit: Annotated[int | None, "Numero massimo di suggerimenti (default da config)"] = None,
) -> str:
    """Suggerisce ristoranti compatibili con le preferenze/intolleranze."""
    try:
        suggestions = service.suggest_restaurants(event_id, limit)
        if not suggestions:
            return "Nessun suggerimento disponibile per questo evento."
        lines = []
        for s in suggestions:
            lines.append(f"{s.name} ({s.cuisine}, {s.price_level}) - supporta: {', '.join(s.supports) or 'n.d.'} - luogo: {s.location}")
        return "\n".join(lines)
    except EventError as exc:
        return f"Errore: {exc}"


@mcp.tool()
async def split_bill(
    event_id: Annotated[str, "ID evento"],
    total_amount: Annotated[float, "Importo totale da dividere"],
    mode: Annotated[str, "Modalita' di split: equal | weighted"] = "equal",
) -> str:
    """Divide il conto tra i partecipanti (equal o weighted)."""
    try:
        shares = service.split_bill(event_id, total_amount, mode)
        lines = [f"{name}: {amount:.2f} {service.get_event(event_id).currency}" for name, amount in shares.items()]
        return "\n".join(lines)
    except Exception as exc:  # noqa: BLE001
        return f"Errore: {exc}"
