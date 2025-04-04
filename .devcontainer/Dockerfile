FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Install JMeter and other tools
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
    curl \
    wget \
    unzip \
    fontconfig \
    apt-transport-https \
    ca-certificates \
    gnupg \
    software-properties-common \
    docker.io \
    docker-compose \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Note: Java will be installed by the devcontainer features
# This ensures we use the Java installation from the features

# Install JMeter
ENV JMETER_VERSION=5.6.2
ENV JMETER_HOME=/opt/apache-jmeter-${JMETER_VERSION}
ENV PATH=${JMETER_HOME}/bin:${PATH}

RUN mkdir -p /opt \
    && wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz -O /tmp/apache-jmeter.tgz \
    && tar -xzf /tmp/apache-jmeter.tgz -C /opt \
    && rm /tmp/apache-jmeter.tgz

# Download JMeter plugins manager (installation will happen in post-create.sh)
RUN wget https://jmeter-plugins.org/get/ -O ${JMETER_HOME}/lib/ext/jmeter-plugins-manager.jar \
    && wget https://repo1.maven.org/maven2/kg/apc/cmdrunner/2.2/cmdrunner-2.2.jar -O ${JMETER_HOME}/lib/cmdrunner-2.2.jar

# Cleanup
RUN apt-get autoremove -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*