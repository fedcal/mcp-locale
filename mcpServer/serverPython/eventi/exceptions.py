"""Eccezioni specifiche per il dominio eventi."""


class EventError(Exception):
    """Errore generico eventi MCP."""


class EventNotFound(EventError):
    """Evento non trovato."""


class ParticipantNotFound(EventError):
    """Partecipante non trovato."""
