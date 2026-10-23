package com.izimboa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Izimboa : monolithe modulaire.
 * Chaque sous-package direct de com.izimboa est un module (common, delivery, puis go, fly, buy...).
 * Règle : un module n'accède jamais aux tables ni aux classes internes d'un autre module
 * (vérifié automatiquement par Spring Modulith dans les tests).
 */
@SpringBootApplication
public class IzimboaApplication {

    public static void main(String[] args) {
        SpringApplication.run(IzimboaApplication.class, args);
    }
}
