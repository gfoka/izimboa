# Hébergement et déploiement

## Phase 1 — Lancement (économique)
- Un seul **VPS** pour tout le projet, avec **Docker Compose**.
- Taille minimale : **8 Go de RAM** (Spring Boot, Keycloak, PostgreSQL, Redis, RabbitMQ ensemble). Un VPS à 5 $ sera trop petit.
- Budget indicatif : ~10 à 15 €/mois (Hetzner, Contabo).
- **MinIO** sur le VPS : stockage compatible S3 → migration vers AWS par simple changement de configuration.
- **Sauvegardes automatiques hors du VPS** (seule protection avec un serveur unique).

## Phase 2 — Croissance : AWS région Paris
Paris répond souvent plus vite depuis le Cameroun (câbles sous-marins via l'Europe).
- **RDS** PostgreSQL avec réplique de secours
- **ElastiCache** Redis
- **S3** pour les documents chiffrés
- **ECS**, puis **EKS** (Kubernetes) quand le trafic grandit
- Sites Next.js : Vercel ou AWS Amplify, avec Cloudflare devant

Migration progressive, service par service, grâce à l'architecture hexagonale et à Docker.

## Point de vigilance
Le Cameroun a une loi récente sur la protection des données personnelles : vérifier si certaines données (notamment les documents d'identité de Go) doivent être stockées sur place.
