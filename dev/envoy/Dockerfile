FROM envoyproxy/envoy:v1.36.4

RUN apt-get update && \
    apt-get install gettext -y

COPY envoy.yaml /envoy.yaml.tmpl
COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN chmod 500 /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]



