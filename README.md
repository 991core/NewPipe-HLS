# NewPipe HLS

Fork expérimental de [NewPipe](https://github.com/TeamNewPipe/NewPipe), basé sur
**NewPipe 0.29.1** avec **NewPipeExtractor 0.26.5**. La lecture vidéo YouTube à
la demande privilégie HLS lorsqu’un manifeste compatible est disponible.

Le projet est indépendant de TeamNewPipe. Le code amont, ses crédits et sa licence
sont conservés. Cette variante est destinée à qualifier un contournement des
refus HTTP 403 intermittents ; elle ne garantit pas leur disparition sur tous
les appareils et réseaux.

## Installer

Télécharger l’APK et `SHA256SUMS` dans les
[Releases](https://github.com/991core/NewPipe-HLS/releases).
L’application apparaît sous **NewPipe HLS** et s’installe à côté de NewPipe officiel.

- Paquet : `org.schabi.newpipe.debug.codexhlsworkaround`.
- Version de base : `0.29.1` ; publication HLS : `v0.29.1-hls.1`.
- Android minimum : 6.0 / API 23.
- APK de test debuggable, signé avec une clé de test distincte de la clé officielle.
- Compatible en mise à jour avec la variante précédente **NewTube HLS Test**,
  car son paquet et sa clé de signature sont conservés.

Ouvrir ou partager un lien YouTube vers **NewPipe HLS**, puis sélectionner
**Video player**. Pour revenir à l’application officielle, ouvrir NewPipe ;
on peut désinstaller uniquement NewPipe HLS en conservant NewPipe officiel.

## Changement de lecture

`YoutubeHlsHelper` prépare le manifeste principal durant l’extraction asynchrone
et expose les variantes AVC/H.264 + AAC comme qualités vidéo. Le resolver vidéo
privilégie ces variantes et laisse ExoPlayer gérer les pistes HLS.
Le parser sélectionne la qualité demandée tout en conservant ses groupes audio
et sous-titres associés. Il évite ainsi de perdre l’audio externe au flux vidéo.

Le client YouTube reste **VISIONOS** et l’extracteur reste inchangé. Aucun
poToken fixe, service de jetons, proxy ou mécanisme de retry/bascule sur erreur
n’a été ajouté. Une erreur HLS reste visible. En absence initiale d’URL HLS,
le chemin existant est conservé ; un manifeste fourni mais invalide échoue.

Les téléchargements conservent les formats directs. La lecture audio ordinaire
en arrière-plan conserve également les formats directs ; le contournement HLS
ne la qualifie pas. Les directs, contenus protégés et pistes multilingues n’ont
pas été qualifiés. Les variantes AV1/HEVC/Opus sont exclues de ce contournement.

## Validation

La variante fonctionnelle précédente a été testée sur trois vidéos dans une VM
Android 11 / API 30 : **504 réponses HTTP 200, aucun 403**, image, audio PCM,
changement de qualité, pause/reprise, seek et extraction fraîche.
La vidéo signalée a joué jusqu’à sa fin naturelle à 6 min 57 s.
Le témoin officiel passait aussi pendant cette comparaison : l’élimination
causale et durable du refus intermittent n’est donc pas démontrée.
Aucun téléphone physique n’a été testé par le mainteneur pendant cette qualification.

La publication conserve le même code de lecture. Ses changements supplémentaires
portent sur le nom de l’application/projet, la stabilité du paquet et la
configuration de signature pour permettre la compilation hors du poste de test.
La qualification propre à l’APK publié est consignée dans
[docs/VALIDATION.md](docs/VALIDATION.md).

## Compiler

Prérequis : JDK 21, Android SDK avec la plateforme 37.0 et Build Tools 36.0.0,
licences SDK acceptées par le constructeur, `ANDROID_HOME` configuré.

```sh
git clone https://github.com/991core/NewPipe-HLS.git
cd NewPipe-HLS
./gradlew :app:assembleDebug :app:runCheckstyle :app:testDebugUnitTest \
  --tests org.schabi.newpipe.util.YoutubeHlsHelperTest --no-daemon
```

Sortie : `app/build/outputs/apk/debug/app-debug.apk`.
Sans configuration supplémentaire, Gradle utilise votre propre clé debug.
La clé qui signe l’APK publié reste privée et n’est pas incluse dans le dépôt.
Un APK signé avec une autre clé ne peut pas mettre à jour l’APK publié : exporter
les données de la variante avant de changer de signature.

Pour un constructeur qui possède sa propre clé compatible avec la configuration
debug, on peut fournir `-PhlsSigningKeystore=/chemin/vers/la-cle.jks`.
Ne pas committer une clé de signature ni un fichier contenant ses secrets.
Le workflow **HLS build checks** est déclenchable manuellement ; ses builds
utilisent leur propre clé debug et ne sont pas les APK signés de la publication.

## Origine et licence

Base amont : tag `v0.29.1`, commit
`00acf2f318b0af7ffacbd687bba011b642cc01d0`.
Extracteur : `v0.26.5`, inchangé.
Les ajouts HLS et les tests ont été développés avec assistance LLM puis revus,
compilés et vérifiés localement. Cette assistance ne constitue pas un audit
indépendant ni une preuve de résolution permanente des refus YouTube.

Licence du projet : **GPL-3.0-or-later** ; voir [LICENSE](LICENSE).
Merci à TeamNewPipe, aux contributeurs de NewPipe et de NewPipeExtractor.
Le [README amont](README.upstream.md) conserve les informations et crédits du
projet d’origine ; ses badges, liens de téléchargement et politiques se rapportent
à TeamNewPipe.
