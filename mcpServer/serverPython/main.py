import logging
import os
import sys
from pathlib import Path

from weather import mcp
from weather.config import WeatherConfig


def configure_logging(level: int = logging.INFO) -> None:
    """Set up basic console logging."""
    # Keep logging simple and uniform across runs.
    logging.basicConfig(
        level=level,
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
        stream=sys.stderr,
    )


def main(transport: str | None = None) -> None:
    """Entry point for the MCP weather server."""
    configure_logging()
    app_root = Path(__file__).parent.resolve()
    cfg = WeatherConfig.from_env()
    chosen_transport = transport or os.getenv("MCP_TRANSPORT", "stdio")
    logging.info(
        "Starting MCP weather server (root=%s, transport=%s, api_base=%s, periods=%s)",
        app_root,
        chosen_transport,
        cfg.api_base,
        cfg.forecast_periods,
    )
    mcp.run(transport=chosen_transport)


if __name__ == "__main__":
    main()
