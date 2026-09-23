# Always built and layered onto the image when present (regardless of active
# profiles), right before profile-999-end. The FROM line is rewritten to
# chain onto the prior layer automatically -- its value here is unused.
FROM scratch

# lua5.1: matches the LuaJIT/Lua 5.1 semantics used by Envoy's http.lua filter,
# for running dev/envoy/envoy.yaml's inline Lua script standalone during testing.
RUN apt-get update && apt-get install -y --no-install-recommends lua5.1 \
    && rm -rf /var/lib/apt/lists/*
