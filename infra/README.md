# infra — Infrastructure et déploiement

**Phase 1 (lancement) :** un VPS (8 Go de RAM minimum, ~10-15 €/mois chez Hetzner ou Contabo) avec Docker Compose :
Spring Boot, Keycloak, PostgreSQL, Redis, RabbitMQ, MinIO (stockage compatible S3).
Sauvegardes automatiques **hors du VPS**.

**Phase 2 (croissance) :** AWS région Paris — RDS, ElastiCache, S3, ECS puis EKS. Cloudflare devant tout.

Environnements : développement, préproduction, production.

_Vide pour l'instant._
