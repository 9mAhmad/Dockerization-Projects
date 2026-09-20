FROM jelastic/maven:3.9.5-openjdk-21 AS builder


WORKDIR /app

COPY . .


# RUN mvn clean package -DskipTests # compiles the tests but does not run tests

RUN mvn clean package -Dmaven.test.skip=true # does not compile nor run the tests.

RUN bash -c "ls -1R ./target/"

RUN ls -l

RUN pwd

# RUN mvn spring-boot:run # usually for local 


# ----------------------

# FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu AS prod

FROM alpine/java:21-jre  AS prod

RUN adduser --disabled-password --gecos "" jre_user

WORKDIR /app

COPY --from=builder --chown=jre_user:jre_user /app/target/JtSpringProject-0.0.1-SNAPSHOT.war ./

# RUN java -jar JtSpringProject-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar"]

CMD ["JtSpringProject-0.0.1-SNAPSHOT.war"]


# docker build -t ecommerce_java_mvn:v1.0notest -f ./Dockerfile ./
# docker build -t ecommerce_java_mvn:v1.1notest -f ./Dockerfile ./
# docker build -t ecommerce_java_mvn:v1.1notestwar -f ./Dockerfile ./
# docker run -dit -p 8080:8080 --name ecommerce_java ecommerce_java_mvn:v1.0notest
# docker run -dit -p 8080:8080 --name ecommerce_java_war ecommerce_java_mvn:v1.1notestwar
# docker run -dit -p 8080:8080 --name ecommerce_java_war ecommerce_java_mvn:v1.2
