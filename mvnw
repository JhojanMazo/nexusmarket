#!/bin/sh
# ----------------------------------------------------------------------------
# Root-level Maven Wrapper Delegator for NexusMarket
# Automatically targets nexusmarket/pom.xml unless -f/--file is specified
# ----------------------------------------------------------------------------

set -e

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

has_file_arg=false
for arg in "$@"; do
  case "$arg" in
    -f|--file|-f*|-version|--version|-v|-h|--help)
      has_file_arg=true
      break
      ;;
  esac
done

if [ "$has_file_arg" = "true" ]; then
  exec "$ROOT_DIR/nexusmarket/mvnw" "$@"
else
  exec "$ROOT_DIR/nexusmarket/mvnw" -f "$ROOT_DIR/nexusmarket/pom.xml" "$@"
fi
