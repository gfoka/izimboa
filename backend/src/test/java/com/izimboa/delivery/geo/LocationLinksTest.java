package com.izimboa.delivery.geo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Les formats de localisation que les clients envoient réellement aux vendeurs. */
class LocationLinksTest {

    static GeoPoint p(double lat, double lng) {
        return new GeoPoint(lat, lng);
    }

    @Test
    void lienWhatsAppEtGoogleMapsAvecQ() {
        assertThat(LocationLinks.parse("https://maps.google.com/maps?q=4.0612%2C9.7421&z=17&hl=fr")).contains(p(4.0612, 9.7421));
        assertThat(LocationLinks.parse("https://maps.google.com/?q=4.0612,9.7421")).contains(p(4.0612, 9.7421));
        assertThat(LocationLinks.parse("https://www.google.com/maps/search/?api=1&query=4.0612,9.7421")).contains(p(4.0612, 9.7421));
        assertThat(LocationLinks.parse("https://www.google.com/maps?ll=4.0612,9.7421&z=15")).contains(p(4.0612, 9.7421));
    }

    @Test
    void lienDeLieuPrefereLePointChoisiAuCentreDeLaVue() {
        String url = "https://www.google.com/maps/place/Carrefour+Kotto/@4.0901,9.7512,17z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d4.0899111!4d9.7534222";
        assertThat(LocationLinks.parse(url)).contains(p(4.089911, 9.753422));
        assertThat(LocationLinks.parse("https://www.google.com/maps/@4.0511,9.7679,16z")).contains(p(4.0511, 9.7679));
        assertThat(LocationLinks.parse("https://www.google.com/maps/place/4.0612,9.7421")).contains(p(4.0612, 9.7421));
        assertThat(LocationLinks.parse("https://www.google.com/maps/search/4.0612,+9.7421?entry=tts")).contains(p(4.0612, 9.7421));
    }

    @Test
    void coordonneesEnTexteEtEnDegres() {
        assertThat(LocationLinks.parse("4.0612, 9.7421")).contains(p(4.0612, 9.7421));
        assertThat(LocationLinks.parse(" 4.0612 9.7421 ")).contains(p(4.0612, 9.7421));
        assertThat(LocationLinks.parse("geo:4.0612,9.7421?z=17")).contains(p(4.0612, 9.7421));
        assertThat(LocationLinks.parse("4°03'40.3\"N 9°44'31.6\"E")).hasValueSatisfying(g -> {
            assertThat(g.lat()).isCloseTo(4.06119, org.assertj.core.data.Offset.offset(1e-4));
            assertThat(g.lng()).isCloseTo(9.74211, org.assertj.core.data.Offset.offset(1e-4));
        });
    }

    @Test
    void openStreetMapEtApplePlans() {
        assertThat(LocationLinks.parse("https://www.openstreetmap.org/?mlat=4.0612&mlon=9.7421#map=17/4.0612/9.7421")).contains(p(4.0612, 9.7421));
        assertThat(LocationLinks.parse("https://www.openstreetmap.org/#map=17/4.0612/9.7421")).contains(p(4.0612, 9.7421));
        assertThat(LocationLinks.parse("https://maps.apple.com/?ll=4.0612,9.7421&q=Position")).contains(p(4.0612, 9.7421));
    }

    @Test
    void lienCourtSansCoordonneesEtTexteSansPosition() {
        assertThat(LocationLinks.parse("https://maps.app.goo.gl/AbC123xyz")).isEmpty();
        assertThat(LocationLinks.parse("Bonamoussadi, carrefour Kotto")).isEmpty();
        assertThat(LocationLinks.parse("0,0")).isEmpty();
        assertThat(LocationLinks.parse("95.1, 9.7")).isEmpty();
        assertThat(LocationLinks.firstUrl("Voici ma position : https://maps.app.goo.gl/AbC123xyz.")).contains("https://maps.app.goo.gl/AbC123xyz");
    }

    @Test
    void lePageDeConsentementEstSuivieSansSortirDesDomainesAutorises() {
        var resolver = new LocationLinkResolver();
        assertThat(resolver.resolve("http://169.254.169.254/latest")).isEmpty();
        assertThat(resolver.resolve("https://exemple.com/?q=x")).isEmpty();
        assertThat(resolver.resolve("Ma position https://maps.google.com/?q=4.0612,9.7421")).contains(p(4.0612, 9.7421));
    }
}
