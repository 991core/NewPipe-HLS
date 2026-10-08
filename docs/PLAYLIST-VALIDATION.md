# Qualification de NewPipe HLS v0.29.1-hls.2

Qualification locale du **8 octobre 2026**, sur une VM dédiée **Android 11 / API 30**.

## Comportement

Un clic sur un titre dans une playlist locale ou YouTube transmet désormais la
file de la playlist et la position sélectionnée au lecteur. Les titres restants
sont joués dans leur ordre, puis les recommandations prennent le relais.
Cette continuation après playlist fonctionne même si l'ajout automatique est
désactivé pour les vidéos ouvertes individuellement. Ce réglage général n'est
pas modifié. La répétition conserve sa priorité lorsqu'elle est activée.

Pour YouTube, les pages suivantes restent chargées par la file de playlist.
Les recommandations ne sont autorisées qu'une fois cette file complète et le
dernier titre atteint. Le curseur de pagination correspond aux pages déjà
chargées dans l'écran de playlist. Une file isolée ne peut pas remplacer le
contexte de playlist simplement parce que son titre sélectionné est identique.

La section des suggestions YouTube reste consultable ; la file du lecteur
indique les titres effectivement programmés.

## APK et contrôles locaux

- Fichier : `NewPipe-HLS-v0.29.1-hls.2.apk`.
- Version Android : `0.29.1-hls.2`, code **1016**.
- Paquet conservé : `org.schabi.newpipe.debug.codexhlsworkaround`.
- SHA-256 : `a71fe36b4b1ad2182963233bd93e181dc75a272d212e988590337bf7726631fd`.
- Certificat de test SHA-256 : `9a0cdb7ff7c8b79d3b0bd04d69d57c1b482bdc8091257f6fe375beecf57cd3de`.
- Signature vérifiée avec `apksigner` ; schémas v1 et v2 valides.
- Installation en mise à jour réussie, sans effacement des données de la variante.

`assembleDebug` et `runCheckstyle` passent. **25 tests ciblés** passent sans
échec ni erreur : neuf tests de continuation de playlist, treize tests existants
de file, trois tests HLS. Ils couvrent notamment l'ordre, les doublons, la
sérialisation de la position et du contexte, les sources de playlists distinctes,
la pagination et la priorité de la playlist sur les recommandations.
Le workflow GitHub manuel inclut ces tests ; il n'a pas été exécuté ici.

## Playlist locale : parcours réel

Playlist créée dans la VM avec trois vidéos publiques : `9HZ_tx8aWuA`,
`aqz-KE-bpKQ`, `M7lc1UVf-VE`. Avec l'ancien APK `hls.1`, un clic sur le deuxième
titre ouvrait une file contenant un seul titre. Avec l'APK final `hls.2`, le
même clic conserve les trois titres et sélectionne l'index 1.

Après déplacement vers la fin du deuxième titre, sa fin naturelle lance le
troisième (index 2). Après déplacement vers la fin du troisième, sa fin naturelle
lance la première recommandation (index 3), `HzGOWq5UyjY`, ajoutée après la
playlist. Les deux transitions sont automatiques, sans commande « Suivant ».
Le réglage général d'ajout automatique reste désactivé pendant cet essai.

L'image est vérifiée sur les titres joués. Deux captures PCM exclusives de cinq
secondes sont positives : troisième titre et première recommandation, avec titre
vérifié avant et après la mesure. Volume et connexions audio de la VM restaurés.
Les fenêtres de journaux dédupliquées comptent **160 HTTP 200**, aucun 403 et
aucune erreur de lecture relevée. Ce compteur ne représente pas tout le trafic.
Les vidéos ont été avancées vers leur fin ; elles n'ont pas été lues intégralement
pendant ce parcours.

Un geste de contrôle du protocole a temporairement mis le dernier titre en pause ;
la lecture a été reprise et la transition naturelle ensuite constatée. Une mesure
audio préparatoire a été interrompue avant enregistrement, puis une capture
positive a été obtenue pendant la lecture de la recommandation. Les résultats
ci-dessus se rapportent aux observations finales.

## Playlist YouTube

Playlist publique `PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj`, affichant 200 titres lors
du test. Un clic sur le deuxième titre (« Die With A Smile », `kPa7bsKwL-c`)
ouvre la file des 100 premiers titres à l'index 1. Après déplacement vers sa fin,
le lecteur lance automatiquement le troisième titre de cette playlist
(« Let Me Love You », `euCqAq6BRa4`), à l'index 2, sans commande « Suivant ».
L'image et une capture audio PCM exclusive positive sont vérifiées sur chacun.

Les fenêtres de journaux dédupliquées de ce parcours comptent **120 HTTP 200**,
aucun 403 et aucune erreur de lecture relevée. Les quatre captures PCM positives
de cette qualification (deux locales, deux YouTube) durent chacune cinq secondes.

La récupération effective de la deuxième page et la fin des 200 titres n'ont pas
été parcourues dans la VM. La conservation du curseur et le chargement des pages
sont vérifiés dans le code ; les tests de file vérifient la pagination, sa
sérialisation et l'autorisation des recommandations une fois la file complète.

## Portée et conservation

Le contournement HLS et NewPipeExtractor 0.26.5 sont conservés. La qualification
HLS précédente reste dans [VALIDATION.md](VALIDATION.md). Aucun téléphone
physique n'est qualifié ici. Les tests de cette version démontrent les parcours
de playlist décrits ; ils ne démontrent pas une disparition durable des refus
YouTube sur tous les réseaux et appareils.

L'APK officiel conserve la même somme SHA-256 avant et après les essais. Les
playlists locales et leurs entrées sont identiques à la sauvegarde initiale ;
l'identité, le contenu et l'ordre des favoris distants sont préservés. L'indice
interne d'affichage du favori distant a été renuméroté pendant l'utilisation du
jeu d'essai, sans changement de son ordre visible. La playlist de test a été
retirée. Les réglages de lecture initiaux, le volume et les connexions audio sont
restaurés.

Les fichiers bruts de diagnostic, sauvegardes, captures et audio restent locaux.
Seules les mesures épurées sont publiées dans
[playlist-validation.json](playlist-validation.json). La clé de signature reste
privée ; le dépôt conserve la licence et les crédits amont.
