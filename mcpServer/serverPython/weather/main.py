import os

from weather import mcp
from weather.config import WeatherConfig


def main(transport: str | None = None) -> None:
    """Run the weather MCP server over the selected transport (default: stdio)."""
    WeatherConfig.from_env()
    selected = transport or os.getenv("MCP_TRANSPORT", "stdio")
    mcp.run(transport=selected)


if __name__ == "__main__":
    main()
