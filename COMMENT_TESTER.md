# 🧪 Comment Tester le Nouveau Système de Planification

## ✅ Modifications Effectuées

Le **nouveau composant de planification** (`intervention-planification`) est maintenant intégré dans le dashboard du chef de service.

### Ce qui a changé :

1. **Ancien système** : Modal avec formulaire simple de planification
2. **Nouveau système** : Composant en 3 étapes avec validation des ressources et calendrier coloré

---

## 🚀 Comment Tester

### 1. Démarrer l'application

**Terminal 1 - Backend :**
```bash
cd SGII-Ville
mvn spring-boot:run
```

**Terminal 2 - Frontend :**
```bash
cd SGIIVILLE-FE
npm start
```

### 2. Se connecter

- Aller sur : `http://localhost:4200`
- Se connecter en tant que **Chef de service**
- Accéder au dashboard

### 3. Tester la planification

#### Étape 1 : Ouvrir une demande

1. Dans la section "**Demandes en attente**"
2. Cliquer sur le bouton **"📅 Planifier"** d'une demande

#### Étape 2 : Le nouveau composant s'affiche

Vous verrez maintenant le **nouveau composant** en 3 étapes :

---

## 📝 Guide de Test - Étape par Étape

### **ÉTAPE 1 : Définir les Exigences**

1. **Ajouter des compétences requises** :
   - Tapez "Électricien" dans le champ
   - Choisir "2" techniciens
   - Cliquer sur **"+ Ajouter"**
   - ✅ La compétence apparaît dans la liste

2. **Ajouter des matériels requis** :
   - Tapez "Câble électrique"
   - Quantité : "100"
   - Cliquer sur **"+ Ajouter"**
   - ✅ Le matériel apparaît dans la liste

3. **Ajouter des équipements requis** :
   - Tapez "Camion"
   - Quantité : "1"
   - Cliquer sur **"+ Ajouter"**
   - ✅ L'équipement apparaît dans la liste

4. **Sélectionner la période** :
   - Date de début : Aujourd'hui
   - Date de fin : Dans 15 jours

5. **Cliquer sur** : **"🔍 Rechercher les dates disponibles"**

---

### **ÉTAPE 2 : Voir le Calendrier Coloré**

Vous devriez voir :

1. **Statistiques en haut** :
   ```
   [10 dates]  [3 dates]  [2 dates]
   Disponibles  Partielles Indisponibles
   ```

2. **Légende des couleurs** :
   - 🟢 Vert = Tout OK
   - 🟡 Jaune = Tech + Équip OK, Matériel KO
   - 🔴 Rouge = Ressources manquantes

3. **Grille de dates** avec :
   - Chaque date affichée
   - Code couleur (vert/jaune/rouge)
   - Icônes de statut :
     - 👨‍🔧 OK/KO (Techniciens)
     - 🚜 OK/KO (Équipements)
     - 🔧 OK/KO (Matériels)
   - Message explicatif

4. **Cliquer sur une date VERTE** → Passe à l'étape 3

---

### **ÉTAPE 3 : Confirmer**

1. Vous voyez :
   - 📅 **Date sélectionnée** en grand
   - 📋 **Récapitulatif des exigences**
   - ✅ Badge "Toutes les conditions remplies"

2. **Cliquer sur** : **"✅ Confirmer la planification"**

3. **Résultat attendu** :
   - ✅ Message "Planification enregistrée avec succès!"
   - Le modal se ferme
   - La demande change d'état

---

## 🔍 Cas de Test Spécifiques

### Test 1 : Date Verte (Tout OK)

**Exigences :**
- 1 Électricien
- 50m Câble électrique
- 1 Camion

**Résultat attendu :**
- Plusieurs dates vertes ✅
- Possibilité de sélectionner

---

### Test 2 : Date Jaune (Matériel insuffisant)

**Exigences :**
- 1 Électricien
- 1000m Câble électrique (plus que le stock!)
- 1 Camion

**Résultat attendu :**
- Dates jaunes ⚠️
- Message "Matériel insuffisant"
- Possibilité de sélectionner quand même

---

### Test 3 : Date Rouge (Technicien manquant)

**Exigences :**
- 10 Électriciens (plus qu'il n'y en a!)
- 50m Câble
- 1 Camion

**Résultat attendu :**
- Dates rouges ❌
- Message "Techniciens manquants"
- **IMPOSSIBLE de sélectionner**

---

## 🎨 Vérifications Visuelles

### Interface générale

✅ **Le composant doit** :
- S'afficher dans un modal centré
- Être responsive
- Avoir des transitions fluides
- Afficher les 3 étapes clairement

### Calendrier

✅ **Les dates doivent** :
- Avoir les bonnes couleurs (vert/jaune/rouge)
- Être cliquables (sauf les rouges)
- Afficher les icônes de statut
- Montrer un message explicatif

### Navigation

✅ **Vous devez pouvoir** :
- Passer d'une étape à l'autre
- Revenir en arrière (bouton ← Retour)
- Annuler à tout moment
- Fermer le modal avec ×

---

## 🐛 Problèmes Possibles et Solutions

### Problème : "Aucune date disponible"

**Cause :** Pas de techniciens avec la compétence demandée

**Solution :**
1. Vérifier que des techniciens existent dans la base
2. Vérifier qu'ils ont la compétence demandée
3. Vérifier qu'ils sont marqués "disponibles"

---

### Problème : "Toutes les dates sont rouges"

**Cause :** Ressources insuffisantes

**Solution :**
1. Réduire les exigences (moins de techniciens, etc.)
2. Vérifier le stock de matériels
3. Vérifier l'état des équipements

---

### Problème : "Le composant ne s'affiche pas"

**Cause :** Erreur d'import

**Solution :**
1. Vérifier la console du navigateur (F12)
2. Vérifier que le backend est démarré
3. Vérifier les imports dans `chef-dashboard.component.ts`

---

## 📊 Données de Test Recommandées

### Créer des données de test

1. **Techniciens** :
   - Au moins 2 avec compétence "Électricien"
   - Au moins 1 avec compétence "Plombier"

2. **Équipements** :
   - Au moins 2 camions (état: fonctionnel)
   - Au moins 1 tractopelle

3. **Matériels** :
   - Câble électrique (500m en stock)
   - Tuyau PVC (200m en stock)

---

## ✅ Checklist de Validation

Avant de valider le système, vérifier :

### Backend
- [ ] Service `InterventionValidationService` compile sans erreur
- [ ] Endpoint `/api/interventions/valider-dates` répond
- [ ] Les repositories retournent les bonnes données

### Frontend
- [ ] Le composant `intervention-planification` s'affiche
- [ ] Les 3 étapes sont navigables
- [ ] Le calendrier affiche les bonnes couleurs
- [ ] La sélection de date fonctionne
- [ ] La confirmation fonctionne
- [ ] Le modal se ferme correctement

### Intégration
- [ ] Le bouton "Planifier" ouvre le nouveau composant
- [ ] Les données sont transmises correctement
- [ ] La demande change d'état après planification

---

## 🎯 Résultat Final Attendu

Après avoir planifié une intervention :

1. ✅ Message de confirmation
2. ✅ Modal se ferme
3. ✅ Demande passe en état "TRAITEE"
4. ✅ Intervention créée dans la base
5. ✅ Liste des demandes mise à jour

---

## 📞 En cas de problème

1. **Vérifier les logs** :
   - Backend : Dans le terminal où tourne `mvn spring-boot:run`
   - Frontend : Console du navigateur (F12)

2. **Vérifier la compilation** :
   ```bash
   # Backend
   cd SGII-Ville
   mvn clean compile
   
   # Frontend
   cd SGIIVILLE-FE
   npm run build
   ```

3. **Redémarrer les serveurs** :
   - Arrêter (Ctrl+C)
   - Relancer

---

## 🎉 Félicitations !

Si tous les tests passent, votre système de planification d'intervention avec validation des ressources est **fonctionnel** ! 🚀

Le chef de service peut maintenant :
- ✅ Définir précisément les besoins
- ✅ Voir instantanément les dates disponibles
- ✅ Planifier intelligemment les interventions

**Bonne utilisation ! 🎊**
