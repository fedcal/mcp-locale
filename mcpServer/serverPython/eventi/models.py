"""Modelli di dominio per eventi tra amici."""

from dataclasses import dataclass, field
from typing import Optional
from uuid import uuid4


def _new_id() -> str:
    return uuid4().hex


@dataclass(slots=True)
class Participant:
    id: str
    name: str
    intolerances: list[str] = field(default_factory=list)
    preferences: list[str] = field(default_factory=list)
    weight: float = 1.0


@dataclass(slots=True)
class Event:
    id: str
    name: str
    date: str
    location: str
    budget: float | None
    currency: str
    notes: str | None
    participants: list[Participant] = field(default_factory=list)


@dataclass(slots=True)
class RestaurantSuggestion:
    name: str
    cuisine: str
    price_level: str
    supports: list[str]
    location: str


def new_participant(name: str, intolerances: list[str], preferences: list[str], weight: float | None = None) -> Participant:
    return Participant(id=_new_id(), name=name, intolerances=intolerances, preferences=preferences, weight=weight or 1.0)


def new_event(
    name: str,
    date: str,
    location: str,
    currency: str,
    budget: Optional[float] = None,
    notes: Optional[str] = None,
) -> Event:
    return Event(id=_new_id(), name=name, date=date, location=location, budget=budget, currency=currency, notes=notes or "", participants=[])
