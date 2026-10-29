# Fiche Google Play « Izimboa Livreur »

Visuels publiés sur la fiche Play Store (icône 512 px, image de présentation 1024×500, 6 captures 1080×1920).
Les captures partent des rendus réels de l'application (`ScreensScreenshotTest`, dossier `app/build/screens/`),
mis en page par les fichiers HTML de `src/` (police Outfit/Inter du thème Izimboa) et rendus avec Playwright :

    cp ../app/build/screens/*.png src/ && cp ../../../infra/keycloak-theme/izimboa/login/resources/fonts/*.woff2 src/
    cd src && mkdir -p out && node render.js "$PWD"
