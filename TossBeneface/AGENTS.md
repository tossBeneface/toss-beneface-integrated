# Session Notes

## Next Session Backlog

- P0 follow-up: Move remaining deployment-only secrets (`DOCKER_USERNAME`, `DOCKER_PASSWORD`, `EC2_HOST`, `EC2_PRIVATE_KEY`) out of GitHub Secrets into an AWS-managed path or another approved secret store.
- P0 follow-up: Decide whether the `local` profile should also require full env injection instead of keeping developer-friendly defaults.
- P0/P1 follow-up: Externalize the cookie encryption key in `CookieEncryptionUtils` into runtime configuration instead of keeping a source-controlled constant.
