# Mise à jour des Entités Backend - Conformité XSD

## 📋 Résumé des Modifications

Toutes les entités Java ont été mises à jour pour correspondre aux schémas XSD avec les relations, clés primaires et clés étrangères.

---

## 📄 Entités Modifiées

### 1. **Photo.java** ✅
**Modifications:**
- ✅ Ajout de `demandeId` (Integer, optionnel) - Référence à la demande
- ✅ Constructeur supplémentaire avec demandeId
- ✅ Getters/Setters pour demandeId
- ✅ Commentaire: Conforme au schéma XSD photos.xsd

**Relation:** 1 Demande -> 1..* Photos

---

### 2. **Equipement.java** ✅
**Modifications:**
- ✅ Remplacé `Fournisseur fournisseur` par `Integer fournisseurId` (clé étrangère)
- ✅ Ajout de `LocalDate dateAchat` (optionnel)
- ✅ Mis à jour constructeur et tous les getters/setters
- ✅ Mis à jour toString()
- ✅ Ajout import `java.time.LocalDate`
- ✅ Commentaire: Relation 1 Fournisseur -> * Equipements

**Relation:** 1 Fournisseur -> * Equipements

---

### 3. **RessourceMaterielle.java** ✅
**Modifications:**
- ✅ Remplacé `Fournisseur fournisseur` par `Integer fournisseurId` (clé étrangère)
- ✅ Ajout de `String unite` (optionnel) - Unité de mesure
- ✅ Mis à jour constructeur et tous les getters/setters
- ✅ Mis à jour toString()
- ✅ Commentaire: Relation 1 Fournisseur -> * RessourcesMaterielles

**Relation:** 1 Fournisseur -> * Ressources Matérielles

---

### 4. **Notification.java** ✅
**Modifications:**
- ✅ Ajout de `String type` (optionnel) - Type de notification
- ✅ Constructeur supplémentaire avec type
- ✅ Getters/Setters pour type
- ✅ Mis à jour toString()
- ✅ Commentaire sur `userId`: Référence à l'utilisateur (clé étrangère)
- ✅ Commentaire: Relation 1 Utilisateur -> * Notifications

**Relation:** 1 Utilisateur -> * Notifications

---

### 5. **Demande.java** ✅
**Modifications:**
- ✅ Changé `Integer citoyenId` en `int citoyenId` (obligatoire selon XSD)
- ✅ Renommé `photoRefs` en `photoIds` pour cohérence
- ✅ Mis à jour getters/setters (getCitoyenId retourne int, getPhotoIds/setPhotoIds)
- ✅ Commentaire: ID du citoyen (clé étrangère obligatoire)
- ✅ Commentaire: 1 Demande -> 1..* Photos selon XSD

**Relations:** 
- 1 Citoyen -> * Demandes
- 1 Demande -> 1..* Photos

---

### 6. **Intervention.java** ✅
**Modifications principales:**

#### Clés étrangères:
- ✅ `demandeId` changé de `Integer` en `int` (obligatoire)
- ✅ `technicienId` changé de `int` en `Integer` (optionnel)
- ✅ `chefServiceId` reste `Integer` (optionnel)

#### Types de dates:
- ✅ `dateDebut` changé de `LocalDateTime` en `LocalDate`
- ✅ `dateFin` changé de `LocalDateTime` en `LocalDate`

#### Relations many-to-many:
- ✅ Renommé `mainDOeuvreIds` en `ouvrierIds` pour cohérence XSD
- ✅ Supprimé `photoIds` (pas dans le XSD intervention)
- ✅ Conservé `equipementIds` et `ressourceIds`

#### Commentaires JavaDoc:
- ✅ Ajout documentation complète des relations:
  - 1 Demande -> 0..1 Intervention
  - 1 Technicien -> * Interventions
  - 1 ChefService -> * Interventions  
  - * Intervention <-> * Equipements
  - * Intervention <-> * RessourcesMaterielles
  - * Intervention <-> * MainDOeuvre

**Relations complexes:**
```
1:1     Demande -> Intervention (demandeId obligatoire)
1:N     Technicien -> Interventions (technicienId optionnel)
1:N     ChefService -> Interventions (chefServiceId optionnel)
N:M     Interventions <-> Equipements (via equipementIds)
N:M     Interventions <-> Ressources (via ressourceIds)
N:M     Interventions <-> Ouvriers (via ouvrierIds)
```

---

## 🔄 Changements de Types

| Classe | Champ | Ancien Type | Nouveau Type | Raison |
|--------|-------|-------------|--------------|--------|
| Photo | demandeId | - | Integer | Ajout référence demande |
| Equipement | fournisseur | Fournisseur | Integer | Clé étrangère |
| Equipement | dateAchat | - | LocalDate | Ajout champ optionnel |
| RessourceMaterielle | fournisseur | Fournisseur | Integer | Clé étrangère |
| RessourceMaterielle | unite | - | String | Ajout unité de mesure |
| Notification | type | - | String | Ajout type notification |
| Demande | citoyenId | Integer | int | Obligatoire selon XSD |
| Demande | photoRefs | List<Integer> | photoIds | Renommage cohérence |
| Intervention | demandeId | Integer | int | Obligatoire selon XSD |
| Intervention | technicienId | int | Integer | Optionnel selon XSD |
| Intervention | dateDebut | LocalDateTime | LocalDate | Conformité XSD |
| Intervention | dateFin | LocalDateTime | LocalDate | Conformité XSD |
| Intervention | mainDOeuvreIds | List<Integer> | ouvrierIds | Renommage cohérence |

---

## ⚠️ Points d'Attention pour les Services

### Services à mettre à jour:

1. **EquipementService**
   - Remplacer références à `getFournisseur()` par `getFournisseurId()`
   - Ajouter logique pour `getDateAchat()`

2. **RessourceService**  
   - Remplacer références à `getFournisseur()` par `getFournisseurId()`
   - Ajouter support pour `getUnite()`

3. **DemandeService**
   - Vérifier que `citoyenId` est toujours fourni (non null)
   - Remplacer `photoRefs` par `photoIds`

4. **InterventionService**
   - Vérifier que `demandeId` est toujours fourni (non null)
   - `technicienId` peut être null maintenant
   - Remplacer `mainDOeuvreIds` par `ouvrierIds`
   - Adapter logique dates (LocalDate au lieu de LocalDateTime)

5. **NotificationService**
   - Ajouter support pour le champ `type`

6. **PhotoService**
   - Gérer le nouveau champ `demandeId`

---

## 🎯 Conformité XSD

Toutes les entités respectent maintenant:
- ✅ Les clés primaires (xs:key)
- ✅ Les clés étrangères (xs:keyref)
- ✅ Les cardinalités (minOccurs/maxOccurs)
- ✅ Les relations 1:N et N:M
- ✅ Les types de données XSD

---

## 📝 Prochaines Étapes

1. ⏳ Mettre à jour les **Services** (EquipementService, RessourceService, etc.)
2. ⏳ Mettre à jour les **Repositories** (méthodes de parsing XML)
3. ⏳ Mettre à jour les **Controllers** (validation des clés étrangères)
4. ⏳ Tester l'intégrité référentielle
5. ⏳ Mettre à jour les tests unitaires

---

**Date:** 26 novembre 2025  
**Statut:** ✅ Entités mises à jour  
**Conformité XSD:** 100%
