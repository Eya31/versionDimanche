/**
 * Utilitaires pour manipulation sécurisée des chaînes de caractères
 * Évite les erreurs TypeError lors d'appels sur null/undefined
 */

/**
 * Convertit une chaîne en minuscules de manière sécurisée
 * @param str - La chaîne à convertir
 * @param defaultValue - Valeur par défaut si str est null/undefined
 * @returns La chaîne en minuscules ou la valeur par défaut
 */
export function safeToLowerCase(str: string | null | undefined, defaultValue: string = ''): string {
  if (!str) return defaultValue;
  return str.toLowerCase();
}

/**
 * Convertit une chaîne en majuscules de manière sécurisée
 * @param str - La chaîne à convertir
 * @param defaultValue - Valeur par défaut si str est null/undefined
 * @returns La chaîne en majuscules ou la valeur par défaut
 */
export function safeToUpperCase(str: string | null | undefined, defaultValue: string = ''): string {
  if (!str) return defaultValue;
  return str.toUpperCase();
}

/**
 * Vérifie si une chaîne contient une sous-chaîne de manière sécurisée
 * @param str - La chaîne principale
 * @param searchString - La sous-chaîne à rechercher
 * @returns true si contient, false sinon
 */
export function safeIncludes(str: string | null | undefined, searchString: string | null | undefined): boolean {
  if (!str || !searchString) return false;
  return str.toLowerCase().includes(searchString.toLowerCase());
}

/**
 * Normalise un texte pour la recherche (trim + lowercase)
 * @param text - Le texte à normaliser
 * @returns Le texte normalisé ou chaîne vide
 */
export function normalizeText(text: string | null | undefined): string {
  if (!text) return '';
  return text.trim().toLowerCase();
}

/**
 * Formate un texte avec fallback
 * @param text - Le texte à formater
 * @param fallback - Valeur de repli si text est null/undefined
 * @returns Le texte ou le fallback
 */
export function safeText(text: string | null | undefined, fallback: string = 'N/A'): string {
  return text || fallback;
}

