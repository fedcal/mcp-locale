import logging
import os
import sys

from eventi import mcp


def main(transport: str | None = None) -> None:
    logging.basicConfig(level=logging.INFO, stream=sys.stderr)
    selected = transport or os.getenv("MCP_TRANSPORT", "stdio")
    mcp.run(transport=selected)


if __name__ == "__main__":
    main()
