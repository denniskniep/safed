FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
# Source: https://github.com/SeleniumHQ/docker-selenium/tree/trunk/NodeChromium

USER root

# Install Chromium
ARG CHROMIUM_VERSION="latest"
ARG CHROMIUM_DEB_SITE="http://deb.debian.org/debian"
RUN echo "deb ${CHROMIUM_DEB_SITE}/ sid main" >/etc/apt/sources.list.d/debian.list \
  && wget -qO- https://ftp-master.debian.org/keys/archive-key-12.asc | gpg --dearmor > /etc/apt/trusted.gpg.d/debian-archive-keyring.gpg \
  && wget -qO- https://ftp-master.debian.org/keys/archive-key-12-security.asc | gpg --dearmor > /etc/apt/trusted.gpg.d/debian-archive-security-keyring.gpg \
  && for d in bin lib lib32 lib64 libo32 libx32 sbin; do dpkg-divert --package base-files --no-rename --remove /$d; done \
  && apt-get update -qqy \
  && apt-get -qqy install libnss3-tools \
  && if [ "${CHROMIUM_VERSION}" = "latest" ]; \
      then apt-get -qqy --no-install-recommends install chromium-common chromium chromium-l10n chromium-driver; \
     else mkdir -p /tmp/chromium \
      && wget -q ${CHROMIUM_DEB_SITE}/pool/main/c/chromium/chromium-common_${CHROMIUM_VERSION}-1_$(dpkg --print-architecture).deb -O /tmp/chromium/chromium-common.deb \
      && wget -q ${CHROMIUM_DEB_SITE}/pool/main/c/chromium/chromium_${CHROMIUM_VERSION}-1_$(dpkg --print-architecture).deb -O /tmp/chromium/chromium.deb \
      && wget -q ${CHROMIUM_DEB_SITE}/pool/main/c/chromium/chromium-l10n_${CHROMIUM_VERSION}-1_all.deb -O /tmp/chromium/chromium-l10n.deb \
      && wget -q ${CHROMIUM_DEB_SITE}/pool/main/c/chromium/chromium-driver_${CHROMIUM_VERSION}-1_$(dpkg --print-architecture).deb -O /tmp/chromium/chromium-driver.deb \
      && apt-get -qqyf install /tmp/chromium/chromium-common.deb /tmp/chromium/chromium.deb /tmp/chromium/chromium-l10n.deb /tmp/chromium/chromium-driver.deb \
      && rm -rf /tmp/chromium; \
    fi \
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/* /etc/apt/sources.list.d/debian.list

#=================================
# Chromium Launch Script Wrapper
#=================================
COPY ./docker/wrap_chromium_binary /opt/bin/wrap_chromium_binary

RUN /opt/bin/wrap_chromium_binary && chromium --version

#============================================
# Chromium cleanup script and supervisord file
#============================================
COPY ./docker/chrome-cleanup.sh /opt/bin/chrome-cleanup.sh
COPY ./docker/chrome-cleanup.conf /etc/supervisor/conf.d/chrome-cleanup.conf

USER ${SEL_UID}

#============================================
# Dumping Browser information for config
#============================================
RUN mkdir -p /opt/selenium/browsers/chrome \
    && echo "chrome" > /opt/selenium/browsers/chrome/name \
    && chromium --version | awk '{print $2}' > /opt/selenium/browsers/chrome/version \
    && echo '{"goog:chromeOptions": {"binary": "${SE_BROWSER_BINARY_LOCATION:-/usr/bin/chromium}"}}' > /opt/selenium/browsers/chrome/binary_location

ENV SE_OTEL_SERVICE_NAME="selenium-node-chrome" \
    SE_NODE_ENABLE_MANAGED_DOWNLOADS="true"

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Create a non-root user to run the application
RUN useradd -m -u 1000 appuser && \
    chown -R appuser:appuser /home/appuser && \
    chown -R appuser:appuser /app

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]