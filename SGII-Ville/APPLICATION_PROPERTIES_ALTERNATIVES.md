# Configurations Email Alternatives

## 1. Avec Gmail (Production) ✅

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=eyadammak.ig@gmail.com
spring.mail.password=VOTRE_MOT_DE_PASSE_APP_16_CARACTERES
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Étapes :**
1. Aller sur https://myaccount.google.com/apppasswords
2. Activer la vérification en 2 étapes
3. Créer un mot de passe d'application
4. Copier le mot de passe (format: xxxx xxxx xxxx xxxx)
5. Le coller dans application.properties (sans espaces)

---

## 2. Avec Mailtrap (Développement) 🧪

Service gratuit pour tester les emails sans les envoyer réellement.

```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=VOTRE_USERNAME_MAILTRAP
spring.mail.password=VOTRE_PASSWORD_MAILTRAP
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Étapes :**
1. Créer un compte sur https://mailtrap.io
2. Aller dans "Email Testing" → "Inboxes" → "My Inbox"
3. Copier les credentials SMTP
4. Les coller dans application.properties

---

## 3. Désactiver l'envoi d'emails temporairement

Si vous voulez tester l'application sans email, modifiez `AuthService.java` :

```java
// Commenter l'appel à emailService dans la méthode register()
/*
try {
    emailService.sendWelcomeEmail(
        utilisateur.getEmail(), 
        utilisateur.getNom(), 
        plainPassword,
        utilisateur.getRole().name()
    );
} catch (Exception e) {
    System.err.println("❌ Erreur email: " + e.getMessage());
}
*/
```

---

## 4. Avec Outlook/Hotmail

```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=votre-email@outlook.com
spring.mail.password=VOTRE_MOT_DE_PASSE
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## Vérifier la configuration

Après modification, redémarrez le serveur :

```bash
cd /Users/eyadammak/Documents/CYCLE\ ING/sgiiv/SGII-Ville
mvn spring-boot:run
```

Les logs doivent montrer :
```
✅ Email de bienvenue envoyé à: utilisateur@example.com
```

Au lieu de :
```
❌ Erreur lors de l'envoi de l'email de bienvenue: Authentication failed
```
