package com.izimboa.delivery.geo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PlaceSearchTest {

    @Test
    void detailCourtSansRegionNiPays() {
        assertThat(PlaceSearch.shortDetail(
                "Rond-Point de Deido, Bonatene, Deido, Douala I, Communauté urbaine de Douala, Wouri, Région du Littoral, Cameroun",
                "Rond-Point de Deido")).isEqualTo("Bonatene, Deido, Douala I");
        assertThat(PlaceSearch.shortDetail("Bonamoussadi, Bonagang, Douala V, Communauté urbaine de Douala, Wouri, Région du Littoral, Cameroun",
                "Bonamoussadi")).isEqualTo("Bonagang, Douala V");
    }
}
