"""Domain-specific exceptions for the weather service."""


class WeatherServiceError(Exception):
    """Base error for service-layer issues."""


class UpstreamServiceError(WeatherServiceError):
    """Raised when the external NWS API cannot be reached or returns invalid data."""


class InvalidLocationError(WeatherServiceError):
    """Raised when coordinates or state codes do not map to forecast data."""
