"""Backwards-compatible shim that re-exports the FastMCP instance."""

from .server import get_alerts, get_forecast, mcp

__all__ = ["mcp", "get_alerts", "get_forecast"]
