FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY src ./src
RUN javac -d out src/*.java

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/out ./out
COPY public ./public
EXPOSE 8080
CMD ["java", "-cp", "out", "PortfolioServer"]