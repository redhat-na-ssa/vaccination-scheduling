FROM quay.io/btison/ubi8-java11:latest

EXPOSE 8080 8443

COPY target/lib/* /deployments/lib/
COPY target/*-runner.jar /deployments/app.jar
