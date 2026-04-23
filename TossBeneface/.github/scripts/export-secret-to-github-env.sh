#!/usr/bin/env bash

set -euo pipefail

if [[ $# -lt 3 ]]; then
  echo "usage: $0 <secret-json-path> <github-env-path> <key> [<key>...]" >&2
  exit 1
fi

secret_json_path="$1"
github_env_path="$2"
shift 2

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to export secret values" >&2
  exit 1
fi

if [[ ! -f "$secret_json_path" ]]; then
  echo "secret json not found: $secret_json_path" >&2
  exit 1
fi

for key in "$@"; do
  value="$(jq -er --arg key "$key" '.[$key] | tostring' "$secret_json_path")"
  printf '::add-mask::%s\n' "$value"
  delimiter="CODEX_SECRET_${key}_$(date +%s%N)"
  {
    printf '%s<<%s\n' "$key" "$delimiter"
    printf '%s\n' "$value"
    printf '%s\n' "$delimiter"
  } >> "$github_env_path"
done
