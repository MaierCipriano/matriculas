FROM eclipse-temurin:11-jdk-alpine

WORKDIR /app

# Copy source code
COPY src /app/src
# Copy static resources to the path expected by Main.java (enrollment-webapp/static/...)
COPY static /app/enrollment-webapp/static

# Compile
RUN mkdir -p /app/classes
RUN javac -d /app/classes src/Main.java

# Run
CMD ["java", "-cp", "/app/classes", "Main"]
