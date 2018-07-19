ARG IMAGE_BASE=gitlab-registry.cern.ch/industrial-controls/sw-infra/cc7-oraclejava:8.0.2
FROM ${IMAGE_BASE} 

ARG MODESTI_VERSION=0.2.24

ADD http://artifactory.cern.ch/beco-release-local/cern/modesti/modesti-server/$MODESTI_VERSION/modesti-server-$MODESTI_VERSION-dist.tar.gz /opt/modesti/
RUN cd /opt/modesti && tar xvfz /opt/modesti/*.tar.gz && rm /opt/modesti/modesti-server*-dist.tar.gz

#ADD modesti-server-$MODESTI_VERSION-dist.tar.gz /opt/modesti/
RUN cd /opt/modesti/lib && ln -s modesti-server-$MODESTI_VERSION-exec.jar modesti-server-exec.jar

ONBUILD COPY target/dependency/* /opt/modesti-plugins/

COPY opt /opt

CMD ["java","-Dloader.path=/opt/modesti-plugins", "-Dspring.config.location=/opt/modesti/modesti.properties", "-Dlogging.config=/opt/modesti-plugins/logback.xml", "-jar","/opt/modesti/lib/modesti-server-exec.jar"]
