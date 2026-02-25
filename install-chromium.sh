export CHROMIUM_VERSION="latest"
export CHROMIUM_DEB_SITE="http://deb.debian.org/debian"
echo "deb ${CHROMIUM_DEB_SITE}/ sid main" >/etc/apt/sources.list.d/debian.list \
  && wget -qO- https://ftp-master.debian.org/keys/archive-key-12.asc | gpg --dearmor > /etc/apt/trusted.gpg.d/debian-archive-keyring.gpg \
  && wget -qO- https://ftp-master.debian.org/keys/archive-key-12-security.asc | gpg --dearmor > /etc/apt/trusted.gpg.d/debian-archive-security-keyring.gpg \
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
    fi