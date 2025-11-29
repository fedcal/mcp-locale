"""Service layer coordinating the MCP-facing tools."""

from __future__ import annotations

from .client import NwsClient
from .config import WeatherConfig
from .exceptions import WeatherServiceError
from .models import Alert, ForecastBundle, ForecastPeriod


class WeatherService:
    """High-level operations used by MCP tools."""

    def __init__(self, client: NwsClient, config: WeatherConfig) -> None:
        self.client = client
        self.config = config

    async def alerts_for_state(self, state: str) -> list[Alert]:
        """Return alerts for a state, bubbling up service errors."""
        normalized = state.strip().upper()
        if len(normalized) != 2:
            raise WeatherServiceError("Il codice dello stato deve contenere due lettere.")
        return await self.client.fetch_alerts(normalized)

    async def forecast_for_coordinates(self, latitude: float, longitude: float, periods: int | None = None) -> ForecastBundle:
        """Return a forecast bundle limited to the requested number of periods."""
        limit = periods or self.config.forecast_periods
        gridpoint = await self.client.resolve_gridpoint(latitude, longitude)
        forecast_periods = await self.client.fetch_forecast(gridpoint.forecast_url)
        return ForecastBundle(
            office=gridpoint.office,
            grid_id=gridpoint.grid_id,
            grid_x=gridpoint.grid_x,
            grid_y=gridpoint.grid_y,
            periods=self._trim_periods(forecast_periods, limit),
        )

    @staticmethod
    def _trim_periods(periods: list[ForecastPeriod], limit: int) -> list[ForecastPeriod]:
        """Protect against overly long responses."""
        if limit <= 0:
            raise WeatherServiceError("Il numero di periodi richiesti deve essere maggiore di zero.")
        return periods[:limit]
