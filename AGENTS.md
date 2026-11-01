# StudentHub — contexte pour les assistants IA

## Objectif

StudentHub est une application Android étudiante 100 % locale et utilisable hors
ligne. Elle regroupe les études, le budget, les repas et les courses dans une
interface française douce et rassurante. Aucun compte ni backend n'est prévu.

## Stack et organisation

- Kotlin, Jetpack Compose, Material 3 et Navigation Compose.
- UI Compose -> ViewModel avec `StateFlow` -> Repository -> Room.
- Room pour les données persistantes, DataStore pour les préférences et Hilt
  pour l'injection.
- CameraX + ML Kit pour les codes-barres ; Open Food Facts pour rechercher le
  nom d'un produit.
- WorkManager pour les rappels locaux et Canvas Compose pour les graphiques.
- Le code principal est sous `app/src/main/java/com/dyna/studenthub/` :
  `ui/`, `viewmodel/`, `data/`, `domain/`, `navigation/`, `di/` et
  `notification/`. Les tests unitaires sont dans `app/src/test/`.

Chaque écran doit avoir son `UiState` (`data class`), son ViewModel et son
composable. Les composables ne contiennent pas de logique métier. Les écrans
doivent traiter explicitement le chargement, les listes vides, les erreurs, les
permissions refusées et l'absence de réseau sans provoquer de crash.

## Commandes utiles

Depuis la racine du projet :

```bash
./gradlew assembleDebug
./gradlew test
```

## Conventions de code

- Interface et messages utilisateur en français.
- Montants représentés en centimes avec `Long`, jamais avec `Float`.
- Réutiliser les repositories, helpers et conventions existants avant d'ajouter
  une nouvelle abstraction.
- Respecter les couleurs pastel, les cartes arrondies et les composants Material
  3 déjà utilisés.
- Toute règle de calcul métier doit être testée unitairement.
- Les catégories de dépenses sont `Logement`, `Transport`, `Alimentation`,
  `Loisirs` et `Autres`.
- Les revenus sont `Salaire`, `APL / Aides` et `Autres revenus`.

## Règles métier importantes

- Solde disponible = revenus du mois - dépenses du mois.
- Budget hebdomadaire = solde disponible / semaines restantes du mois.
- Une catégorie est en alerte au-delà de 80 % de son plafond.
- Coût par portion = prix total des ingrédients / nombre de portions.
- Total du panier = somme(prix estimé × quantité).
- Terminer les courses enregistre le total comme dépense `Alimentation`.
- Un devoir est rappelé la veille de son échéance ; si les notifications sont
  refusées, l'écran reste utilisable et indique « rappels désactivés ».

## À ne pas faire

- Ne pas ajouter de bibliothèque ou de service distant sans validation
  préalable.
- Ne pas introduire de backend, de compte utilisateur ou de collecte de données.
- Ne pas placer de logique métier dans les composables.
- Ne pas stocker d'argent en `Float`/`Double` ni arrondir silencieusement les
  montants.
- Ne pas ignorer une erreur, une permission refusée ou un mode hors ligne avec
  un faux état de succès.
- Ne pas versionner `local.properties`, les sorties de build, les secrets ou
  les fichiers générés.

Mettre ce document à jour lorsque l'architecture, les commandes ou les règles
du projet évoluent.
