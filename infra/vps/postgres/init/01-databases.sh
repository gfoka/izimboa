#!/bin/sh
# Exécuté une seule fois, au premier démarrage du conteneur (volume vide).
# Crée deux bases séparées : « izimboa » (données métier) et « keycloak » (comptes, sessions).
# Les schémas métier (common, delivery, go, fly, buy, saas) sont créés par les migrations
# Flyway de l'API, pas ici.
set -eu

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-SQL
  CREATE ROLE izimboa LOGIN PASSWORD '${IZIMBOA_DB_PASSWORD}';
  CREATE DATABASE izimboa OWNER izimboa ENCODING 'UTF8';
  CREATE ROLE keycloak LOGIN PASSWORD '${KEYCLOAK_DB_PASSWORD}';
  CREATE DATABASE keycloak OWNER keycloak ENCODING 'UTF8';
SQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname izimboa <<-SQL
  REVOKE CREATE ON SCHEMA public FROM PUBLIC;
  CREATE EXTENSION IF NOT EXISTS pgcrypto;
SQL
