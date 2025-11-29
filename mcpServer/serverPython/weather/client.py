"""Thin HTTP client for the National Weather Service API."""

from __future__ import annotations

import httpx

from .config import WeatherConfig
from .exceptions import InvalidLocationError, UpstreamServiceError
from .models import Alert, ForecastPeriod, Gridpoint


class NwsClient:
    """Fetch alerts and forecasts from api.weather.gov."""

    def __init__(self, config: WeatherConfig) -> None:
        self.config = config

    async def _get_json(self, path_or_url: str) -> dict:
        """Perform a GET request and return JSON data, raising service-friendly errors."""
        url = path_or_url if path_or_url.startswith("http") else f"{self.config.api_base}{path_or_url}"
        headers = {
            "User-Agent": self.config.user_agent,
            "Accept": "application/geo+json",
        }

        try:
            async with httpx.AsyncClient(follow_redirects=True, timeout=self.config.timeout_seconds) as client:
                response = await client.get(url, headers=headers)
                response.raise_for_status()
                return response.json()
        except httpx.HTTPStatusError as exc:
            if exc.response.status_code == 404:
                raise InvalidLocationError(f"Risorsa non trovata su NWS ({url}).") from exc
            raise UpstreamServiceError(f"Errore HTTP {exc.response.status_code} per {url}.") from exc
        except httpx.RequestError as exc:
            raise UpstreamServiceError(f"Errore di rete verso NWS: {exc}.") from exc
        except ValueError as exc:
            raise UpstreamServiceError(f"Risposta NWS non valida su {url}.") from exc

    async def fetch_alerts(self, state: str) -> list[Alert]:
        """Return active alerts for a two-letter US state code."""
        data = await self._get_json(f"/alerts/active/area/{state}")
        features = data.get("features", [])
        alerts: list[Alert] = []
        for feature in features:
            props = feature.get("properties", {})
            alerts.append(
                Alert(
                    event=props.get("event", "Unknown"),
                    area=props.get("areaDesc", "Unknown"),
                    severity=props.get("severity", "Unknown"),
                    description=props.get("description", "No description available"),
                    instruction=props.get("instruction", "No specific instructions provided"),
                )
            )
        return alerts

    async def resolve_gridpoint(self, latitude: float, longitude: float) -> Gridpoint:
        """Resolve coordinates to a gridpoint to obtain the forecast endpoint."""
        data = await self._get_json(f"/points/{latitude},{longitude}")
        props = data.get("properties", {})
        forecast_url = props.get("forecast")
        if not forecast_url:
            raise InvalidLocationError("Coordinate valide ma nessun endpoint di forecast restituito.")
        return Gridpoint(
            forecast_url=forecast_url,
            office=props.get("gridId"),
            grid_id=props.get("gridId"),
            grid_x=props.get("gridX"),
            grid_y=props.get("gridY"),
        )

    async def fetch_forecast(self, forecast_url: str) -> list[ForecastPeriod]:
        """Download the detailed forecast and normalize to `ForecastPeriod` objects."""
        data = await self._get_json(forecast_url)
        periods = data.get("properties", {}).get("periods", [])
        normalized: list[ForecastPeriod] = []
        for period in periods:
            normalized.append(
                ForecastPeriod(
                    name=period.get("name", "Unknown period"),
                    temperature=f"{period.get('temperature', '?')}°{period.get('temperatureUnit', '')}",
                    wind=f"{period.get('windSpeed', '?')} {period.get('windDirection', '')}".strip(),
                    detailed_forecast=period.get("detailedForecast", "Nessuna descrizione disponibile."),
                )
            )
        return normalized
