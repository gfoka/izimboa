// Nettoyage après export statique : on ne publie qu'une seule page 404 (404.html),
// servie avec le vrai statut HTTP 404 par l'hébergement. Les copies /404/ et /_not-found/
// répondraient en 200 et créeraient des URL parasites pour Google.
import { rmSync, existsSync } from "node:fs";

for (const dir of ["out/404", "out/_not-found"]) {
  if (existsSync(dir)) rmSync(dir, { recursive: true, force: true });
}
console.log("postbuild : pages 404 parasites supprimées");
