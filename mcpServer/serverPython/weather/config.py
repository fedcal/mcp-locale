"""Configuration helpers for the weather MCP server."""

from dataclasses import dataclass
import os


@dataclass(slots=True)
class WeatherConfig:
    """Runtime configuration derived from environment variables."""

    api_base: str = os.getenv("WEATHER_API_BASE", "https://api.weather.gov")
    user_agent: str = os.getenv("WEATHER_USER_AGENT", "mcp-weather/1.0")
    timeout_seconds: float = float(os.getenv("WEATHER_HTTP_TIMEOUT", "30.0"))
    forecast_periods: int = int(os.getenv("WEATHER_FORECAST_PERIODS", "5"))

    @classmethod
    def from_env(cls) -> "WeatherConfig":
        """Factory method to keep creation uniform."""
        return cls()
