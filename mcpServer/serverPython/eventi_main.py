import logging
import os
import sys
from pathlib import Path

# Rende importabile il package `eventi` quando eseguito dalla root.
CURRENT_DIR = Path(__file__).parent
sys.path.insert(0, str(CURRENT_DIR))
from eventi import mcp  # noqa: E402


def configure_logging(level: int = logging.INFO) -> None:
    logging.basicConfig(
        level=level,
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
        stream=sys.stderr,
    )


def main(transport: str | None = None) -> None:
    configure_logging()
    selected = transport or os.getenv("MCP_TRANSPORT", "stdio")
    logging.info("Starting MCP eventi (transport=%s)", selected)
    mcp.run(transport=selected)


if __name__ == "__main__":
    main()
