"""FastMCP server exposing the weather tools."""

from __future__ import annotations

from typing import Annotated

from mcp.server.fastmcp import FastMCP

from .client import NwsClient
from .config import WeatherConfig
from .exceptions import InvalidLocationError, UpstreamServiceError, WeatherServiceError
from .formatters import format_alerts, format_forecast
from .service import WeatherService

mcp = FastMCP("weather")
config = WeatherConfig.from_env()
service = WeatherService(NwsClient(config), config)


@mcp.tool()
async def get_alerts(state: Annotated[str, "Codice di due lettere dello stato USA (es. CA, NY)."]) -> str:
    """Elenca le allerte meteo attive per uno stato USA."""
    try:
        alerts = await service.alerts_for_state(state)
        return format_alerts(alerts)
    except InvalidLocationError as exc:
        return f"Stato non valido o non riconosciuto: {exc}"
    except UpstreamServiceError as exc:
        return f"Errore nel contattare NWS: {exc}"
    except WeatherServiceError as exc:
        return f"Errore servizio meteo: {exc}"


@mcp.tool()
async def get_forecast(
    latitude: Annotated[float, "Latitudine in decimali (es. 37.7749)."],
    longitude: Annotated[float, "Longitudine in decimali (es. -122.4194)."],
    periods: Annotated[int, "Numero di periodi di forecast da restituire (default da env)."] | None = None,
) -> str:
    """Restituisce le previsioni puntuali per coordinate fornite."""
    try:
        bundle = await service.forecast_for_coordinates(latitude, longitude, periods=periods)
        return format_forecast(bundle)
    except InvalidLocationError as exc:
        return f"Coordinate non valide per il servizio NWS: {exc}"
    except UpstreamServiceError as exc:
        return f"Errore nel contattare NWS: {exc}"
    except WeatherServiceError as exc:
        return f"Errore servizio meteo: {exc}"
