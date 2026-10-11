#!/usr/bin/env python3
"""Fail-closed Zero-Gap audit orchestrator control-plane validator."""
from __future__ import annotations
import json, os, pathlib

ROOT = pathlib.Path(__file__).resolve().parents[2]
CFG = pathlib.Path(__file__).with_name("agents.yaml")
STAGES = {f"G{i}" for i in range(14)}
SCHEMA = pathlib.Path(__file__).with_name("finding-schema.json")

def main() -> int:
    if not CFG.exists() or not SCHEMA.exists():
        print("BLOCKED: orchestrator configuration is incomplete.")
        return 3
    json.loads(SCHEMA.read_text())
    stage = os.environ.get("ZERO_GAP_START_STAGE", "G0").upper()
    if stage not in STAGES:
        print(f"BLOCKED: invalid ZERO_GAP_START_STAGE={stage}.")
        return 3
    dry = os.environ.get("ZERO_GAP_DRY_RUN", "true").lower() == "true"
    if dry:
        print(f"READY: dry-run validation passed for start stage {stage}; no model calls and no repository mutations.")
        return 0
    if not os.environ.get("OPENAI_API_KEY"):
        print("BLOCKED: OPENAI_API_KEY is not configured. No agents were executed.")
        return 2
    print(f"READY: model execution prerequisites are present for start stage {stage}.")
    print("Execution adapter is intentionally fail-closed until a provider adapter is enabled.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
