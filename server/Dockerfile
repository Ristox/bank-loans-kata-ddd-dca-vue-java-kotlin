FROM amazoncorretto:21-alpine-jdk AS build

WORKDIR /opt/rsx/bank-loans
COPY . . 
RUN ./gradlew clean build


FROM pnavato/amazoncorretto-jre:21-alpine AS deploy
ARG user=bank-user
ARG version=1.0.0-SNAPSHOT
RUN adduser -H -S -u 999 $user
COPY --from=build --chown=$user:$user /opt/rsx/bank-loans/app/build/libs/app-$version.jar bank-loans-application.jar
USER $user
ENTRYPOINT ["java", "-jar", "bank-loans-application.jar"]

