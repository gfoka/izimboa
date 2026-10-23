package com.izimboa;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Vérifie les frontières entre modules : aucun module n'utilise les classes internes d'un autre, pas de cycle. */
class ModularityTests {

    @Test
    void lesModulesRespectentLeursFrontieres() {
        ApplicationModules.of(IzimboaApplication.class).verify();
    }
}
