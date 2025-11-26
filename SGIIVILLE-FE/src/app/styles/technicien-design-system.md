# 🎨 Guide de Styles UI/UX - Interface Technicien
## Design System Professionnel (Niveau Expert/Doctorant)

---

## 📐 1. PRINCIPES DE DESIGN

### 1.1 Philosophie UX
- **Clarté avant tout** : Information hiérarchisée, actions évidentes
- **Efficacité opérationnelle** : Workflow optimisé pour interventions terrain
- **Feedback immédiat** : Confirmations visuelles pour chaque action
- **Accessibilité universelle** : Conforme WCAG 2.1 AA minimum

### 1.2 Principes visuels
- **Hiérarchie visuelle** : Utilisation de taille, couleur, espacement
- **Cohérence** : Patterns réutilisables, langage visuel unifié
- **Progression** : États clairs (attente → en cours → terminé)
- **Responsive** : Mobile-first, adaptatif desktop

---

## 🎨 2. PALETTE DE COULEURS

### 2.1 Couleurs Principales
```css
/* Primary - Bleu professionnel */
--primary-50: #E3F2FD;
--primary-100: #BBDEFB;
--primary-500: #2196F3;  /* Action principale */
--primary-600: #1976D2;   /* Hover */
--primary-700: #1565C0;   /* Active */

/* Secondary - Orange énergique */
--secondary-500: #FF9800;  /* Alertes, actions secondaires */
--secondary-600: #F57C00;

/* Success - Vert validation */
--success-500: #4CAF50;    /* Terminé, validé */
--success-600: #43A047;

/* Warning - Jaune attention */
--warning-500: #FFC107;    /* En attente, pause */
--warning-600: #FFB300;

/* Error - Rouge urgence */
--error-500: #F44336;      /* Erreur, critique */
--error-600: #E53935;

/* Neutral - Gris professionnel */
--neutral-50: #FAFAFA;
--neutral-100: #F5F5F5;
--neutral-200: #EEEEEE;
--neutral-300: #E0E0E0;
--neutral-500: #9E9E9E;
--neutral-700: #616161;
--neutral-900: #212121;
```

### 2.2 États d'Intervention (Sémantique)
```css
/* États avec codes couleur */
--etat-en-attente: #FF9800;    /* Orange - Action requise */
--etat-en-cours: #2196F3;      /* Bleu - En progression */
--etat-suspendue: #FFC107;     /* Jaune - En pause */
--etat-terminee: #4CAF50;      /* Vert - Complétée */
--etat-reportee: #9E9E9E;      /* Gris - Reportée */

/* Priorités */
--priorite-urgente: #F44336;    /* Rouge - Immédiat */
--priorite-haute: #FF9800;     /* Orange - Important */
--priorite-moyenne: #2196F3;    /* Bleu - Normal */
--priorite-basse: #9E9E9E;      /* Gris - Faible */
```

### 2.3 Contraste d'Accessibilité
- **Texte sur fond clair** : Minimum 4.5:1 (WCAG AA)
- **Texte sur fond sombre** : Minimum 4.5:1
- **Éléments interactifs** : Minimum 3:1
- **Focus visible** : Contraste élevé, outline 2px

---

## 📝 3. TYPOGRAPHIE

### 3.1 Hiérarchie
```css
/* Famille principale - Roboto (Google Fonts) */
font-family: 'Roboto', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;

/* Titres */
--h1-size: 2.5rem;      /* 40px - Page principale */
--h1-weight: 700;
--h1-line-height: 1.2;

--h2-size: 2rem;       /* 32px - Sections */
--h2-weight: 600;
--h2-line-height: 1.3;

--h3-size: 1.5rem;     /* 24px - Sous-sections */
--h3-weight: 600;
--h3-line-height: 1.4;

--h4-size: 1.25rem;    /* 20px - Cartes */
--h4-weight: 600;

/* Corps de texte */
--body-large: 1.125rem;  /* 18px - Texte important */
--body-normal: 1rem;     /* 16px - Texte standard */
--body-small: 0.875rem;  /* 14px - Métadonnées */

/* Labels et UI */
--label-size: 0.875rem;
--label-weight: 500;
--label-transform: uppercase;
--label-spacing: 0.05em;
```

### 3.2 Usage contextuel
- **Titres** : Roboto Bold/Black pour hiérarchie
- **Corps** : Roboto Regular pour lisibilité
- **Actions** : Roboto Medium pour boutons
- **Métadonnées** : Roboto Light pour infos secondaires

---

## 📏 4. ESPACEMENTS (8px Grid System)

### 4.1 Échelle d'espacement
```css
--space-1: 0.25rem;   /* 4px - Padding minimal */
--space-2: 0.5rem;    /* 8px - Base unit */
--space-3: 0.75rem;   /* 12px */
--space-4: 1rem;      /* 16px - Standard */
--space-5: 1.25rem;   /* 20px */
--space-6: 1.5rem;    /* 24px - Sections */
--space-8: 2rem;      /* 32px - Grandes sections */
--space-10: 2.5rem;   /* 40px */
--space-12: 3rem;     /* 48px - Marges importantes */
--space-16: 4rem;      /* 64px - Espacement majeur */
```

### 4.2 Application
- **Padding cartes** : `space-6` (24px)
- **Gap grilles** : `space-4` à `space-6`
- **Marges sections** : `space-8` à `space-12`
- **Espacement interne** : Multiples de `space-2`

---

## 🧩 5. COMPOSANTS UI

### 5.1 Boutons
```css
/* Primary Button */
.btn-primary {
  padding: 12px 24px;
  background: var(--primary-500);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(33, 150, 243, 0.2);
}

.btn-primary:hover {
  background: var(--primary-600);
  box-shadow: 0 4px 8px rgba(33, 150, 243, 0.3);
  transform: translateY(-1px);
}

.btn-primary:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(33, 150, 243, 0.2);
}

.btn-primary:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

/* Secondary Button */
.btn-secondary {
  padding: 12px 24px;
  background: white;
  color: var(--primary-500);
  border: 2px solid var(--primary-500);
  border-radius: 8px;
  font-weight: 500;
}

/* Success Button */
.btn-success {
  background: var(--success-500);
  color: white;
}

/* Danger Button */
.btn-danger {
  background: var(--error-500);
  color: white;
}

/* Sizes */
.btn-small {
  padding: 8px 16px;
  font-size: 0.875rem;
}

.btn-large {
  padding: 16px 32px;
  font-size: 1.125rem;
}
```

### 5.2 Cartes (Cards)
```css
.card {
  background: white;
  border-radius: 12px;
  padding: var(--space-6);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--neutral-200);
}

.card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--neutral-200);
}

.card-body {
  margin-bottom: var(--space-4);
}
```

### 5.3 Badges/Status
```css
.badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.badge-success {
  background: var(--success-500);
  color: white;
}

.badge-warning {
  background: var(--warning-500);
  color: var(--neutral-900);
}

.badge-error {
  background: var(--error-500);
  color: white;
}

.badge-info {
  background: var(--primary-500);
  color: white;
}
```

### 5.4 Inputs/Formulaires
```css
.input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--neutral-300);
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.2s ease;
  background: white;
}

.input:focus {
  outline: none;
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(33, 150, 243, 0.1);
}

.input:disabled {
  background: var(--neutral-100);
  cursor: not-allowed;
}

.label {
  display: block;
  margin-bottom: var(--space-2);
  font-size: var(--label-size);
  font-weight: var(--label-weight);
  color: var(--neutral-700);
  text-transform: var(--label-transform);
  letter-spacing: var(--label-spacing);
}
```

### 5.5 Modals
```css
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: var(--space-8);
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

---

## 📱 6. RESPONSIVE DESIGN

### 6.1 Breakpoints
```css
--mobile: 320px;
--tablet: 768px;
--desktop: 1024px;
--large: 1440px;

/* Usage */
@media (max-width: 768px) {
  /* Mobile styles */
}

@media (min-width: 769px) and (max-width: 1023px) {
  /* Tablet styles */
}

@media (min-width: 1024px) {
  /* Desktop styles */
}
```

### 6.2 Grilles adaptatives
- **Mobile** : 1 colonne
- **Tablet** : 2 colonnes
- **Desktop** : 3-4 colonnes

---

## ♿ 7. ACCESSIBILITÉ

### 7.1 Contraste
- Texte normal : 4.5:1 minimum
- Texte large : 3:1 minimum
- Éléments UI : 3:1 minimum

### 7.2 Navigation clavier
- Tab order logique
- Focus visible (outline 2px)
- Skip links pour navigation rapide

### 7.3 ARIA Labels
- Labels explicites pour lecteurs d'écran
- États dynamiques annoncés
- Rôles sémantiques corrects

### 7.4 Touch Targets
- Minimum 44x44px (iOS/Android)
- Espacement entre éléments cliquables

---

## 🎯 8. ANIMATIONS & TRANSITIONS

### 8.1 Principes
- **Durée** : 200-300ms pour interactions
- **Easing** : `cubic-bezier(0.4, 0, 0.2, 1)` (Material Design)
- **Performance** : Utiliser `transform` et `opacity`

### 8.2 Micro-interactions
- Hover : Légère élévation
- Click : Feedback tactile
- Loading : Spinners animés
- Success : Animation de confirmation

---

## 📊 9. HIÉRARCHIE VISUELLE

### 9.1 Z-index Scale
```css
--z-base: 1;
--z-dropdown: 100;
--z-sticky: 200;
--z-fixed: 300;
--z-modal-backdrop: 400;
--z-modal: 500;
--z-tooltip: 600;
```

### 9.2 Profondeur (Elevation)
```css
--elevation-1: 0 1px 3px rgba(0,0,0,0.12);
--elevation-2: 0 2px 6px rgba(0,0,0,0.12);
--elevation-4: 0 4px 12px rgba(0,0,0,0.12);
--elevation-8: 0 8px 24px rgba(0,0,0,0.12);
--elevation-16: 0 16px 48px rgba(0,0,0,0.12);
```

---

## ✅ 10. CHECKLIST QUALITÉ

- [ ] Contraste WCAG AA respecté
- [ ] Navigation clavier fonctionnelle
- [ ] Responsive sur tous breakpoints
- [ ] Performance optimale (< 3s chargement)
- [ ] Compatibilité navigateurs (Chrome, Firefox, Safari, Edge)
- [ ] Tests utilisateurs validés
- [ ] Documentation complète

---

**Version** : 1.0.0  
**Date** : 2025  
**Auteur** : Équipe SGII-Ville  
**Niveau** : Expert/Doctorant

