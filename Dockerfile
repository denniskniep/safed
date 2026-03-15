FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM debian:sid-slim
# Source: https://github.com/SeleniumHQ/docker-selenium/tree/trunk/NodeChromium

USER root

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update -qqy && apt-get -qqy install wget curl default-jdk

COPY docker/install-chromium.sh ./install-chromium.sh
RUN ./install-chromium.sh

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