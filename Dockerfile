FROM java:8-jdk-alpine

COPY ./build/McNativeMonitoringService.jar /usr/app/

WORKDIR /usr/app
ENTRYPOINT ["java", "-jar", "McNativeMonitoringService.jar"]