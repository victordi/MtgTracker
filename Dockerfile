FROM openjdk:17-alpine
COPY ./build/libs/MtgTracker-0.0.1-all.jar /
WORKDIR /
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "MtgTracker-0.0.1-all.jar", "--server.port=8085"]