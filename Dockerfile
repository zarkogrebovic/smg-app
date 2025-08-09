# This Dockerfile is for PostgreSQL only. The app uses Dockerfile.app.

# remove it
FROM postgres:latest

ENV POSTGRES_DB=challenge
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres

EXPOSE 5432
