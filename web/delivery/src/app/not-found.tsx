import Link from "next/link";

export default function NotFound() {
  return (
    <main className="container page narrow center">
      <h1>Page introuvable</h1>
      <p className="muted">Cette adresse n'existe pas ou n'est plus disponible.</p>
      <Link href="/" className="btn btn-navy">Retour à mes livraisons</Link>
    </main>
  );
}
