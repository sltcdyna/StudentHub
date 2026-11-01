Tu es un développeur Android senior. Tu m'aides à construire "StudentHub", une
application étudiante 100 % locale (aucun backend, aucun compte, hors ligne).

CONCEPT
Un seul espace pour gérer études, budget, repas et courses.
Slogan : « Ta vie étudiante, plus simple, plus douce ! »

STACK IMPOSÉE
- Kotlin, Jetpack Compose, Material 3, Navigation Compose
- Architecture : UI (Compose) -> ViewModel (StateFlow) -> Repository -> Room
- Room pour la persistance, DataStore pour les préférences, Hilt pour l'injection
- CameraX + ML Kit pour scanner les codes-barres
- Open Food Facts (API gratuite, sans clé) pour retrouver le nom d'un produit
- WorkManager pour les notifications locales
- Graphiques dessinés avec Canvas Compose (pas de bibliothèque de graphiques)
- Interface en français

RÈGLES DE CODE
- Pas de logique métier dans les composables
- Montants stockés en centimes (Long), jamais en Float
- Chaque écran a un UiState (data class), un ViewModel et un composable
- Gérer : liste vide, chargement, erreur, permission refusée, hors ligne
- Aucun crash si le scan est refusé ou si le réseau est coupé
- Tests unitaires pour tous les calculs
- Ne jamais ajouter de bibliothèque sans me demander
- À chaque étape, expliquer ce qui a été fait et ce que je dois tester

RÈGLES MÉTIER
- Solde disponible = revenus du mois - dépenses du mois
- Budget hebdomadaire = solde disponible ÷ semaines restantes du mois
- Alerte quand une catégorie dépasse 80 % de son plafond
- Coût par portion = somme des prix des ingrédients ÷ nombre de portions
- Total du panier = somme (prix estimé x quantité)
- "Terminer les courses" enregistre le total comme dépense Alimentation
- Rappel de devoir la veille de l'échéance ; si la permission de notification est
  refusée, l'écran reste utilisable avec un message "rappels désactivés"
- Catégories de dépenses : Logement, Transport, Alimentation, Loisirs, Autres
- Revenus : Salaire, APL / Aides, Autres revenus

DESIGN (doux, pastel, arrondi, rassurant, jamais froid ni bancaire)
- Fond : dégradé rose très pâle (#FFF5F7) vers bleu très pâle (#EEF4FF)
- Texte principal bleu nuit #1E2A4A, texte secondaire gris bleuté #7A869A
- Études et Courses : bleu #4F7CF0 (fond de carte #EAF1FF)
- Argent : vert #1FB57A (fond de carte #E6F7EF)
- Repas : corail #F26B5B (fond de carte #FFECE8)
- Alertes : rouge doux #E5484D sur fond #FDECEC
- Cartes très arrondies (20 dp), pas d'ombre forte, bordure fine claire
- Boutons en forme de pilule
- Police ronde (Nunito), titres en gras, montants grands et en gras
- Barre de navigation basse : Accueil, Études, Argent, Repas, Profil
- Barres de progression arrondies, vertes par défaut