import { API_URL } from "./config";

/** Erreur renvoyée par l'API (format Problem Details). */
export class ApiError extends Error {
  constructor(public status: number, public code: string | undefined, message: string) {
    super(message);
  }
}

const GENERIC: Record<number, string> = {
  400: "Certaines informations sont manquantes ou incorrectes.",
  401: "Votre session a expiré. Reconnectez-vous.",
  403: "Vous n'avez pas accès à cette action.",
  404: "Introuvable.",
  409: "Cette action n'est plus possible à cette étape.",
  500: "Le service rencontre un problème. Réessayez dans un instant.",
};

export async function api<T>(
  path: string,
  options: { method?: string; body?: unknown; token?: string | null; headers?: Record<string, string> } = {},
): Promise<T> {
  let res: Response;
  try {
    res = await fetch(API_URL + path, {
      method: options.method ?? "GET",
      headers: {
        Accept: "application/json",
        ...(options.body !== undefined ? { "Content-Type": "application/json" } : {}),
        ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
        ...options.headers,
      },
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    });
  } catch {
    throw new ApiError(0, "network", "Pas de connexion. Vérifiez votre réseau et réessayez.");
  }
  if (res.ok) return (res.status === 204 ? undefined : await res.json()) as T;
  let problem: { detail?: string; code?: string } = {};
  try {
    problem = await res.json();
  } catch {
    /* corps vide */
  }
  const useDetail = problem.code && problem.detail;
  throw new ApiError(res.status, problem.code, (useDetail ? problem.detail : GENERIC[res.status]) ?? GENERIC[500]);
}

// ---- Formats de l'API Delivery ----

export type Status =
  | "CREATED" | "ASSIGNED" | "PICKED_UP" | "IN_TRANSIT" | "DELIVERED" | "FAILED" | "RETURNED" | "CANCELLED";

export type GeoPoint = { lat: number; lng: number };

export type Quote = {
  currency: string; distanceKm: number; onRequest: boolean;
  basePrice: number; expressSurcharge: number; codCommission: number; total: number;
};

export type Stop = { lat: number; lng: number; address: string; landmark: string | null };

export type Delivery = {
  id: string; reference: string; status: Status; trackingUrl: string;
  pickup: Stop; pickupPhone: string; dropoff: Stop; recipientName: string; recipientPhone: string;
  packageDescription: string | null; express: boolean; distanceKm: number;
  pricing: {
    currency: string; basePrice: number; expressSurcharge: number; codAmount: number; codCommission: number;
    totalFee: number; feePayment: "DEDUCTED_FROM_COD" | "PREPAID"; returnFee: number | null; sellerPayout: number;
  };
  handoverCode: string | null; handoverFailures: number; courierId: string | null; deliveryAttempts: number;
  createdAt: string; updatedAt: string;
  timeline: { status: Status; at: string; note: string | null }[];
  dropoffState: DropoffState;
};

/** FIXED : lieu fixé par le vendeur ; TO_CONFIRM : le destinataire doit l'indiquer ; CONFIRMED : indiqué par lui. */
export type DropoffState = "FIXED" | "TO_CONFIRM" | "CONFIRMED";

export type DeliverySummary = {
  id: string; reference: string; status: Status; dropoffAddress: string; recipientName: string;
  express: boolean; codAmount: number; totalFee: number; courierId: string | null; createdAt: string; updatedAt: string;
  dropoffState: DropoffState;
};

export type Tracking = {
  reference: string; status: Status; express: boolean; dropoffLandmark: string | null;
  amountToPay: number; currency: string; timeline: { status: Status; at: string; note: string | null }[];
  dropoffRequest: { state: DropoffState; editable: boolean; lat: number; lng: number; area: string; maxShiftMeters: number } | null;
};

export type Place = { name: string; detail: string; lat: number; lng: number };

export type Me = { id: string; email: string | null; name: string | null; roles: string[]; memberSince: string };
