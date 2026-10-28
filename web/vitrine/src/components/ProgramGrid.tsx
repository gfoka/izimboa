"use client";

import { useState } from "react";
import type { Destination, Goal } from "@/content/go-types";

export type GridItem = { key: string; name: string; intro: string; destination: Destination; goal: Goal; href: string; status?: string };

/** Grille des programmes, filtrable par objectif (tous les liens restent dans le HTML pour Google). */
export function ProgramGrid({ items, goals, goalLabels, destinations, all, filterLabel, readMore, statusLabels }: {
  items: GridItem[];
  goals: Goal[];
  goalLabels: Record<Goal, string>;
  destinations: Record<Destination, string>;
  all: string;
  filterLabel: string;
  readMore: string;
  statusLabels: Record<string, string>;
}) {
  const [goal, setGoal] = useState<Goal | null>(null);
  const shown = goals.filter((g) => items.some((i) => i.goal === g));
  return (
    <>
      <div className="chips" role="group" aria-label={filterLabel}>
        <button type="button" className={"chip" + (goal === null ? " is-on" : "")} aria-pressed={goal === null} onClick={() => setGoal(null)}>
          {all}
        </button>
        {shown.map((g) => (
          <button key={g} type="button" className={"chip" + (goal === g ? " is-on" : "")} aria-pressed={goal === g} onClick={() => setGoal(g)}>
            {goalLabels[g]}
          </button>
        ))}
      </div>
      <ul className="program-grid">
        {items.map((p) => (
          <li key={p.key} hidden={goal !== null && p.goal !== goal}>
            <a href={p.href} className="program-card">
              <span className="program-tags">
                <span className="dest-chip">{destinations[p.destination]}</span>
                <span className="goal-chip">{goalLabels[p.goal]}</span>
              </span>
              <strong className="program-name">{p.name}</strong>
              <span className="program-intro">{p.intro}</span>
              {p.status && statusLabels[p.status] && <span className="program-status">{statusLabels[p.status]}</span>}
              <span className="service-more">{readMore} →</span>
            </a>
          </li>
        ))}
      </ul>
    </>
  );
}
