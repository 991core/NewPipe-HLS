# Qualification de NewPipe HLS v0.29.1-hls.1

Qualification locale effectuée le **8 octobre 2026**, sur une VM dédiée
**Android 11 / API 30**. Aucun téléphone physique n'a été testé.

## APK publié

- Fichier : `NewPipe-HLS-v0.29.1-hls.1.apk`.
- Nom affiché : **NewPipe HLS**.
- Paquet : `org.schabi.newpipe.debug.codexhlsworkaround`.
- Base : NewPipe 0.29.1, NewPipeExtractor 0.26.5 inchangé.
- SHA-256 : `c29d168945f0ab670c3758babee0bec47de0fc1271d7e61444402981ed4209f1`.
- Certificat de test SHA-256 : `9a0cdb7ff7c8b79d3b0bd04d69d57c1b482bdc8091257f6fe375beecf57cd3de`.
- Signature vérifiée par `apksigner`, schémas v1 et v2 valides.
- Mise à jour de la variante précédente réussie ; NewPipe officiel conservé.

Les six fichiers de lecture et de tests correspondent au patch précédemment
qualifié, avec seulement l'ajout d'en-têtes de licence sur les deux fichiers
nouveaux. La publication change le nom affiché, stabilise le paquet debug et
rend le chemin de la clé de signature configurable. Le nom interne Gradle amont
est conservé pour ses classes de ressources générées.

## Compilation et contrôles

`assembleDebug` et `runCheckstyle` passent. Les trois tests ciblés
`YoutubeHlsHelperTest` passent, sans échec ni erreur : variantes AVC/AAC exposées
via le manifeste principal, conservation du groupe audio externe, rejet d'une
variante indisponible au lieu d'une bascule silencieuse.

Le workflow GitHub manuel est fourni ; il n'a pas été exécuté dans cette qualification.

## Lecture de l'APK renommé

Les positions ci-dessous ont été lues dans les captures du lecteur, après une
lecture naturelle depuis le début. Les compteurs couvrent les fenêtres de test
consignées ; ils ne représentent pas tout le trafic de l'application.

| Cas | Vidéo publique | Position observée | HTTP 200 | Image et audio PCM |
| --- | --- | --- | ---: | --- |
| Vidéo signalée | `9HZ_tx8aWuA` | 00:51 | 36 | Vérifiés |
| Big Buck Bunny | `aqz-KE-bpKQ` | 00:53, puis commandes ci-dessous | 132 | Vérifiés |
| YouTube Developers | `M7lc1UVf-VE` | 02:39 | 80 | Vérifiés |
| Réouverture fraîche de la vidéo signalée | `9HZ_tx8aWuA` | 00:48 | 33 | Vérifiés |

Total : **281 réponses HTTP 200, aucun 403, aucune erreur de lecture relevée**.
Cinq captures PCM positives de cinq secondes ont été mesurées exclusivement
sur la sortie de cette VM, avec titre vérifié avant et après la mesure.
Le volume et les connexions audio ont été restaurés après chaque capture.

Sur Big Buck Bunny : passage de **720p60 à 480p**, pause à environ **02:15**,
déplacement dans la vidéo, puis reprise visible à **08:45**. L'audio a été
mesuré à nouveau après le changement de qualité. La réouverture de la vidéo
signalée utilise un nouveau processus et une nouvelle préparation HLS.

Une première mesure audio de la troisième vidéo a été interrompue avant
l'enregistrement, car le volume demandé n'était pas encore appliqué. La lecture
continuait ; une mesure exclusive positive a ensuite été obtenue après
stabilisation, sans relancer la vidéo. Cet incident concerne le protocole de mesure.

L'APK officiel installé a la même somme SHA-256 avant et après ces essais.
Les journaux détaillés, captures d'écran et enregistrements restent locaux ;
seules les mesures épurées sont publiées dans
[publication-validation.json](publication-validation.json).

## Portée

La qualification longue précédente est conservée dans
[baseline-validation.json](baseline-validation.json) : 504 HTTP 200 sans 403,
trois vidéos et fin naturelle de la vidéo signalée à 06:57.
L'APK renommé a fait l'objet de la requalification plus courte décrite ici.

Le témoin officiel et les sondes natives passaient aussi pendant la comparaison
précédente. Ces résultats ne démontrent donc ni la cause initiale des refus,
ni leur disparition durable sur tous les appareils et réseaux.
Les directs, contenus protégés, pistes multilingues, téléchargements et lecture
audio en arrière-plan ne sont pas qualifiés par ce contournement.
