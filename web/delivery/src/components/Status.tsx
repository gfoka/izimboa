import type { Status } from "@/lib/api";
import { formatDateTime, PROGRESS, STATUS } from "@/lib/format";

export function StatusBadge({ status }: { status: Status }) {
  const s = STATUS[status];
  return <span className={`badge badge-${s.tone}`}>{s.label}</span>;
}

/** Barre de progression du parcours normal (créée → livrée). */
export function Progress({ status }: { status: Status }) {
  const off = status === "CANCELLED" || status === "RETURNED";
  const failed = status === "FAILED";
  const index = failed ? PROGRESS.indexOf("IN_TRANSIT") : PROGRESS.indexOf(status);
  const labels = ["Créée", "Livreur", "Ramassée", "En route", "Livrée"];
  return (
    <ol className={`progress${off ? " progress-off" : ""}${failed ? " progress-failed" : ""}`} aria-label="Avancement">
      {labels.map((label, i) => (
        <li key={label} className={i <= index ? "done" : undefined} aria-current={i === index ? "step" : undefined}>
          <span className="dot" aria-hidden="true" />
          <span className="step-label">{label}</span>
        </li>
      ))}
    </ol>
  );
}

export function Timeline({ events }: { events: { status: Status; at: string; note: string | null }[] }) {
  return (
    <ol className="timeline">
      {[...events].reverse().map((e, i) => (
        <li key={i} className={`tl-${STATUS[e.status].tone}`}>
          <span className="tl-dot" aria-hidden="true" />
          <div>
            <p className="tl-title">{e.status === "CREATED" ? "Livraison créée" : STATUS[e.status].label}</p>
            <p className="tl-meta">{formatDateTime(e.at)}{e.note ? ` · ${e.note}` : ""}</p>
          </div>
        </li>
      ))}
    </ol>
  );
}
