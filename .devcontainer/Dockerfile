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
    vim \
    git \
    bash-completion \
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

# Install Java linting tools
ENV SPOTBUGS_VERSION=4.7.3
ENV PMD_VERSION=6.55.0
ENV CHECKSTYLE_VERSION=10.12.3

# Install SpotBugs
RUN mkdir -p /opt/spotbugs \
    && wget https://github.com/spotbugs/spotbugs/releases/download/${SPOTBUGS_VERSION}/spotbugs-${SPOTBUGS_VERSION}.tgz -O /tmp/spotbugs.tgz \
    && tar -xzf /tmp/spotbugs.tgz -C /opt/spotbugs --strip-components=1 \
    && rm /tmp/spotbugs.tgz \
    && ln -s /opt/spotbugs/bin/spotbugs /usr/local/bin/spotbugs

# Install PMD
RUN mkdir -p /opt/pmd \
    && wget https://github.com/pmd/pmd/releases/download/pmd_releases%2F${PMD_VERSION}/pmd-bin-${PMD_VERSION}.zip -O /tmp/pmd.zip \
    && unzip -q /tmp/pmd.zip -d /opt \
    && mv /opt/pmd-bin-${PMD_VERSION}/* /opt/pmd/ \
    && rm -rf /opt/pmd-bin-${PMD_VERSION} \
    && rm /tmp/pmd.zip \
    && ln -s /opt/pmd/bin/run.sh /usr/local/bin/pmd

# Install Checkstyle
RUN mkdir -p /opt/checkstyle \
    && wget https://github.com/checkstyle/checkstyle/releases/download/checkstyle-${CHECKSTYLE_VERSION}/checkstyle-${CHECKSTYLE_VERSION}-all.jar -O /opt/checkstyle/checkstyle.jar \
    && echo '#!/bin/bash\njava -jar /opt/checkstyle/checkstyle.jar "$@"' > /usr/local/bin/checkstyle \
    && chmod +x /usr/local/bin/checkstyle

# Setup SpotBugs, PMD, and Checkstyle rules
RUN mkdir -p /opt/lint-config \
    && echo '<?xml version="1.0"?>\n<ruleset name="Custom PMD Rules" xmlns="http://pmd.sourceforge.net/ruleset/2.0.0">\n    <description>Default PMD rules</description>\n    <rule ref="category/java/bestpractices.xml"/>\n    <rule ref="category/java/errorprone.xml"/>\n</ruleset>' > /opt/lint-config/pmd-ruleset.xml \
    && echo '<?xml version="1.0"?>\n<!DOCTYPE module PUBLIC "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN" "https://checkstyle.org/dtds/configuration_1_3.dtd">\n<module name="Checker">\n    <module name="TreeWalker">\n        <module name="AvoidStarImport"/>\n        <module name="UnusedImports"/>\n        <module name="MethodLength"/>\n    </module>\n</module>' > /opt/lint-config/checkstyle.xml

# Cleanup
RUN apt-get autoremove -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*