FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN mkdir -p /app/newrelic && mkdir -p /app/logs

ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-agent.jar /app/newrelic/newrelic.jar

COPY newrelic.yml /app/newrelic/newrelic.yml
COPY --from=build /app/target/*.jar /app/app.jar

ENV NEW_RELIC_APP_NAME="oncall-demo"
ENV NEW_RELIC_LOG_FILE_NAME=STDOUT
ENV JAVA_OPTS="-javaagent:/app/newrelic/newrelic.jar"

EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
