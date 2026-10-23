import type { ProductKey } from "@/config/site";

const NAVY = "#0B2545";
const SUN = "#F28C28";

/** Symbole Izimboa : un « i » dont le point est une flèche vers le haut et la droite. */
export function Symbol({ size = 40, stem = NAVY, accent = SUN }: { size?: number; stem?: string; accent?: string }) {
  return (
    <svg viewBox="0 0 64 64" width={size} height={size} aria-hidden="true" focusable="false">
      <rect x="18" y="28" width="14" height="32" rx="7" fill={stem} />
      <path d="M27 21 L45 5" stroke={accent} strokeWidth="7" strokeLinecap="round" fill="none" />
      <path d="M32 5 H45 V18" stroke={accent} strokeWidth="7" strokeLinecap="round" strokeLinejoin="round" fill="none" />
    </svg>
  );
}

/** Variante produit : la tige reste, seul le point change. */
export function ProductSymbol({ product, size = 40, stem = NAVY, color }: { product: ProductKey; size?: number; stem?: string; color: string }) {
  return (
    <svg viewBox="0 0 64 64" width={size} height={size} aria-hidden="true" focusable="false">
      {product === "delivery" && (
        <>
          <path d="M5 11 H11" stroke={color} strokeWidth="3" strokeLinecap="round" />
          <path d="M3 17 H11" stroke={color} strokeWidth="3" strokeLinecap="round" />
          <rect x="17" y="5" width="18" height="18" rx="3" fill={color} transform="rotate(-12 26 14)" />
          <path d="M26 5 V23" stroke="#fff" strokeWidth="2.5" transform="rotate(-12 26 14)" />
        </>
      )}
      {product === "go" && (
        <>
          <path d="M27 21 L45 5" stroke={color} strokeWidth="7" strokeLinecap="round" fill="none" />
          <path d="M32 5 H45 V18" stroke={color} strokeWidth="7" strokeLinecap="round" strokeLinejoin="round" fill="none" />
        </>
      )}
      {product === "fly" && (
        <>
          <path d="M14 18 L50 3 L37 29 L31 20 Z" fill={color} stroke={color} strokeWidth="2" strokeLinejoin="round" />
          <path d="M31 20 L50 3" stroke="#fff" strokeWidth="2" strokeLinecap="round" />
        </>
      )}
      {product === "buy" && (
        <g transform="translate(31 13) rotate(-45) scale(0.8) translate(-16 -10)">
          <path d="M0 6 Q0 0 6 0 H22 L32 10 L22 20 H6 Q0 20 0 14 Z" fill={color} />
          <circle cx="23" cy="10" r="2.8" fill="#fff" />
        </g>
      )}
      <rect x="18" y="28" width="14" height="32" rx="7" fill={stem} />
    </svg>
  );
}

/** Logo complet : symbole + « izimboa ». */
export function Logo({ inverted = false }: { inverted?: boolean }) {
  return (
    <span className="logo">
      <Symbol size={34} stem={inverted ? "#fff" : NAVY} />
      <span className="logo-word" style={{ color: inverted ? "#fff" : NAVY }}>
        izimboa
      </span>
    </span>
  );
}
