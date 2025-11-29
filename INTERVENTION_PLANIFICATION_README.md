# Système de Planification d'Intervention avec Validation des Ressources

## Vue d'ensemble

Ce système permet au chef de service de planifier une intervention en définissant des exigences (compétences, matériels, équipements) et de visualiser un calendrier coloré indiquant la disponibilité des ressources pour chaque date.

## Fonctionnalités

### 1. Définition des Exigences

Le chef définit dès le début de la planification :

#### **Compétences des techniciens**
- Type de compétence requise (ex: Électricien, Plombier, Maçon)
- Nombre de techniciens nécessaires pour chaque compétence

#### **Matériels requis**
- Désignation du matériel
- Quantité nécessaire
- Vérification du stock disponible

#### **Équipements requis**
- Type d'équipement (ex: Tractopelle, Camion)
- Quantité nécessaire
- Vérification de l'état (doit être "fonctionnel") et disponibilité (doit être "oui")

### 2. Validation des Dates

Le système vérifie pour chaque date de la période sélectionnée :

#### **Statut VERT** ✅
- ✅ Techniciens avec compétences requises disponibles
- ✅ Équipements fonctionnels et disponibles
- ✅ Matériels en stock suffisant

#### **Statut JAUNE** ⚠️
- ✅ Techniciens disponibles
- ✅ Équipements disponibles
- ❌ Matériels insuffisants

#### **Statut ROUGE** ❌
- Au moins une condition non remplie parmi :
  - ❌ Techniciens manquants ou compétences insuffisantes
  - ❌ Équipements non disponibles ou non fonctionnels
  - ❌ Matériels insuffisants

### 3. Calendrier Visuel

Un calendrier interactif affiche :
- Chaque date avec son code couleur (vert/jaune/rouge)
- Les icônes de statut pour chaque ressource (👨‍🔧 🚜 🔧)
- Un message explicatif de la disponibilité

## Architecture

### Backend (Java/Spring Boot)

#### Schéma XSD mis à jour

**`interventions.xsd`** a été enrichi avec :
```xml
<xs:complexType name="CompetenceRequiseType">
  <xs:element name="competence" type="xs:string"/>
  <xs:element name="nombreTechniciens" type="xs:int"/>
</xs:complexType>

<xs:complexType name="MaterielRequisType">
  <xs:element name="designation" type="xs:string"/>
  <xs:element name="quantiteRequise" type="xs:int"/>
</xs:complexType>

<xs:complexType name="EquipementRequisType">
  <xs:element name="type" type="xs:string"/>
  <xs:element name="quantiteRequise" type="xs:int"/>
</xs:complexType>
```

#### Entités et DTOs

**Nouveaux DTOs créés :**
- `CompetenceRequise.java` - Compétence et nombre de techniciens
- `MaterielRequis.java` - Matériel et quantité
- `EquipementRequis.java` - Type d'équipement et quantité
- `DateValidationRequest.java` - Requête de validation
- `DateValidationResult.java` - Résultat avec statut coloré

**Entité mise à jour :**
- `Intervention.java` - Ajout des listes d'exigences

#### Services

**`InterventionValidationService.java`**
Service principal qui :
1. Valide les disponibilités pour une plage de dates
2. Vérifie les techniciens avec compétences requises
3. Vérifie les équipements (état fonctionnel + disponibilité)
4. Vérifie les matériels en stock
5. Détermine le statut (VERT/JAUNE/ROUGE) pour chaque date

**Méthodes principales :**
```java
// Valide une liste de dates
List<DateValidationResult> validateDates(DateValidationRequest request)

// Valide une date spécifique
DateValidationResult validateDate(LocalDate date, ...)

// Vérifie techniciens avec compétences
boolean verifyTechniciens(CompetenceRequise[] competencesRequises, LocalDate date)

// Vérifie équipements disponibles et fonctionnels
boolean verifyEquipements(EquipementRequis[] equipementsRequis, LocalDate date)

// Vérifie matériels en stock
boolean verifyMateriels(MaterielRequis[] materielsRequis)
```

#### API REST

**Nouvel endpoint :**
```
POST /api/interventions/valider-dates
Content-Type: application/json

Body: {
  "dateDebut": "2025-12-01",
  "dateFin": "2025-12-31",
  "competencesRequises": [
    { "competence": "Électricien", "nombreTechniciens": 2 }
  ],
  "materielsRequis": [
    { "designation": "Câble électrique", "quantiteRequise": 100 }
  ],
  "equipementsRequis": [
    { "type": "Camion", "quantiteRequise": 1 }
  ]
}

Response: [
  {
    "date": "2025-12-01",
    "status": "VERT",
    "message": "Toutes les ressources sont disponibles",
    "techniciensDisponibles": true,
    "equipementsDisponibles": true,
    "materielsDisponibles": true
  },
  ...
]
```

### Frontend (Angular)

#### Nouveau Composant

**`intervention-planification.component.ts`**

**Étapes de planification :**

1. **Étape Exigences** - Définir les besoins
   - Ajouter/supprimer des compétences
   - Ajouter/supprimer des matériels
   - Ajouter/supprimer des équipements
   - Définir la période de recherche

2. **Étape Calendrier** - Visualiser les disponibilités
   - Statistiques (nombre de dates vertes/jaunes/rouges)
   - Légende des couleurs
   - Grille de dates interactives avec code couleur
   - Détails de chaque date au clic

3. **Étape Confirmation** - Valider la planification
   - Récapitulatif de la date choisie
   - Récapitulatif des exigences
   - Confirmation finale

#### Modèles TypeScript

**`intervention-validation.model.ts`**
```typescript
interface CompetenceRequise {
  competence: string;
  nombreTechniciens: number;
}

interface MaterielRequis {
  designation: string;
  quantiteRequise: number;
}

interface EquipementRequis {
  type: string;
  quantiteRequise: number;
}

interface DateValidationResult {
  date: string;
  status: 'VERT' | 'JAUNE' | 'ROUGE';
  message: string;
  techniciensDisponibles: boolean;
  equipementsDisponibles: boolean;
  materielsDisponibles: boolean;
}
```

#### Service mis à jour

**`intervention.service.ts`**
```typescript
validateDates(request: DateValidationRequest): Observable<DateValidationResult[]>
```

## Utilisation

### Scénario d'utilisation typique

1. **Le citoyen crée une demande** d'intervention

2. **Le chef de service planifie l'intervention :**
   - Ouvre la page de planification
   - Définit les compétences requises (ex: 2 électriciens)
   - Définit les matériels requis (ex: 50m de câble)
   - Définit les équipements requis (ex: 1 camion grue)
   - Sélectionne une période (ex: du 1er au 31 décembre)
   - Clique sur "Rechercher les dates disponibles"

3. **Le système valide et affiche le calendrier :**
   - Dates en VERT : Toutes les conditions remplies ✅
   - Dates en JAUNE : Tech + équipements OK, mais matériel insuffisant ⚠️
   - Dates en ROUGE : Ressources manquantes ❌

4. **Le chef sélectionne une date verte ou jaune :**
   - Voit le récapitulatif
   - Confirme la planification

5. **L'intervention est créée avec :**
   - `dateDebut` : Date fixée dès le début
   - `dateFin` : Sera fixée quand l'intervention sera terminée
   - Exigences définies et stockées

## Logique de Validation

### Vérification des Techniciens

```java
Pour chaque compétence requise:
  - Chercher tous les techniciens ayant cette compétence
  - Vérifier leur disponibilité à la date donnée
  - Compter le nombre de techniciens disponibles
  - Si nombre < nombreRequis → ÉCHEC
```

### Vérification des Équipements

```java
Pour chaque type d'équipement requis:
  - Chercher tous les équipements de ce type
  - Filtrer ceux qui sont:
    * État = "fonctionnel"
    * Disponible = true
    * Pas dans une période d'indisponibilité à cette date
  - Compter les équipements valides
  - Si nombre < quantiteRequise → ÉCHEC
```

### Vérification des Matériels

```java
Pour chaque matériel requis:
  - Chercher le matériel par désignation
  - Vérifier quantiteEnStock >= quantiteRequise
  - Si insuffisant → ÉCHEC
```

### Détermination du Statut Final

```java
Si (techniciens OK && équipements OK && matériels OK):
  → STATUT = VERT ✅

Sinon si (techniciens OK && équipements OK && !matériels OK):
  → STATUT = JAUNE ⚠️

Sinon:
  → STATUT = ROUGE ❌
```

## Fichiers Modifiés/Créés

### Backend

**XSD :**
- ✅ `src/main/resources/schemas/entities/interventions.xsd` - Ajout des types d'exigences

**DTOs :**
- ✅ `src/main/java/tn/SGII_Ville/dto/CompetenceRequise.java`
- ✅ `src/main/java/tn/SGII_Ville/dto/MaterielRequis.java`
- ✅ `src/main/java/tn/SGII_Ville/dto/EquipementRequis.java`
- ✅ `src/main/java/tn/SGII_Ville/dto/DateValidationRequest.java`
- ✅ `src/main/java/tn/SGII_Ville/dto/DateValidationResult.java`

**Entités :**
- ✅ `src/main/java/tn/SGII_Ville/entities/Intervention.java` - Ajout des exigences

**Services :**
- ✅ `src/main/java/tn/SGII_Ville/service/InterventionValidationService.java`

**Repositories :**
- ✅ `src/main/java/tn/SGII_Ville/repository/RessourceMaterielleRepository.java`

**Controllers :**
- ✅ `src/main/java/tn/SGII_Ville/controller/InterventionController.java` - Nouvel endpoint

### Frontend

**Modèles :**
- ✅ `src/app/models/intervention-validation.model.ts`

**Services :**
- ✅ `src/app/services/intervention.service.ts` - Méthode validateDates

**Composants :**
- ✅ `src/app/components/intervention-planification/intervention-planification.component.ts`
- ✅ `src/app/components/intervention-planification/intervention-planification.component.html`
- ✅ `src/app/components/intervention-planification/intervention-planification.component.css`

## Tests

### Test Backend

```bash
cd SGII-Ville
./mvnw spring-boot:run
```

**Test avec curl :**
```bash
curl -X POST http://localhost:8080/api/interventions/valider-dates \
  -H "Content-Type: application/json" \
  -d '{
    "dateDebut": "2025-12-01",
    "dateFin": "2025-12-10",
    "competencesRequises": [
      {"competence": "Électricien", "nombreTechniciens": 1}
    ],
    "materielsRequis": [
      {"designation": "Câble", "quantiteRequise": 10}
    ],
    "equipementsRequis": [
      {"type": "Camion", "quantiteRequise": 1}
    ]
  }'
```

### Test Frontend

```bash
cd SGIIVILLE-FE
npm install
npm start
```

Naviguer vers : `http://localhost:4200`

## Améliorations Futures

1. **Gestion des périodes d'indisponibilité** des équipements
2. **Réservation automatique** des ressources à la confirmation
3. **Suggestions intelligentes** de dates optimales
4. **Notifications** aux techniciens concernés
5. **Calendrier partagé** entre chefs de service
6. **Export PDF** du planning validé
7. **Historique** des planifications

## Notes Importantes

- Les dates en JAUNE peuvent être sélectionnées (le chef assume le manque de matériel)
- Les dates en ROUGE ne peuvent pas être sélectionnées
- La validation est en temps réel sur les données actuelles
- Les équipements doivent être en état "fonctionnel" pour être comptabilisés
- Les matériels vérifient uniquement le stock, pas les réservations futures

## Support

Pour toute question ou problème, contactez l'équipe de développement.
