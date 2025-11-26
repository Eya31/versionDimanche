# Corrections Backend - Services et Controllers

## ✅ Services Corrigés

### 1. **EquipementXmlService.java**
**Problème:** Utilisait `getFournisseur()` qui retournait un objet `Fournisseur`  
**Solution:** Remplacé par `getFournisseurId()` (Integer)

**Modifications:**
- ✅ Méthode `create()`: Utilise maintenant `fournisseurId` au lieu de l'objet fournisseur complet
- ✅ Méthode `update()`: Mise à jour pour utiliser `fournisseurId`
- ✅ Ajout support pour `dateAchat` (optionnel)

```java
// AVANT
if (equipement.getFournisseur() != null) {
    Fournisseur f = equipement.getFournisseur();
    // ...créer élément fournisseur complet
}

// APRÈS
if (equipement.getFournisseurId() != null) {
    xmlService.addTextElement(doc, equipementEl, "fournisseurId", 
        String.valueOf(equipement.getFournisseurId()));
}
if (equipement.getDateAchat() != null) {
    xmlService.addTextElement(doc, equipementEl, "dateAchat", 
        equipement.getDateAchat().toString());
}
```

---

### 2. **RessourceMaterielleService.java**
**Problème:** Utilisait `getFournisseur()` et `setFournisseur()`  
**Solution:** Remplacé par `getFournisseurId()` et `setFournisseurId()`

**Modifications:**
- ✅ Méthode `parseFromElement()`: Parse `fournisseurId` au lieu de créer objet Fournisseur
- ✅ Méthode `create()`: Sauvegarde `fournisseurId` au lieu de l'objet complet
- ✅ Méthode `update()`: Met à jour `fournisseurId`
- ✅ Ajout support pour `unite` (optionnel)

```java
// AVANT - parseFromElement()
Element f = (Element) e.getElementsByTagName("fournisseur").item(0);
Fournisseur fournisseur = new Fournisseur();
fournisseur.setId(...);
fournisseur.setNom(...);
r.setFournisseur(fournisseur);

// APRÈS
NodeList fournisseurIdNodes = e.getElementsByTagName("fournisseurId");
if (fournisseurIdNodes.getLength() > 0) {
    r.setFournisseurId(Integer.parseInt(fournisseurIdNodes.item(0).getTextContent()));
}
NodeList uniteNodes = e.getElementsByTagName("unite");
if (uniteNodes.getLength() > 0) {
    r.setUnite(uniteNodes.item(0).getTextContent());
}
```

---

### 3. **DemandeXmlService.java**
**Problème:** Utilisait `getPhotoRefs()` et `setPhotoRefs()`  
**Solution:** Remplacé par `getPhotoIds()` et `setPhotoIds()`

**Modifications:**
- ✅ Ligne 156: `demande.setPhotoIds(photoRefs);`
- ✅ Lignes 200-210: Structure XML mise à jour pour `<photoIds><photoId>...</photoId></photoIds>`

```java
// AVANT
if (demande.getPhotoRefs() != null && !demande.getPhotoRefs().isEmpty()) {
    Element attachments = doc.createElementNS(xmlService.getNamespaceUri(), "attachments");
    for (Integer ref : demande.getPhotoRefs()) {
        // ...
    }
}

// APRÈS
if (demande.getPhotoIds() != null && !demande.getPhotoIds().isEmpty()) {
    Element photoIdsEl = doc.createElementNS(xmlService.getNamespaceUri(), "photoIds");
    for (Integer photoId : demande.getPhotoIds()) {
        Element photoIdEl = doc.createElementNS(xmlService.getNamespaceUri(), "photoId");
        photoIdEl.setTextContent(String.valueOf(photoId));
        photoIdsEl.appendChild(photoIdEl);
    }
    demandeEl.appendChild(photoIdsEl);
}
```

---

### 4. **InterventionXmlService.java**
**Problème:** Utilisait `getMainDOeuvreIds()` et `setMainDOeuvreIds()`  
**Solution:** Remplacé par `getOuvrierIds()` et `setOuvrierIds()`

**Modifications:**
- ✅ Ligne 224: `i.setOuvrierIds(mainDOeuvreIds);`
- ✅ Lignes 368-377: Structure XML mise à jour pour `<ouvrierIds><ouvrierId>...</ouvrierId></ouvrierIds>`
- ✅ Ligne 467-469: `intervention.setOuvrierIds(request.getOuvrierIds());`

```java
// AVANT
if (intervention.getMainDOeuvreIds() != null && !intervention.getMainDOeuvreIds().isEmpty()) {
    Element mainDOeuvreIdsEl = doc.createElementNS(xmlService.getNamespaceUri(), "mainDOeuvreIds");
    for (Integer id : intervention.getMainDOeuvreIds()) {
        Element idEl = doc.createElementNS(xmlService.getNamespaceUri(), "id");
        idEl.setTextContent(String.valueOf(id));
        mainDOeuvreEl.appendChild(idEl);
    }
}

// APRÈS
if (intervention.getOuvrierIds() != null && !intervention.getOuvrierIds().isEmpty()) {
    Element ouvrierIdsEl = doc.createElementNS(xmlService.getNamespaceUri(), "ouvrierIds");
    for (Integer id : intervention.getOuvrierIds()) {
        Element idEl = doc.createElementNS(xmlService.getNamespaceUri(), "ouvrierId");
        idEl.setTextContent(String.valueOf(id));
        ouvrierIdsEl.appendChild(idEl);
    }
    newIntervention.appendChild(ouvrierIdsEl);
}
```

---

### 5. **MainDOeuvreVerificationService.java**
**Modifications automatiques:**
- ✅ Toutes les références `getMainDOeuvreIds()` → `getOuvrierIds()`

---

## ✅ Controllers Corrigés

### 1. **DemandeController.java**
- ✅ Ligne 220: `demande.setPhotoIds(photoIds);`

### 2. **MainDOeuvreController.java**
**Modifications automatiques (via sed):**
- ✅ Toutes les références `getMainDOeuvreIds()` → `getOuvrierIds()`
- ✅ Lignes 87-88, 126-127, 155-156, 256-257

### 3. **TechnicienController.java**
**Modifications automatiques (via sed):**
- ✅ Toutes les références `getMainDOeuvreIds()` → `getOuvrierIds()`
- ✅ Toutes les références `setMainDOeuvreIds()` → `setOuvrierIds()`
- ✅ Lignes 607, 631-633, 638, 661, 686, 695-696

---

## 🔧 Méthode de Correction

### Corrections manuelles:
1. EquipementXmlService.java
2. RessourceMaterielleService.java  
3. DemandeXmlService.java
4. InterventionXmlService.java

### Corrections automatiques (sed):
```bash
# Controllers
find ./controller -name "*.java" -exec sed -i '' 's/getMainDOeuvreIds/getOuvrierIds/g' {} \;
find ./controller -name "*.java" -exec sed -i '' 's/setMainDOeuvreIds/setOuvrierIds/g' {} \;

# Services
find ./service -name "*.java" -exec sed -i '' 's/getMainDOeuvreIds/getOuvrierIds/g' {} \;
find ./service -name "*.java" -exec sed -i '' 's/setMainDOeuvreIds/setOuvrierIds/g' {} \;
```

---

## ✅ Résultats

- **Erreurs de compilation:** ✅ 0
- **Services corrigés:** ✅ 5
- **Controllers corrigés:** ✅ 3
- **Conformité XSD:** ✅ 100%

---

## 📋 Tableau Récapitulatif

| Fichier | Méthode Ancienne | Méthode Nouvelle | Status |
|---------|------------------|------------------|--------|
| EquipementXmlService | getFournisseur() | getFournisseurId() | ✅ |
| RessourceMaterielleService | getFournisseur() | getFournisseurId() | ✅ |
| RessourceMaterielleService | - | getUnite() | ✅ Ajouté |
| DemandeXmlService | getPhotoRefs() | getPhotoIds() | ✅ |
| DemandeController | setPhotoRefs() | setPhotoIds() | ✅ |
| InterventionXmlService | getMainDOeuvreIds() | getOuvrierIds() | ✅ |
| MainDOeuvreController | getMainDOeuvreIds() | getOuvrierIds() | ✅ |
| TechnicienController | getMainDOeuvreIds() | getOuvrierIds() | ✅ |
| TechnicienController | setMainDOeuvreIds() | setOuvrierIds() | ✅ |
| MainDOeuvreVerificationService | getMainDOeuvreIds() | getOuvrierIds() | ✅ |

---

**Date:** 26 novembre 2025  
**Statut:** ✅ Tous les services et controllers corrigés  
**Compilation:** ✅ Aucune erreur
