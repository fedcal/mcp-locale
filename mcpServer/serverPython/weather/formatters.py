"""Output formatters used by MCP tools."""

from __future__ import annotations

from .models import Alert, ForecastBundle


def format_alerts(alerts: list[Alert]) -> str:
    """Render alerts in a concise, human-friendly string."""
    if not alerts:
        return "Nessuna allerta attiva per questo stato."

    parts: list[str] = []
    for alert in alerts:
        parts.append(
            "\n".join(
                [
                    f"Evento: {alert.event}",
                    f"Area: {alert.area}",
                    f"Severita': {alert.severity}",
                    f"Descrizione: {alert.description}",
                    f"Istruzioni: {alert.instruction}",
                ]
            )
        )
    return "\n---\n".join(parts)


def format_forecast(bundle: ForecastBundle) -> str:
    """Render a forecast bundle."""
    header_bits: list[str] = []
    if bundle.grid_id:
        header_bits.append(f"Ufficio: {bundle.grid_id}")
    if bundle.grid_x is not None and bundle.grid_y is not None:
        header_bits.append(f"Grid point: {bundle.grid_x},{bundle.grid_y}")
    header = " | ".join(header_bits)
    if not bundle.periods:
        fallback = "Nessun periodo di forecast disponibile."
        return f"{header}\n{fallback}" if header else fallback

    periods_text: list[str] = []
    for period in bundle.periods:
        periods_text.append(
            "\n".join(
                [
                    f"{period.name}",
                    f"Temperatura: {period.temperature}",
                    f"Vento: {period.wind}",
                    f"Previsione: {period.detailed_forecast}",
                ]
            )
        )
    body = "\n---\n".join(periods_text)
    return f"{header}\n{body}" if header else body
