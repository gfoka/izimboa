#!/usr/bin/env bash
# Izimboa — préparation initiale du VPS (Ubuntu 24.04), à lancer UNE fois en root :
#   ssh root@212.47.78.167 'bash -s' < infra/vps/bootstrap.sh
#
# Ce que fait le script (relançable sans danger) :
#   - mises à jour + mises à jour de sécurité automatiques
#   - fuseau horaire Africa/Douala, fichier d'échange (swap) de 4 Go
#   - utilisateur « deploy » (sudo + docker) avec les mêmes clés SSH que root
#   - pare-feu : seuls SSH (22), HTTP (80) et HTTPS (443) sont ouverts
#   - fail2ban contre les attaques par mot de passe sur SSH
#   - Docker + Docker Compose (dépôt officiel Docker)
#   - connexion SSH par mot de passe et connexion root désactivées, SEULEMENT si une clé SSH est déjà en place
set -euo pipefail
export DEBIAN_FRONTEND=noninteractive

echo "==> Mises à jour"
apt-get update -y
apt-get upgrade -y
apt-get install -y ca-certificates curl gnupg ufw fail2ban unattended-upgrades git
dpkg-reconfigure -f noninteractive unattended-upgrades

echo "==> Fuseau horaire et swap"
timedatectl set-timezone Africa/Douala
if ! swapon --show | grep -q '/swapfile'; then
  fallocate -l 4G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

echo "==> Docker"
if ! command -v docker >/dev/null; then
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  chmod a+r /etc/apt/keyrings/docker.asc
  . /etc/os-release
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${VERSION_CODENAME} stable" > /etc/apt/sources.list.d/docker.list
  apt-get update -y
  apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
fi
systemctl enable --now docker

echo "==> Utilisateur deploy"
id deploy >/dev/null 2>&1 || adduser --disabled-password --gecos "" deploy
usermod -aG sudo,docker deploy
echo "deploy ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/90-deploy && chmod 440 /etc/sudoers.d/90-deploy
mkdir -p /home/deploy/.ssh /srv/izimboa
if [ -s /root/.ssh/authorized_keys ]; then
  cp /root/.ssh/authorized_keys /home/deploy/.ssh/authorized_keys
fi
chown -R deploy:deploy /home/deploy/.ssh /srv/izimboa
chmod 700 /home/deploy/.ssh; [ -f /home/deploy/.ssh/authorized_keys ] && chmod 600 /home/deploy/.ssh/authorized_keys

echo "==> Pare-feu"
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 443/udp
ufw --force enable

echo "==> fail2ban"
cat > /etc/fail2ban/jail.d/sshd.local <<'J'
[sshd]
enabled = true
maxretry = 5
bantime = 1h
J
systemctl enable --now fail2ban
systemctl restart fail2ban

echo "==> SSH"
if [ -s /home/deploy/.ssh/authorized_keys ]; then
  cat > /etc/ssh/sshd_config.d/90-izimboa.conf <<'S'
PasswordAuthentication no
KbdInteractiveAuthentication no
PermitRootLogin no
S
  systemctl reload ssh || systemctl reload sshd
  echo "SSH durci : connexion par clé uniquement, utilisateur « deploy »."
else
  echo "ATTENTION : aucune clé SSH trouvée pour root. La connexion par mot de passe reste active."
  echo "Ajoutez votre clé (ssh-copy-id root@IP) puis relancez ce script."
fi

echo "==> Terminé. Connexion désormais : ssh deploy@$(curl -s -4 ifconfig.me || hostname -I | awk '{print $1}')"
