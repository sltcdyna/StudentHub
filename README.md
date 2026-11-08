# 🎓 StudentHub

**StudentHub** est une application Android moderne, 100 % locale et utilisable hors ligne, conçue spécialement pour simplifier la vie quotidienne des étudiants. Elle regroupe la gestion du budget, le suivi des études, la planification des repas et la gestion des courses au sein d'une interface en français, douce, rassurante et conforme aux principes Material 3.

---

## 🌟 Philosophie & Engagements

- **100 % Local & Confidentialité Absolue** : Aucun backend, aucun compte utilisateur, aucune collecte de données externes.
- **Fonctionnement Hors-Ligne** : L'ensemble des fonctionnalités (budget, tâches, recettes, courses) est fonctionnel sans connexion internet.
- **Rigueur Financière** : Tous les montants d'argent sont gérés en centimes avec le type `Long` pour éviter tout problème de précision ou d'arrondi flottant.
- **Interface Français & Pastel** : Design Material 3 soigné avec cartes arrondies et couleurs douces adaptées à un usage quotidien.

---

## 🚀 Fonctionnalités Principales

### 🏠 1. Tableau de Bord (Accueil)
- Synthèse globale du solde disponible mensuel.
- Affichage du budget hebdomadaire restant.
- Liste des devoirs et projets urgents.
- Idees de repas du jour adaptées.
- Recommandations intelligentes et alertes de budget (dépassement de 80 % d'un plafond de catégorie).

### 💰 2. Gestion du Budget (Finances)
- **Dépenses & Revenus** : Saisie et suivi chronologique.
- **Catégories de dépenses** : `Logement`, `Transport`, `Alimentation`, `Loisirs`, `Autres`.
- **Sources de revenus** : `Salaire`, `APL / Aides`, `Autres revenus`.
- **Calculs automatiques** :
  - *Solde disponible* = Revenus du mois - Dépenses du mois.
  - *Budget hebdomadaire* = Solde disponible / Semaines restantes dans le mois.
- Visualisation de la répartition par catégorie et indicateurs visuels d'alerte.

### 📚 3. Études & Devoirs
- Suivi des devoirs, travaux pratiques et examens par matière.
- Gestion des états (à faire, en cours, terminé).
- **Rappels Automatiques** : Planification de rappels locaux la veille de l'échéance via **WorkManager**.
- Gestion transparente du refus des permissions de notification avec message d'information (« rappels désactivés »).

### 🍳 4. Planification des Repas
- Catalogue pré-chargé de recettes économiques et adaptées aux étudiants (`recipes_catalog.json`).
- Calcul du coût par portion (*Prix total des ingrédients / Nombre de portions*).
- Filtrage selon le budget et le temps de préparation.
- Ajout direct des ingrédients manquants dans la liste de courses.

### 🛒 5. Liste de Courses & Scanner
- Gestion dynamique de la liste d'achats avec estimation du panier (*Somme des prix estimatifs × quantités*).
- **Scanner de Codes-Barres** : Intégration de **CameraX** et **ML Kit** pour scanner les produits.
- **Recherche de Produit** : Intégration de l'API **Open Food Facts** pour l'autocomplétion des noms de produits (mode dégradé gracieux sans réseau).
- **Finalisation des Courses** : La validation des courses enregistre automatiquement le montant total en dépense sous la catégorie `Alimentation`.

### 👤 6. Profil & Préférences
- Configuration du profil étudiant (nom, prénom, statut, budget mensuel cible).
- Personnalisation des plafonds de dépenses par catégorie.
- Stockage réactif via **DataStore Preferences**.

---

## 🛠️ Stack Technique & Architecture

L'application respecte l'architecture recommandée par Android (UI -> ViewModel -> Repository -> Data Sources).

- **Langage** : [Kotlin](https://kotlinlang.org/)
- **UI Framework** : [Jetpack Compose](https://developer.android.com/jetpack/compose) avec **Material 3**
- **Navigation** : Navigation Compose
- **Architecture** : MVVM (Model-View-ViewModel) avec `StateFlow` et états d'UI explicites (`UiState` : Chargement, Succès, Vide, Erreur)
- **Base de données** : [Room Database](https://developer.android.com/training/data-storage/room) pour la persistance locale
- **Préférences** : [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences)
- **Injection de Dépendances** : [Hilt](https://dagger.dev/hilt/) & Container d'injection
- **Tâches en arrière-plan** : [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Scanner & Vision** : [CameraX](https://developer.android.com/training/camerax) + [Google ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
- **API Extérieure** : Open Food Facts API (HTTP Client Ktor/OkHttp)

---

## 📁 Structure du Projet

```text
app/src/main/java/com/dyna/studenthub/
├── data/
│   ├── local/            # Entités Room, DAOs, Database, Converters et Repositories locaux
│   ├── OpenFoodFactsRepository.kt  # Client API Open Food Facts
│   └── RecipeCatalogLoader.kt      # Chargement du catalogue JSON de recettes
├── di/                   # Modules Hilt & AppContainer (Injection de dépendances)
├── domain/               # Logique métier pure & Règles de calcul (Money, Home, Meal, Task, Grocery, etc.)
├── navigation/           # Bar de navigation & NavHost Jetpack Compose
├── notification/         # Workers WorkManager pour les rappels de devoirs
├── ui/
│   ├── screen/           # Composables pour chaque écran (Home, Money, Studies, Meals, Courses, Profile)
│   └── theme/            # Thème Material 3 (Couleurs pastel, Typographie, Shapes)
└── viewmodel/            # ViewModels gérant le StateFlow et le flux de données pour chaque écran
```

---

## 🧪 Tests Unitaires & Validation

Toute la logique de calcul domaine fait l'objet de tests unitaires rigoureux situés sous `app/src/test/` :

- `MoneyCalculationsTest.kt` : Calcul du solde disponible et du budget hebdo.
- `GroceryCalculationsTest.kt` : Calcul des totaux de paniers et conversion en dépenses.
- `MealCalculationsTest.kt` : Calcul des coûts par portion.
- `HomeCalculationsTest.kt` : Recommandations, alertes et agrégation des données d'accueil.
- `TaskCalculationsTest.kt` : Filtres et échéances des devoirs.
- `RecommendationEngineTest.kt` : Moteur de suggestions adaptées au profil.
- `OpenFoodFactsParserTest.kt` : Traitement des données d'API et cas hors ligne.
- `ProfileValidationTest.kt` : Validation des saisies et des plafonds.

---

## ⚙️ Compilation & Exécution

### Prérequis
- Android Studio Ladybug (ou version plus récente)
- JDK 17
- SDK Android 34 / 35

### Commandes Gradle

Depuis la racine du projet :

- **Compiler l'application en mode Debug** :
  ```bash
  ./gradlew assembleDebug
  ```

- **Lancer tous les tests unitaires** :
  ```bash
  ./gradlew test
  ```

- **Vérifier les tests d'instrumentation (DAOs / Room)** :
  ```bash
  ./gradlew connectedCheck
  ```

---

## 📜 Licence & Crédits

Projet développé avec passion pour la communauté étudiante. 100 % open-source et respectueux de la vie privée.
