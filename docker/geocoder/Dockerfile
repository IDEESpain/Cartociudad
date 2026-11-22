
# ETAPA 1: Compilar el Geocoder con Maven y Java 8
FROM maven:3.9-eclipse-temurin-8 AS build

WORKDIR /app

COPY . .

RUN mvn -B -DskipTests clean package

# ETAPA 2: Ejecutar la aplicación en Tomcat 9
FROM tomcat:9.0-jdk8-temurin

# Limpiar aplicaciones por defecto
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar el WAR generado
COPY --from=build /app/target/*.war \
    /usr/local/tomcat/webapps/geocoder.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
