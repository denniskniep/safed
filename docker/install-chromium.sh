#!/usr/bin/env bash
set -e

# Install Chromium
export CHROMIUM_VERSION="145.0.7632.159"
export CHROMIUM_DEB_SITE="http://deb.debian.org/debian"

echo "DPKG Architecture:" && dpkg --print-architecture

export URL1="${CHROMIUM_DEB_SITE}/pool/main/c/chromium/chromium-common_${CHROMIUM_VERSION}-1_$(dpkg --print-architecture).deb"
export URL2="${CHROMIUM_DEB_SITE}/pool/main/c/chromium/chromium_${CHROMIUM_VERSION}-1_$(dpkg --print-architecture).deb"
export URL3="${CHROMIUM_DEB_SITE}/pool/main/c/chromium/chromium-l10n_${CHROMIUM_VERSION}-1_all.deb"
export URL4="${CHROMIUM_DEB_SITE}/pool/main/c/chromium/chromium-driver_${CHROMIUM_VERSION}-1_$(dpkg --print-architecture).deb"

echo "Checking URL availability..."
wget --spider "$URL1" || { echo "ERROR: Chromium Version ${CHROMIUM_VERSION} does not exist. Failed to access $URL1"; exit 1; }
wget --spider "$URL2" || { echo "ERROR: Chromium Version ${CHROMIUM_VERSION} does not exist. Failed to access $URL2"; exit 1; }
wget --spider "$URL3" || { echo "ERROR: Chromium Version ${CHROMIUM_VERSION} does not exist. Failed to access $URL3"; exit 1; }
wget --spider "$URL4" || { echo "ERROR: Chromium Version ${CHROMIUM_VERSION} does not exist. Failed to access $URL4"; exit 1; }

echo "All URLs valid, downloading..."
mkdir -p /tmp/chromium
wget "$URL1" -O /tmp/chromium/chromium-common.deb
wget "$URL2" -O /tmp/chromium/chromium.deb
wget "$URL3" -O /tmp/chromium/chromium-l10n.deb
wget "$URL4" -O /tmp/chromium/chromium-driver.deb

echo "deb ${CHROMIUM_DEB_SITE}/ sid main" >/etc/apt/sources.list.d/debian.list
wget -qO- https://ftp-master.debian.org/keys/archive-key-12.asc | gpg --dearmor > /etc/apt/trusted.gpg.d/debian-archive-keyring.gpg
wget -qO- https://ftp-master.debian.org/keys/archive-key-12-security.asc | gpg --dearmor > /etc/apt/trusted.gpg.d/debian-archive-security-keyring.gpg
apt-get update -qqy
apt-get -qqy install libnss3-tools

apt-get -qqyf install /tmp/chromium/chromium-common.deb /tmp/chromium/chromium.deb /tmp/chromium/chromium-l10n.deb /tmp/chromium/chromium-driver.deb

rm -rf /tmp/chromium;
