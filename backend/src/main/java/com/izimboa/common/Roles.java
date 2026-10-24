package com.izimboa.common;

/** Rôles du realm Keycloak « izimboa ». */
public final class Roles {
    public static final String CLIENT = "client";
    public static final String VENDEUR = "vendeur";
    public static final String LIVREUR = "livreur";
    public static final String CONSEILLER = "conseiller";
    public static final String OPERATEUR = "operateur";
    /** Partenaire de flotte : possède des motos, gère ses propres livreurs. */
    public static final String PARTENAIRE = "partenaire";
    public static final String ADMIN = "admin";

    private Roles() {}
}
