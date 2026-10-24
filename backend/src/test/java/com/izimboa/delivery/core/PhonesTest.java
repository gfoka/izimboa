package com.izimboa.delivery.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PhonesTest {

    @Test
    void lesFormatsCourantsDuCamerounSontNormalises() {
        assertThat(Phones.normalize("6 99 12 34 56")).contains("+237699123456");
        assertThat(Phones.normalize("699-12-34-56")).contains("+237699123456");
        assertThat(Phones.normalize("+237 699 12 34 56")).contains("+237699123456");
        assertThat(Phones.normalize("00237699123456")).contains("+237699123456");
        assertThat(Phones.normalize("237699123456")).contains("+237699123456");
        assertThat(Phones.normalize("233 42 12 34")).contains("+237233421234");
    }

    @Test
    void lesNumerosEtrangersAvecIndicatifSontAcceptes() {
        assertThat(Phones.normalize("+33 6 12 34 56 78")).contains("+33612345678");
    }

    @Test
    void lesNumerosInvalidesSontRefuses() {
        assertThat(Phones.normalize("12345")).isEmpty();
        assertThat(Phones.normalize("bonjour")).isEmpty();
        assertThat(Phones.normalize(null)).isEmpty();
    }
}
