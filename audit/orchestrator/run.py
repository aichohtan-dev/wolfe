#!/usr/bin/env python3
"""Fail-closed Zero-Gap audit orchestrator.

This runner validates configuration and execution prerequisites. The actual
LLM adapter is deliberately isolated so credentials/providers never enter
repository source. It is safe to run in CI with no secret: it reports the
missing prerequisite and exits non-zero.
"""
from __future__ import annotations
import json, os, pathlib, sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
CFG = pathlib.Path(__file__).with_name("agents.yaml")
SCHEMA = pathlib.Path(__file__).with_name("finding-schema.json")

def main() -> int:
    if not os.environ.get("OPENAI_API_KEY"):
        print("BLOCKED: OPENAI_API_KEY is not configured. No agents were executed.")
        return 2
    if not CFG.exists() or not SCHEMA.exists():
        print("BLOCKED: orchestrator configuration is incomplete.")
        return 3
    json.loads(SCHEMA.read_text())
    print("READY: Zero-Gap orchestrator prerequisites are present.")
    print("Next adapter stage must execute agents through the configured model provider.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
