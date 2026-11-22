# ETAPA 1: Compilar Geocoder con Maven y Java 8
FROM maven:3.9-eclipse-temurin-8 AS build

WORKDIR /app

COPY . .

RUN mvn -B -DskipTests clean package


# ETAPA 2: Ejecutar Geocoder en Tomcat 9
FROM tomcat:9.0-jdk8-temurin

# Limpiar aplicaciones de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar el WAR generado y descomprimirlo
COPY --from=build /app/target/*.war /tmp/geocoder.war

RUN mkdir -p /usr/local/tomcat/webapps/geocoder \
    && cd /usr/local/tomcat/webapps/geocoder \
    && jar -xf /tmp/geocoder.war \
    && rm /tmp/geocoder.war

# Incorporar script de configuración
COPY docker/geocoder/entrypoint.sh /usr/local/bin/entrypoint.sh

RUN chmod +x /usr/local/bin/entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
