package com.izimboa.keycloak.whatsapp;

import java.util.List;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/** Déclare l'étape « Code WhatsApp » dans Keycloak (identifiant utilisé par le flux « izimboa-navigateur »). */
public class WhatsAppOtpAuthenticatorFactory implements AuthenticatorFactory {

    public static final String ID = "izimboa-whatsapp-otp";

    private WhatsAppClient client;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new WhatsAppOtpAuthenticator(client);
    }

    @Override
    public void init(Config.Scope config) {
        client = WhatsAppClient.fromEnvironment();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    // Libellés : clés de messages du thème (choix « Essayer une autre méthode »).
    @Override
    public String getDisplayType() {
        return "waDisplayName";
    }

    @Override
    public String getHelpText() {
        return "waHelpText";
    }

    @Override
    public String getReferenceCategory() {
        return "whatsapp-otp";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public Requirement[] getRequirementChoices() {
        return new Requirement[] {Requirement.ALTERNATIVE, Requirement.REQUIRED, Requirement.DISABLED};
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }
}
