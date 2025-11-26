# Documentation des Relations et Contraintes XSD

## Vue d'ensemble
Les schémas XSD ont été mis à jour pour refléter correctement les relations, cardinalités et contraintes d'intégrité référentielle du diagramme de classe.

## 🔑 Clés Primaires (xs:key)

### Utilisateurs
- **utilisateurKey**: Clé pour tous les types d'utilisateurs
- **citoyenKey**: Clé spécifique pour les citoyens
- **technicienKey**: Clé spécifique pour les techniciens  
- **chefServiceKey**: Clé spécifique pour les chefs de service
- **administrateurKey**: Clé spécifique pour les administrateurs

### Entités Métier
- **demandeKey**: Demandes des citoyens (id)
- **interventionKey**: Interventions planifiées (id)
- **equipementKey**: Équipements (id)
- **ressourceMaterielleKey**: Ressources matérielles (id)
- **mainDOeuvreKey**: Main d'œuvre/ouvriers (id)
- **fournisseurKey**: Fournisseurs (id)
- **photoKey**: Photos (id_photo)

### Workflow
- **notificationKey**: Notifications (id_notification)
- **demandeAjoutKey**: Demandes d'ajout matériel/équipement (id)

## 🔗 Relations et Cardinalités

### 1. Citoyen ↔ Demande
**Cardinalité**: 1 Citoyen → * Demandes
- **Fichier**: `demandes.xsd`
- **Attribut**: `citoyenId` (obligatoire)
- **Keyref**: `demandeCitoyenRef`
- Un citoyen peut soumettre plusieurs demandes

### 2. Demande ↔ Intervention  
**Cardinalité**: 1 Demande → 0..1 Intervention
- **Fichier**: `interventions.xsd`
- **Attribut**: `demandeId` (obligatoire)
- **Keyref**: `interventionDemandeRef`
- Une demande peut générer au maximum une intervention

### 3. Technicien ↔ Intervention
**Cardinalité**: 1 Technicien → * Interventions
- **Fichier**: `interventions.xsd`
- **Attribut**: `technicienId` (optionnel)
- **Keyref**: `interventionTechnicienRef`
- Un technicien peut être assigné à plusieurs interventions

### 4. Chef de Service ↔ Intervention
**Cardinalité**: 1 Chef → * Interventions
- **Fichier**: `interventions.xsd`
- **Attribut**: `chefServiceId` (optionnel)
- **Keyref**: `interventionChefServiceRef`
- Un chef de service supervise plusieurs interventions

### 5. Intervention ↔ Équipement
**Cardinalité**: * Interventions ↔ * Équipements (many-to-many)
- **Fichier**: `interventions.xsd`
- **Structure**: `<equipementIds><equipementId>*</equipementId></equipementIds>`
- **Keyref**: `interventionEquipementRef`
- Une intervention peut utiliser plusieurs équipements
- Un équipement peut être utilisé dans plusieurs interventions

### 6. Intervention ↔ Ressource Matérielle
**Cardinalité**: * Interventions ↔ * Ressources (many-to-many)
- **Fichier**: `interventions.xsd`
- **Structure**: `<ressourceIds><ressourceId>*</ressourceId></ressourceIds>`
- **Keyref**: `interventionRessourceRef`
- Une intervention peut nécessiter plusieurs ressources matérielles

### 7. Intervention ↔ Main d'Œuvre
**Cardinalité**: * Interventions ↔ * Ouvriers (many-to-many)
- **Fichier**: `interventions.xsd`
- **Structure**: `<ouvrierIds><ouvrierId>*</ouvrierId></ouvrierIds>`
- **Keyref**: `interventionOuvrierRef`
- Une intervention peut mobiliser plusieurs ouvriers

### 8. Fournisseur ↔ Équipement
**Cardinalité**: 1 Fournisseur → * Équipements
- **Fichier**: `equipements.xsd`
- **Attribut**: `fournisseurId` (obligatoire)
- **Keyref**: `equipementFournisseurRef`
- Un fournisseur peut fournir plusieurs équipements

### 9. Fournisseur ↔ Ressource Matérielle
**Cardinalité**: 1 Fournisseur → * Ressources
- **Fichier**: `ressources.xsd`
- **Attribut**: `fournisseurId` (obligatoire)
- **Keyref**: `ressourceFournisseurRef`
- Un fournisseur peut fournir plusieurs types de ressources

### 10. Demande ↔ Photo
**Cardinalité**: 1 Demande → 1..* Photos
- **Fichier**: `demandes.xsd`, `photos.xsd`
- **Structure**: `<photoIds><photoId>+</photoId></photoIds>`
- **Keyref**: `photoDemandeRef`
- Une demande doit avoir au moins une photo

### 11. Utilisateur ↔ Notification
**Cardinalité**: 1 Utilisateur → * Notifications
- **Fichier**: `notifications.xsd`
- **Attribut**: `user_id` (obligatoire)
- **Keyref**: `notificationUtilisateurRef`
- Un utilisateur peut recevoir plusieurs notifications

### 12. Chef de Service ↔ Demande d'Ajout
**Cardinalité**: 1 Chef → * Demandes d'Ajout (créateur)
- **Fichier**: `demandesAjout.xsd`
- **Attribut**: `chefId` (obligatoire)
- **Keyref**: `demandeAjoutChefRef`
- Un chef peut créer plusieurs demandes d'ajout de matériel

### 13. Administrateur ↔ Demande d'Ajout
**Cardinalité**: 1 Admin → * Demandes d'Ajout (traitement)
- **Fichier**: `demandesAjout.xsd`
- **Attribut**: `adminId` (optionnel)
- **Keyref**: `demandeAjoutAdminRef`
- Un admin traite plusieurs demandes d'ajout

## 📋 Utilisation de complexContent

### Extension (Héritage)
Les types d'utilisateurs utilisent `xs:extension` pour hériter de `UtilisateurType`:

```xml
<xs:complexType name="CitoyenType">
  <xs:complexContent>
    <xs:extension base="tns:UtilisateurType">
      <xs:sequence>
        <xs:element name="adresse" type="xs:string"/>
        <xs:element name="telephone" type="tns:TelephoneType"/>
      </xs:sequence>
    </xs:extension>
  </xs:complexContent>
</xs:complexType>
```

**Types concernés**:
- `CitoyenType` extends `UtilisateurType` (+ adresse, telephone)
- `TechnicienType` extends `UtilisateurType` (+ competences, disponibilite)
- `ChefDeServiceType` extends `UtilisateurType` (+ departement)
- `AdministrateurType` extends `UtilisateurType` (aucun attribut supplémentaire)

### Restriction
La restriction peut être utilisée pour des types plus contraints (exemple futur):

```xml
<xs:complexType name="TypeRestreint">
  <xs:complexContent>
    <xs:restriction base="tns:TypeBase">
      <!-- Contraintes plus strictes -->
    </xs:restriction>
  </xs:complexContent>
</xs:complexType>
```

## 📁 Nouveaux Fichiers

### photos.xsd
Nouveau schéma créé pour gérer les photos associées aux demandes:
- `id_photo` (int): Identifiant unique
- `url` (string): URL de la photo
- `nom` (string): Nom du fichier
- `demandeId` (int, optionnel): Référence à la demande

## ✅ Validations d'Intégrité

Grâce aux `xs:key` et `xs:keyref`, le schéma garantit:

1. **Unicité des identifiants** (clés primaires)
2. **Intégrité référentielle** (clés étrangères valides)
3. **Cardinalités respectées** (minOccurs/maxOccurs)
4. **Relations many-to-many** (via collections d'IDs)

## 🔍 Exemple de Validation

```xml
<!-- Citoyen avec id=1 -->
<Citoyen>
  <id>1</id>
  <nom>Dupont</nom>
  <email>dupont@mail.com</email>
  ...
</Citoyen>

<!-- Demande référençant le citoyen -->
<Demande>
  <id>100</id>
  <citoyenId>1</citoyenId> <!-- VALID: référence le citoyen ci-dessus -->
  ...
</Demande>

<!-- INVALIDE si citoyenId=999 n'existe pas -->
<Demande>
  <id>101</id>
  <citoyenId>999</citoyenId> <!-- ERREUR: violation de contrainte keyref -->
  ...
</Demande>
```

## 📊 Résumé des Modifications

| Fichier | Modifications Principales |
|---------|---------------------------|
| `root.xsd` | + 13 xs:key, + 13 xs:keyref, + élément Photos |
| `utilisateurs.xsd` | ✓ complexContent/extension déjà présent |
| `demandes.xsd` | + citoyenId obligatoire, + photoIds collection |
| `interventions.xsd` | + demandeId, technicienId, chefServiceId, + collections many-to-many |
| `equipements.xsd` | + fournisseurId remplace objet fournisseur |
| `ressources.xsd` | + fournisseurId remplace objet fournisseur |
| `photos.xsd` | ✓ Nouveau fichier créé |
| `notifications.xsd` | + user_id, + type |
| `demandesAjout.xsd` | + chefId, adminId, + attributs complets |

## 🎯 Conformité avec le Diagramme

Toutes les relations du diagramme de classe sont maintenant implémentées:
- ✅ Associations 1-to-many
- ✅ Associations many-to-many  
- ✅ Héritage (extension)
- ✅ Contraintes d'intégrité
- ✅ Cardinalités min/max

---
**Date**: 26 novembre 2025  
**Version**: 2.0
