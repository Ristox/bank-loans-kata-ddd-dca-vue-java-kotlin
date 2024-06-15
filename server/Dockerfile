FROM amazoncorretto:21-alpine-jdk AS build

WORKDIR /opt/rsx/bank-loans
COPY . . 
RUN ./gradlew clean build


FROM pnavato/amazoncorretto-jre:21-alpine AS deploy
RUN addgroup -S bank-user && adduser -H -S bank-user
COPY --from=build --chown=bank-user:bank-user /opt/rsx/bank-loans/app/build/libs/app-1.0.0-SNAPSHOT.jar bank-loans-application.jar
USER bank-user
ENTRYPOINT ["java", "-jar", "bank-loans-application.jar"]
