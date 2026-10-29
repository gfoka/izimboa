// Extension Keycloak : connexion par code WhatsApp (décision 40). Produit un .jar déposé dans /opt/keycloak/providers.
plugins { java }

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories { mavenCentral() }

val keycloakVersion = "26.3.5" // même version que l'image quay.io/keycloak/keycloak:26.3 du serveur

dependencies {
    // Fournies par Keycloak à l'exécution : jamais embarquées dans le .jar.
    compileOnly("org.keycloak:keycloak-server-spi:$keycloakVersion")
    compileOnly("org.keycloak:keycloak-server-spi-private:$keycloakVersion")
    compileOnly("org.keycloak:keycloak-services:$keycloakVersion")
    compileOnly("org.keycloak:keycloak-core:$keycloakVersion")
    compileOnly("org.jboss.logging:jboss-logging:3.6.1.Final")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.testImplementation { extendsFrom(configurations.compileOnly.get()) }

tasks.test { useJUnitPlatform() }

tasks.jar { archiveFileName.set("izimboa-whatsapp-otp.jar") }
