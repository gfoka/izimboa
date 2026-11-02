package com.izimboa.common.whatsapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.izimboa.common.WhatsAppMessages;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WhatsAppConfigTest {

    @Test
    void corpsDUnMessageAModeleAvecBoutonLien() {
        var body = WhatsAppConfig.payload(new WhatsAppMessages.Template("+237699000002", "izimboa_livraison_creee", "fr",
                List.of("Awa", "Chaussures"), "tok123"));
        assertThat(body).containsEntry("to", "237699000002").containsEntry("type", "template")
                .containsEntry("messaging_product", "whatsapp");
        var template = (Map<?, ?>) body.get("template");
        assertThat(template.get("name")).isEqualTo("izimboa_livraison_creee");
        assertThat(template.get("language")).isEqualTo(Map.of("code", "fr"));
        var components = (List<?>) template.get("components");
        assertThat(components).hasSize(2);
        assertThat(components.get(1).toString()).contains("sub_type=url", "index=0", "text=tok123");
    }

    @Test
    void sansBoutonNiVariables() {
        var body = WhatsAppConfig.payload(new WhatsAppMessages.Template("+237699000002", "x", "fr", List.of(), null));
        assertThat((List<?>) ((Map<?, ?>) body.get("template")).get("components")).isEmpty();
    }

    @Test
    void leJournalMasqueLeNumero() {
        assertThat(WhatsAppConfig.mask("+237699000002")).isEqualTo("+23769900****");
    }

    @Test
    void sansJetonRienNePart() {
        var off = new WhatsAppConfig().whatsAppMessages("", "1", "http://localhost:1");
        assertThat(off.send(new WhatsAppMessages.Template("+237699000002", "x", "fr", List.of(), null)).status())
                .isEqualTo(WhatsAppMessages.Status.DISABLED);
    }
}
