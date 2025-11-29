# Guide de Test - Système de Planification d'Intervention

## Prérequis

### Données de Test

Assurez-vous que votre système contient les données suivantes :

#### Techniciens
| ID  | Nom            | Compétences                    | Disponible |
|-----|----------------|--------------------------------|------------|
| 101 | Jean Dupont    | Électricien, Installation      | Oui ✅     |
| 102 | Marie Martin   | Électricien, Maintenance       | Oui ✅     |
| 103 | Pierre Dubois  | Plombier                       | Oui ✅     |
| 104 | Sophie Bernard | Maçon, Travaux publics         | Non ❌     |

#### Équipements
| ID  | Nom           | Type        | État          | Disponible | Périodes Indispo    |
|-----|---------------|-------------|---------------|------------|---------------------|
| 201 | Camion Grue 1 | Camion      | Fonctionnel ✅ | Oui ✅     | Aucune              |
| 202 | Tractopelle A | Tractopelle | Fonctionnel ✅ | Oui ✅     | 10-15 déc 2025      |
| 203 | Nacelle B     | Nacelle     | En panne ❌    | Non ❌     | -                   |
| 204 | Camion Benne  | Camion      | Fonctionnel ✅ | Oui ✅     | Aucune              |

#### Matériels
| ID  | Désignation       | Stock | Unité  |
|-----|-------------------|-------|--------|
| 301 | Câble électrique  | 500   | mètre  |
| 302 | Tuyau PVC         | 200   | mètre  |
| 303 | Ciment            | 50    | sac    |
| 304 | Peinture blanche  | 5     | litre  |
| 305 | Lampadaire LED    | 15    | unité  |

## Scénarios de Test

### Test 1 : Validation avec toutes les ressources disponibles (VERT ✅)

**Objectif :** Vérifier qu'une date apparaît en vert quand toutes les ressources sont disponibles.

**Requête :**
```bash
curl -X POST http://localhost:8080/api/interventions/valider-dates \
  -H "Content-Type: application/json" \
  -d '{
    "dateDebut": "2025-12-01",
    "dateFin": "2025-12-05",
    "competencesRequises": [
      {
        "competence": "Électricien",
        "nombreTechniciens": 2
      }
    ],
    "materielsRequis": [
      {
        "designation": "Câble électrique",
        "quantiteRequise": 100
      }
    ],
    "equipementsRequis": [
      {
        "type": "Camion",
        "quantiteRequise": 1
      }
    ]
  }'
```

**Résultat attendu :**
```json
[
  {
    "date": "2025-12-01",
    "status": "VERT",
    "message": "Toutes les ressources sont disponibles",
    "techniciensDisponibles": true,
    "equipementsDisponibles": true,
    "materielsDisponibles": true
  },
  {
    "date": "2025-12-02",
    "status": "VERT",
    ...
  }
]
```

**Explication :**
- ✅ 2 électriciens disponibles (Jean + Marie)
- ✅ 2 camions fonctionnels et disponibles
- ✅ 500m de câble en stock (> 100m requis)

---

### Test 2 : Validation avec matériel insuffisant (JAUNE ⚠️)

**Objectif :** Vérifier qu'une date apparaît en jaune quand seuls les matériels manquent.

**Requête :**
```bash
curl -X POST http://localhost:8080/api/interventions/valider-dates \
  -H "Content-Type: application/json" \
  -d '{
    "dateDebut": "2025-12-01",
    "dateFin": "2025-12-03",
    "competencesRequises": [
      {
        "competence": "Électricien",
        "nombreTechniciens": 1
      }
    ],
    "materielsRequis": [
      {
        "designation": "Peinture blanche",
        "quantiteRequise": 20
      }
    ],
    "equipementsRequis": [
      {
        "type": "Camion",
        "quantiteRequise": 1
      }
    ]
  }'
```

**Résultat attendu :**
```json
[
  {
    "date": "2025-12-01",
    "status": "JAUNE",
    "message": "Techniciens et équipements disponibles, mais matériel insuffisant",
    "techniciensDisponibles": true,
    "equipementsDisponibles": true,
    "materielsDisponibles": false
  }
]
```

**Explication :**
- ✅ 1 électricien disponible
- ✅ Camion disponible
- ❌ Seulement 5 litres de peinture en stock (< 20 requis)

---

### Test 3 : Validation avec techniciens insuffisants (ROUGE ❌)

**Objectif :** Vérifier qu'une date apparaît en rouge quand les techniciens manquent.

**Requête :**
```bash
curl -X POST http://localhost:8080/api/interventions/valider-dates \
  -H "Content-Type: application/json" \
  -d '{
    "dateDebut": "2025-12-01",
    "dateFin": "2025-12-03",
    "competencesRequises": [
      {
        "competence": "Électricien",
        "nombreTechniciens": 5
      }
    ],
    "materielsRequis": [],
    "equipementsRequis": []
  }'
```

**Résultat attendu :**
```json
[
  {
    "date": "2025-12-01",
    "status": "ROUGE",
    "message": "Ressources manquantes : techniciens",
    "techniciensDisponibles": false,
    "equipementsDisponibles": true,
    "materielsDisponibles": true
  }
]
```

**Explication :**
- ❌ Seulement 2 électriciens disponibles (< 5 requis)

---

### Test 4 : Validation avec équipement non fonctionnel (ROUGE ❌)

**Objectif :** Vérifier que les équipements en panne ne sont pas comptabilisés.

**Requête :**
```bash
curl -X POST http://localhost:8080/api/interventions/valider-dates \
  -H "Content-Type: application/json" \
  -d '{
    "dateDebut": "2025-12-01",
    "dateFin": "2025-12-03",
    "competencesRequises": [],
    "materielsRequis": [],
    "equipementsRequis": [
      {
        "type": "Nacelle",
        "quantiteRequise": 1
      }
    ]
  }'
```

**Résultat attendu :**
```json
[
  {
    "date": "2025-12-01",
    "status": "ROUGE",
    "message": "Ressources manquantes : équipements",
    "techniciensDisponibles": true,
    "equipementsDisponibles": false,
    "materielsDisponibles": true
  }
]
```

**Explication :**
- ❌ Nacelle B est en panne et indisponible

---

### Test 5 : Validation avec période d'indisponibilité (ROUGE ❌)

**Objectif :** Vérifier qu'un équipement indisponible pendant une période n'est pas comptabilisé.

**Requête :**
```bash
curl -X POST http://localhost:8080/api/interventions/valider-dates \
  -H "Content-Type: application/json" \
  -d '{
    "dateDebut": "2025-12-10",
    "dateFin": "2025-12-15",
    "competencesRequises": [],
    "materielsRequis": [],
    "equipementsRequis": [
      {
        "type": "Tractopelle",
        "quantiteRequise": 1
      }
    ]
  }'
```

**Résultat attendu :**
```json
[
  {
    "date": "2025-12-10",
    "status": "ROUGE",
    "message": "Ressources manquantes : équipements",
    "techniciensDisponibles": true,
    "equipementsDisponibles": false,
    "materielsDisponibles": true
  },
  ...
  {
    "date": "2025-12-15",
    "status": "ROUGE",
    ...
  }
]
```

**Explication :**
- ❌ Tractopelle A indisponible du 10 au 15 décembre

---

### Test 6 : Validation avec compétence inexistante (ROUGE ❌)

**Objectif :** Vérifier qu'une compétence non disponible génère un statut rouge.

**Requête :**
```bash
curl -X POST http://localhost:8080/api/interventions/valider-dates \
  -H "Content-Type: application/json" \
  -d '{
    "dateDebut": "2025-12-01",
    "dateFin": "2025-12-03",
    "competencesRequises": [
      {
        "competence": "Soudeur",
        "nombreTechniciens": 1
      }
    ],
    "materielsRequis": [],
    "equipementsRequis": []
  }'
```

**Résultat attendu :**
```json
[
  {
    "date": "2025-12-01",
    "status": "ROUGE",
    "message": "Ressources manquantes : techniciens",
    "techniciensDisponibles": false,
    "equipementsDisponibles": true,
    "materielsDisponibles": true
  }
]
```

**Explication :**
- ❌ Aucun technicien avec la compétence "Soudeur"

---

### Test 7 : Validation avec exigences multiples

**Objectif :** Vérifier la validation avec plusieurs types de ressources.

**Requête :**
```bash
curl -X POST http://localhost:8080/api/interventions/valider-dates \
  -H "Content-Type: application/json" \
  -d '{
    "dateDebut": "2025-12-01",
    "dateFin": "2025-12-03",
    "competencesRequises": [
      {
        "competence": "Électricien",
        "nombreTechniciens": 1
      },
      {
        "competence": "Plombier",
        "nombreTechniciens": 1
      }
    ],
    "materielsRequis": [
      {
        "designation": "Câble électrique",
        "quantiteRequise": 50
      },
      {
        "designation": "Tuyau PVC",
        "quantiteRequise": 30
      }
    ],
    "equipementsRequis": [
      {
        "type": "Camion",
        "quantiteRequise": 1
      }
    ]
  }'
```

**Résultat attendu :**
```json
[
  {
    "date": "2025-12-01",
    "status": "VERT",
    "message": "Toutes les ressources sont disponibles",
    "techniciensDisponibles": true,
    "equipementsDisponibles": true,
    "materielsDisponibles": true
  }
]
```

**Explication :**
- ✅ 1 électricien (Jean ou Marie)
- ✅ 1 plombier (Pierre)
- ✅ Câble et tuyau en stock suffisant
- ✅ Camion disponible

---

## Test Frontend

### Étapes de Test Manuel

1. **Démarrer l'application :**
   ```bash
   # Terminal 1 - Backend
   cd SGII-Ville
   ./mvnw spring-boot:run
   
   # Terminal 2 - Frontend
   cd SGIIVILLE-FE
   npm start
   ```

2. **Naviguer vers le dashboard chef :**
   - URL : `http://localhost:4200`
   - Se connecter en tant que chef de service
   - Accéder à la liste des demandes

3. **Ouvrir la planification :**
   - Cliquer sur "Planifier" pour une demande
   - Vérifier que le composant de planification s'ouvre

4. **Tester l'étape 1 - Exigences :**
   - Ajouter une compétence : "Électricien" (2 techniciens)
   - Ajouter un matériel : "Câble électrique" (100 unités)
   - Ajouter un équipement : "Camion" (1 unité)
   - Sélectionner une période (du 1er au 10 décembre 2025)
   - Cliquer sur "Rechercher les dates disponibles"

5. **Tester l'étape 2 - Calendrier :**
   - Vérifier que les dates s'affichent avec les bonnes couleurs
   - Vérifier les statistiques en haut (nombre de dates vertes/jaunes/rouges)
   - Survoler les dates pour voir les détails
   - Cliquer sur une date verte

6. **Tester l'étape 3 - Confirmation :**
   - Vérifier le récapitulatif de la date sélectionnée
   - Vérifier le récapitulatif des exigences
   - Cliquer sur "Confirmer la planification"

7. **Vérifier la création :**
   - Vérifier qu'une notification de succès apparaît
   - Vérifier que l'intervention a été créée dans la liste

### Cas de Test Frontend

| Test | Action | Résultat Attendu |
|------|--------|------------------|
| 1 | Ajouter une compétence vide | Rien ne se passe |
| 2 | Ajouter une compétence valide | Apparaît dans la liste |
| 3 | Supprimer une compétence | Disparaît de la liste |
| 4 | Chercher sans exigences | Message d'erreur |
| 5 | Chercher avec période invalide | Message d'erreur |
| 6 | Cliquer sur date rouge | Message indiquant qu'elle n'est pas disponible |
| 7 | Cliquer sur date verte | Passage à l'étape confirmation |
| 8 | Retour arrière | Retour à l'étape précédente |
| 9 | Annuler | Fermeture du composant |

---

## Checklist de Validation

### Backend ✅
- [ ] Service de validation créé
- [ ] Endpoint `/valider-dates` fonctionnel
- [ ] Validation des techniciens par compétence
- [ ] Validation des équipements (état + disponibilité)
- [ ] Validation des matériels (stock)
- [ ] Statuts VERT/JAUNE/ROUGE corrects
- [ ] Messages d'erreur pertinents

### Frontend ✅
- [ ] Composant de planification créé
- [ ] Étape 1 : Ajout/suppression d'exigences
- [ ] Étape 2 : Affichage du calendrier coloré
- [ ] Étape 3 : Confirmation et récapitulatif
- [ ] Navigation entre étapes fluide
- [ ] Design responsive
- [ ] Messages d'erreur clairs

### Intégration ✅
- [ ] Communication Backend ↔ Frontend
- [ ] Gestion des erreurs réseau
- [ ] Temps de réponse acceptable
- [ ] Données cohérentes

---

## Dépannage

### Problème : Toutes les dates sont rouges

**Cause possible :**
- Aucun technicien ne correspond aux compétences
- Tous les équipements sont en panne
- Stock de matériels insuffisant

**Solution :**
1. Vérifier les données de test dans la base XML
2. Ajuster les exigences pour correspondre aux ressources disponibles

### Problème : Erreur 500 au backend

**Cause possible :**
- NullPointerException dans le service de validation
- Repository non trouvé

**Solution :**
1. Vérifier les logs du backend
2. S'assurer que tous les repositories sont correctement injectés
3. Vérifier que les fichiers XML existent

### Problème : Calendrier ne s'affiche pas

**Cause possible :**
- Erreur de communication avec le backend
- CORS non configuré

**Solution :**
1. Vérifier que le backend est démarré
2. Vérifier la configuration CORS dans `InterventionController`
3. Ouvrir la console développeur pour voir les erreurs

---

## Métriques de Performance

| Opération | Temps Maximum Accepté |
|-----------|----------------------|
| Validation de 30 jours | 2 secondes |
| Affichage du calendrier | 500ms |
| Confirmation planification | 1 seconde |

---

## Conclusion

Ce guide couvre tous les scénarios de test pour valider le système de planification d'intervention. Assurez-vous de tester chaque scénario avant de déployer en production.

Pour toute question, consultez la documentation complète dans `INTERVENTION_PLANIFICATION_README.md`.
