"""Typed domain objects representing NWS responses."""

from dataclasses import dataclass
from typing import Optional


@dataclass(slots=True)
class Alert:
    event: str
    area: str
    severity: str
    description: str
    instruction: str


@dataclass(slots=True)
class Gridpoint:
    forecast_url: str
    office: Optional[str]
    grid_id: Optional[str]
    grid_x: Optional[int]
    grid_y: Optional[int]


@dataclass(slots=True)
class ForecastPeriod:
    name: str
    temperature: str
    wind: str
    detailed_forecast: str


@dataclass(slots=True)
class ForecastBundle:
    """Aggregated forecast context for formatting."""

    office: Optional[str]
    grid_id: Optional[str]
    grid_x: Optional[int]
    grid_y: Optional[int]
    periods: list[ForecastPeriod]
