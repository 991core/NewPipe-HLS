# Qualification physique de la continuité de lecture

Essais du 11 au 12 octobre 2026 sur une Samsung SM-X200, Android 14 / API 34.
La version `hls.2` a été installée puis remplacée par les candidates `hls.3`
sans effacer les données. Les APK conservent le paquet et la clé de test.
Le réseau initial de la tablette est comparé à un point d'accès mobile décrit
comme 5G par l'utilisateur ; le type de liaison mobile n'est pas mesuré ici.

## Mesure

Le volume média de la tablette reste à zéro. Le signal de sortie interne est
enregistré en FLAC avec scrcpy 5.0.1, sans microphone et sans lecture sur le PC.
Chaque cycle de 40 secondes comprend deux rotations, un verrouillage et un
réveil. Les silences d'au moins 100 ms à moins de -55 dB sont recherchés avec
FFmpeg, puis comparés aux transitions du lecteur. Un silence musical naturel
ne suffit pas à attribuer une coupure au lecteur.

Les captures d'écran, enregistrements et journaux bruts restent privés. Aucun
identifiant USB, SSID domestique, adresse réseau, URL signée, jeton ni donnée
applicative n'est publié dans ce dépôt.

## Résultats de la comparaison

| Variante et réseau | Rotation | Verrouillage | Réveil |
| --- | --- | --- | --- |
| `hls.2`, réseau domestique, Fade To Black | Silences de 1,133 s et 1,069 s ; source rechargée | Silence de 0,557 s ; passage HLS vers audio direct | Audio direct conservé |
| Première candidate, réseau domestique | Rechargement évité | HLS conservé | Silence numérique résiduel de 0,321 s |
| Première candidate, point d'accès mobile, Big Buck Bunny | Pas de silence détecté | Pas de silence détecté | 7,705 s de silence avec remise en mémoire tampon |
| Candidate corrigée, point d'accès mobile, Big Buck Bunny | Pas de silence détecté | Pas de silence détecté | Pas de silence détecté ; pas de nouvelle remise en mémoire tampon |
| APK finale `hls.3`, réseau domestique, Fade To Black | Pas de silence détecté | Pas de silence détecté | Pas de silence détecté ; pas de nouvelle remise en mémoire tampon |

Le cycle musical de l'APK finale produit un signal moyen de -17,2 dB et ne
présente aucun silence d'au moins 100 ms. Un premier contrôle final sur Big
Buck Bunny contenait des silences du contenu sans transition de mise en mémoire
tampon ; il n'est pas utilisé pour conclure à l'absence de coupures.

Les deux images relevées après le réveil de la candidate corrigée montrent des
images distinctes de Big Buck Bunny et une position qui progresse. Ce contrôle
confirme la reprise de l'image ; il ne mesure pas l'absence de tout écran noir
pendant la reconstruction de la surface d'une activité en rotation.

Le correctif évite le rechargement pour une visibilité inchangée, conserve la
source HLS lorsque la vidéo est masquée, et maintient sa piste vidéo sélectionnée
pour éviter la réinitialisation de la chronologie audio au réveil. Les paramètres
de piste sont réévalués lors du changement de média, notamment dans une file.
Les chemins audio seul et les sources non HLS gardent leur sélection habituelle.

Conserver la piste HLS active implique du décodage vidéo en arrière-plan. Le HLS
continuait déjà à télécharger des segments vidéo avec la piste désactivée.
La consommation de batterie sur une longue écoute n'est pas qualifiée.

## Ouverture directe en arrière-plan

Une ouverture à froid vers le lecteur audio a reproduit une
`ForegroundServiceStartNotAllowedException` sur Android 14 : le service
d'extraction s'arrêtait avant le démarrage différé du lecteur. L'exception
est distincte du HTTP 403.

Le thread de travail de l'IntentService attend maintenant que le traitement
du résultat sur le thread principal ait terminé le transfert au lecteur.
Le contrôle suivant sur le même lien démarre bien le lecteur avant la
destruction du service d'extraction, puis lit le flux audio direct.
Le thread principal n'attend pas l'extraction.

## HTTP 403

Le point d'accès mobile a d'abord affiché un avertissement Android sur l'accès
Internet. Les essais mobiles qualifiés commencent seulement après confirmation
du réseau connecté et accès effectif aux segments YouTube.

Des sélections du point d'accès ont aussi échoué avec le code Android
`ASSOCIATION_REJECTION`, avant toute requête YouTube. Une sélection suivante
a établi la connexion sans modification du mot de passe ou de la sécurité.
La raison interne du refus par le point d'accès n'est pas donnée par le journal.
Les tentatives restées sur le réseau domestique ne sont pas comptées comme
des essais mobiles.

Pendant les fenêtres capturées, les segments vidéo HLS et le flux audio direct
VISIONOS / itag 140 ont répondu en HTTP 200. Le contrôle audio direct inclut
un enregistrement positif de 40 secondes après démarrage, puis plusieurs
minutes de requêtes sans 403. Le passage du point d'accès au réseau domestique
conserve aussi des réponses 200 pour le flux extrait sur le réseau mobile.
Dans l'autre sens, l'APK finale lit sur le point d'accès le flux HLS extrait
à domicile : le contrôle audio interne de 40 secondes est positif (moyenne
-11,6 dB) et les segments suivants répondent en HTTP 200. Le compteur du
processus de l'APK finale totalise 209 réponses HTTP 200 et aucun 403 sur sa
fenêtre cumulée, qui comprend les deux réseaux ; ce compteur n'est pas une
mesure de toutes les requêtes d'extraction.

**Le 403 signalé sur le téléphone en 4G n'a pas été reproduit.** Ces résultats
ne démontrent ni sa correction durable, ni une cause liée au cache, à l'IP,
à l'opérateur ou au poToken. Aucun changement de client YouTube, proxy, VPN,
jeton fixe ou nouvelle tentative automatique sur erreur n'a été ajouté.

## Vérifications locales

La compilation et Checkstyle passent. Les 32 tests ciblés passent : visibilité
du lecteur, maintien des playlists, file de lecture existante et helper HLS.
Les APK sont vérifiés avec apksigner et signés avec la clé de test compatible
avec `hls.2`. L'APK distribué est debuggable et expérimental.

APK final : `NewPipe-HLS-v0.29.1-hls.3.apk`, code Android 1017.
SHA-256 : `fe3ec7273227cdf97f4f01cda7fc5b4b8d164fea4877fcb66ad4f4a4f065aefa`.
Après les essais, la lecture est arrêtée et le réseau domestique, la rotation
automatique et le volume média initial sont rétablis.

Une contribution LLM ciblée a été obtenue via une route explicite, en un seul
appel borné, sans nouvelle tentative ni bascule. Seuls des éléments publics
de code ont été transmis. Ses suggestions ont été relues localement ; les
attributions non étayées concernant signatures, IP ou jetons ont été écartées.
Cette contribution ne constitue pas un audit indépendant.
