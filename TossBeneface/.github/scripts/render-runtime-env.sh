#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "usage: $0 <secret-json-path> <output-env-path> <profile>" >&2
  exit 1
fi

secret_json_path="$1"
output_env_path="$2"
profile="$3"

required_keys=(
  FAST_API_HOST
  SPRING_DATASOURCE_URL
  SPRING_DATASOURCE_USERNAME
  SPRING_DATASOURCE_PASSWORD
  SPRING_DATA_REDIS_HOST
  SPRING_DATA_REDIS_PORT
  TOKEN_SECRET
  COOKIE_ENCRYPTION_SECRET
  AWS_ACCESS_KEY
  AWS_SECRET_KEY
  TOSS_TEST_CLIENT_API_KEY
  TOSS_TEST_SECRET_API_KEY
  TOSS_SUCCESS_URL
  TOSS_FAIL_URL
)

optional_keys=(
  SPRING_DATA_REDIS_PASSWORD
)

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to render runtime env files" >&2
  exit 1
fi

if [[ ! -f "$secret_json_path" ]]; then
  echo "secret json not found: $secret_json_path" >&2
  exit 1
fi

printf 'SPRING_PROFILES_ACTIVE=%s\n' "$profile" > "$output_env_path"

for key in "${required_keys[@]}"; do
  value="$(jq -er --arg key "$key" '.[$key] | tostring' "$secret_json_path")"
  printf '%s=%s\n' "$key" "$value" >> "$output_env_path"
done

for key in "${optional_keys[@]}"; do
  value="$(jq -r --arg key "$key" 'if has($key) and .[$key] != null then .[$key] | tostring else empty end' "$secret_json_path")"
  if [[ -n "$value" ]]; then
    printf '%s=%s\n' "$key" "$value" >> "$output_env_path"
  fi
done
