FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew \
	&& ./gradlew --no-daemon bootJar \
	&& find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -exec cp {} app.jar \;

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar
COPY --from=build /app/webapp ./webapp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]