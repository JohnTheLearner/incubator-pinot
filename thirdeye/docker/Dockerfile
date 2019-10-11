# MAINTAINER John Clem <John_Clem@comcast.com>

# Use an official image as parent
# https://hub.docker.com/_/maven
# https://hub.docker.com/_/node

# ThirdEye needs node 6*
FROM node:6.17.0



##### install jdk-8...

RUN apt-get update \
  && apt-get install -y openjdk-8-jdk \
  && apt-get install -y openjfx

# Dynamic didn't work...
# ENV JAVA_HOME=$(dirname $(dirname $(update-alternatives --list javac)))
# RUN export JAVA_HOME=$(dirname $(dirname $(update-alternatives --list javac)))

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

RUN export JAVA_HOME

##### end jdk-8...



##### install maven:3.6.2-jdk-8
#     found at: https://github.com/carlossg/docker-maven/blob/081d9d041aa640694fceabc02c72fe93d5cd42cd/jdk-8/Dockerfile

ARG MAVEN_VERSION=3.6.2
ARG USER_HOME_DIR="/root"
ARG SHA=d941423d115cd021514bfd06c453658b1b3e39e6240969caf4315ab7119a77299713f14b620fb2571a264f8dff2473d8af3cb47b05acf0036fc2553199a5c1ee
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

COPY settings-docker.xml /usr/share/maven/ref/

##### end maven:3.6.2-jdk-8



##### ThirdEye Install
#     from: https://thirdeye.readthedocs.io/en/latest/quick_start.html

WORKDIR /app

# RUN git clone https://github.com/apache/incubator-pinot.git \

# TODO:  For PR dev, use my fork/branch as it's updated to fix the build...
# TODO:  When merging into master, need to update to the main repo and master branch

RUN git clone https://github.com/JohnTheLearner/incubator-pinot.git \
  && git checkout add-Docker-containerization-for-ThirdEye \
  && cd incubator-pinot/thirdeye \
  && chmod +x install.sh run-frontend.sh run-backend.sh reset.sh \
  && cd thirdeye-frontend \
  && npm install

WORKDIR /app/incubator-pinot/thirdeye

RUN ./install.sh  

##### end ThirdEye Install



##### Run ThirdEye

# ports per incubator-pinot/thirdeye/thirdeye-pinot/config/dashboard.yml 
#    1426 = app
#    1427 = admin
EXPOSE 1426
EXPOSE 1427

COPY thirdeye-entrypoint.sh /usr/local/bin/thirdeye-entrypoint.sh

ENTRYPOINT ["/usr/local/bin/thirdeye-entrypoint.sh"]

# CMD ["/bin/bash"]
CMD ["./run-frontend.sh"]

##### end Run ThirdEye
