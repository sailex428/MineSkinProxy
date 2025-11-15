FROM amazoncorretto:21
LABEL authors="sailex"

WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies
COPY src/ src/

RUN ./gradlew build

RUN cp build/libs/*-all.jar app.jar
ENV API_PORT=22222

CMD ["java", "-jar", "app.jar"]