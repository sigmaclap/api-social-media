FROM amazoncorretto:11-alpine-jdk
COPY target/social-media-api-0.0.1-SNAPSHOT.jar social-media-api.jar
ENTRYPOINT ["java","-jar","/social-media-api.jar"]