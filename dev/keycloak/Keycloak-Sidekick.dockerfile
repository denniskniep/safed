FROM ubuntu:24.04

RUN apt-get -qq update && \
    apt-get -qq install -y curl jq && \
    rm -rf /var/lib/apt/lists/*