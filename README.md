# Demo Project for Online Stream #16 - Microservice for Excel File Processing

Demo project for online stream #16 where a small microservice is created that processes Excel file
uploaded through REST API, saves file to AWS S3, parses it to JSON and send that to another service.

![Diagram for Application](https://github.com/yuriytkach/stream-16-demo-app/blob/main/arch.png?raw=true)

## Access to Online Stream on YouTube

To get a link to online stream on YouTube please do the following:

- :moneybag: Make any donation to support my volunteering initiative to help Ukrainian Armed Forces by means described on [my website](https://www.yuriytkach.com/volunteer)
- :email: Write me an [email](mailto:me@yuriytkach.com) indicating donation amount and time
- :tv: I will reply with the link to the stream on YouTube.

Thank you in advance for your support! Слава Україні! :ukraine:

## Technologies
- Spring Boot
- Apache POI
- AWS Java SDK
- TestContainers for testing

## Building and Running the Application

To build the app, use gradle:
```shell
./gradlew build
```

To run application, use gradle:
```shell
./gradlew bootRun
```

After that you can access application on [http://localhost:8080](http://localhost:8080)

### Docker

To build docker image, use gradle:
```shell
./gradlew bootBuildImage
```

Then you run the docker container with exposing app port `8899`:
```shell
docker run -p 8899:8080 -t stream-16-demo-app:0.0.1-SNAPSHOT
```

After that you can access application on [http://localhost:8899](http://localhost:8899)

## Documentation
### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.4/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.4/gradle-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.4/reference/htmlsingle/#web)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.7.4/reference/htmlsingle/#actuator)
* [Testcontainers](https://www.testcontainers.org/)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
