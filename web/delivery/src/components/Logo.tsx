/** Logo Izimboa Delivery : la tige du « i » et le point-colis (variante produit, voir docs/07-identite-visuelle.md). */
export function DeliverySymbol({ size = 34, stem = "#0B2545", color = "#F28C28" }: { size?: number; stem?: string; color?: string }) {
  return (
    <svg viewBox="0 0 64 64" width={size} height={size} aria-hidden="true" focusable="false">
      <path d="M5 11 H11" stroke={color} strokeWidth="3" strokeLinecap="round" />
      <path d="M3 17 H11" stroke={color} strokeWidth="3" strokeLinecap="round" />
      <rect x="17" y="5" width="18" height="18" rx="3" fill={color} transform="rotate(-12 26 14)" />
      <path d="M26 5 V23" stroke="#fff" strokeWidth="2.5" transform="rotate(-12 26 14)" />
      <rect x="18" y="28" width="14" height="32" rx="7" fill={stem} />
    </svg>
  );
}

export function Logo({ inverted = false }: { inverted?: boolean }) {
  const ink = inverted ? "#fff" : "#0B2545";
  return (
    <span className="logo">
      <DeliverySymbol stem={ink} />
      <span className="logo-word" style={{ color: ink }}>
        izimboa <span className="logo-product">delivery</span>
      </span>
    </span>
  );
}
