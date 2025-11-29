"""Configurazione per il server MCP eventi tra amici."""

from dataclasses import dataclass
import os


@dataclass(slots=True)
class EventConfig:
    default_currency: str = os.getenv("EVENTS_DEFAULT_CURRENCY", "EUR")
    suggestion_limit: int = int(os.getenv("EVENTS_SUGGESTION_LIMIT", "5"))

    @classmethod
    def from_env(cls) -> "EventConfig":
        return cls()
