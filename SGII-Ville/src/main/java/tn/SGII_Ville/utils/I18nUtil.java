package tn.SGII_Ville.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Utilitaire pour accéder aux messages multilingues
 * Exemple: I18nUtil.getMessage("common.success")
 */
@Component
public class I18nUtil {

    private static MessageSource messageSource;

    @Autowired
    public I18nUtil(MessageSource messageSource) {
        I18nUtil.messageSource = messageSource;
    }

    /**
     * Récupère un message traduit selon la locale courante
     * @param key La clé du message
     * @return Le message traduit
     */
    public static String getMessage(String key) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            return key; // Retourner la clé si le message n'est pas trouvé
        }
    }

    /**
     * Récupère un message traduit avec des paramètres
     * @param key La clé du message
     * @param args Les paramètres à injecter
     * @return Le message traduit
     */
    public static String getMessage(String key, Object[] args) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * Récupère un message traduit pour une locale spécifique
     * @param key La clé du message
     * @param locale La locale souhaitée
     * @return Le message traduit
     */
    public static String getMessage(String key, Locale locale) {
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * Récupère la locale courante
     */
    public static Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Récupère le code de la langue courante (fr, en, ar)
     */
    public static String getCurrentLanguage() {
        return LocaleContextHolder.getLocale().getLanguage();
    }
}
