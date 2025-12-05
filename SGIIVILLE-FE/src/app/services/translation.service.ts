import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  private defaultLanguage = 'fr';
  private supportedLanguages = ['fr', 'en', 'ar'];
  
  private currentLanguage = new BehaviorSubject<string>(this.defaultLanguage);
  public currentLanguage$ = this.currentLanguage.asObservable();
  
  // Traductions intégrées directement
  private translations: { [key: string]: any } = {
    fr: this.getFrenchTranslations(),
    en: this.getEnglishTranslations(),
    ar: this.getArabicTranslations()
  };

  constructor() {
    this.initialize();
  }

  /**
   * Initialise le service
   */
  private initialize(): void {
    const storedLanguage = this.getStoredLanguage();
    this.currentLanguage.next(storedLanguage);
    this.applyLanguageChanges(storedLanguage);
  }

  /**
   * Récupère la langue stockée de manière sûre
   */
  private getStoredLanguage(): string {
    try {
      const stored = localStorage?.getItem('selectedLanguage');
      return (stored && this.supportedLanguages.includes(stored)) ? stored : this.defaultLanguage;
    } catch (error) {
      return this.defaultLanguage;
    }
  }

  /**
   * Change la langue actuelle
   */
  setLanguage(language: string): void {
    if (this.supportedLanguages.includes(language)) {
      console.log(`[TranslationService] Changement de langue: ${this.currentLanguage.value} → ${language}`);
      this.currentLanguage.next(language);
      try {
        localStorage?.setItem('selectedLanguage', language);
      } catch (error) {
        console.warn('Impossible de sauvegarder la langue dans localStorage');
      }
      this.applyLanguageChanges(language);
    }
  }

  /**
   * Retourne la langue actuellement sélectionnée
   */
  getCurrentLanguage(): string {
    return this.currentLanguage.value;
  }

  /**
   * Retourne les langues supportées
   */
  getSupportedLanguages(): string[] {
    return this.supportedLanguages;
  }

  /**
   * Récupère une clé traduite
   */
  translate(key: string, defaultValue?: string): string {
    const currentLang = this.currentLanguage.value;
    const langTranslations = this.translations[currentLang] || this.translations[this.defaultLanguage];
    
    const keys = key.split('.');
    let value: any = langTranslations;

    for (const k of keys) {
      if (value && typeof value === 'object' && k in value) {
        value = value[k];
      } else {
        console.warn(`[TranslationService] Clé non trouvée: ${key} pour langue: ${currentLang}`);
        return defaultValue || key;
      }
    }

    return typeof value === 'string' ? value : (defaultValue || key);
  }

  /**
   * Traduit une catégorie de demande
   */
  translateCategory(category?: string): string {
    if (!category) return 'Demande';
    return this.translate(`categories.${category}`, category);
  }

  /**
   * Applique les changements de langue au DOM
   */
  private applyLanguageChanges(language: string): void {
    const dir = language === 'ar' ? 'rtl' : 'ltr';
    if (typeof document !== 'undefined') {
      document.documentElement.lang = language;
      document.documentElement.dir = dir;
      document.body.dir = dir;
    }
  }

  /**
   * Retourne tous les labels de langue avec leurs codes
   */
  getLanguageLabels(): { code: string; label: string }[] {
    return [
      { code: 'fr', label: 'Français' },
      { code: 'en', label: 'English' },
      { code: 'ar', label: 'العربية' }
    ];
  }

  // ===== TRADUCTIONS FRANÇAIS =====
  private getFrenchTranslations(): any {
    return {
      app: {
        title: 'SGII-Ville',
        subtitle: 'Système de Gestion des Interventions Urbaines'
      },
      nav: {
        home: 'Accueil',
        interventions: 'Interventions',
        demandes: 'Demandes',
        planification: 'Planification',
        rapports: 'Rapports',
        admin: 'Administration',
        profile: 'Profil',
        logout: 'Déconnexion',
        login: 'Connexion',
        register: 'S\'inscrire'
      },
      dashboard: {
        citoyen: 'Espace Citoyen',
        chef: 'Espace Chef de Service',
        technicien: 'Espace Technicien',
        mainDoeuvre: 'Espace Main-d\'Œuvre',
        admin: 'Espace Admin',
        welcome: 'Bienvenue',
        manageRequests: 'Gérez vos signalements et suivez leur état'
      },
      demande: {
        create: 'Créer une demande',
        list: 'Liste des demandes',
        edit: 'Modifier',
        delete: 'Supprimer',
        details: 'Détails',
        status: 'État',
        date: 'Date',
        description: 'Description',
        location: 'Localisation',
        priority: 'Priorité',
        type: 'Type'
      },
      categories: {
        'Eau & Assainissement': 'Eau & Assainissement',
        'Éclairage public': 'Éclairage public',
        'Voirie': 'Voirie',
        'Propreté urbaine': 'Propreté urbaine',
        'Espaces verts': 'Espaces verts',
        'Sécurité routière': 'Sécurité routière',
        'Gestion des déchets': 'Gestion des déchets',
        'Transports': 'Transports',
        'Logement': 'Logement',
        'Autre': 'Autre'
      },
      intervention: {
        create: 'Créer une intervention',
        list: 'Liste des interventions',
        edit: 'Modifier',
        delete: 'Supprimer',
        plan: 'Planifier',
        assign: 'Assigner',
        start: 'Démarrer',
        finish: 'Terminer',
        status: 'État',
        date: 'Date',
        technician: 'Technicien'
      },
      common: {
        save: 'Enregistrer',
        cancel: 'Annuler',
        edit: 'Modifier',
        delete: 'Supprimer',
        add: 'Ajouter',
        close: 'Fermer',
        back: 'Retour',
        next: 'Suivant',
        previous: 'Précédent',
        search: 'Rechercher',
        filter: 'Filtrer',
        export: 'Exporter',
        import: 'Importer',
        download: 'Télécharger',
        upload: 'Télécharger',
        loading: 'Chargement...',
        error: 'Erreur',
        success: 'Succès',
        warning: 'Avertissement',
        noData: 'Aucune donnée',
        yes: 'Oui',
        no: 'Non'
      },
      notifications: {
        unread: 'Non lu',
        all: 'Notifications',
        read: 'Lu',
        newMessage: 'Nouveau message',
        markAsRead: 'Marquer comme lu'
      },
      visiteur: {
        home: 'Accueil',
        demandesTerminees: 'Demandes terminées',
        faq: 'FAQ',
        about: 'À propos',
        connection: 'Connexion',
        platformTitle: 'elBaladiya.tn',
        platformSubtitle: 'La plateforme numérique municipale',
        platformDescription: 'Connectez les citoyens et les municipalités pour une gestion transparente et efficace des services urbains',
        whatIs: 'Qu\'est-ce que elBaladiya.tn ?',
        whatIsDescription: 'elBaladiya.tn est une plateforme innovante qui permet aux citoyens de soumettre des demandes d\'intervention urbaine et de suivre leur traitement en temps réel. Les municipalités peuvent planifier et gérer efficacement les interventions grâce à un système intelligent de validation des ressources.',
        discoverServices: 'Découvrir les services >',
        registerMunicipality: 'S\'inscrire en tant que municipalité >',
        impact: 'Notre impact',
        demandesTraitees: 'Demandes traitées',
        totalDemandes: 'Total des demandes',
        citizenDemands: 'Demandes citoyennes terminées',
        loading: 'Chargement...',
        noDemands: 'Aucune demande terminée disponible pour le moment.',
        viewDetails: 'Voir détails >',
        viewAllDemands: 'Voir toutes les demandes terminées >',
        usefulLinks: 'Liens utiles',
        contact: 'Contact',
        contactMessage: 'Pour toute question, contactez-nous via le formulaire d\'inscription municipalité.',
        copyright: '© 2024 elBaladiya.tn - Tous droits réservés',
        request: 'Demande',
        demandesTermineesPage: 'Demandes citoyennes terminées',
        demandesTermineesSubtitle: 'Consultez les demandes qui ont été traitées avec succès',
        filters: 'Filtres',
        category: 'Catégorie',
        allCategories: 'Toutes les catégories',
        dateDebut: 'Date début',
        dateFin: 'Date fin',
        reset: 'Réinitialiser',
        loadingDemands: 'Chargement des demandes...',
        noCriteriaMatch: 'Aucune demande terminée ne correspond à vos critères de recherche.',
        terminated: 'Terminée',
        location: 'Localisation',
        faqPage: 'Foire aux Questions (FAQ)',
        faqSubtitle: 'Trouvez les réponses aux questions les plus fréquentes',
        aboutPage: 'À propos de elBaladiya.tn',
        mission: 'Notre Mission',
        missionText: 'elBaladiya.tn a pour mission de moderniser la gestion des services municipaux en créant un pont numérique entre les citoyens et les municipalités. Nous facilitons la communication, la transparence et l\'efficacité dans le traitement des demandes d\'intervention urbaine.',
        values: 'Nos Valeurs',
        citizenParticipation: 'Participation Citoyenne',
        citizenParticipationText: 'Nous croyons en l\'importance de donner la parole aux citoyens et de les impliquer activement dans l\'amélioration de leur environnement urbain.',
        transparency: 'Transparence',
        transparencyText: 'Tous les citoyens peuvent consulter les demandes terminées pour garantir la transparence et la crédibilité du service public.',
        efficiency: 'Efficacité',
        efficiencyText: 'Notre système de planification intelligent permet d\'optimiser l\'utilisation des ressources et de réduire les délais de traitement.',
        techUsed: 'Technologies Utilisées'
      }
    };
  }

  // ===== TRADUCTIONS ANGLAIS =====
  private getEnglishTranslations(): any {
    return {
      app: {
        title: 'SGII-City',
        subtitle: 'Urban Intervention Management System'
      },
      nav: {
        home: 'Home',
        interventions: 'Interventions',
        demandes: 'Requests',
        planification: 'Planning',
        rapports: 'Reports',
        admin: 'Administration',
        profile: 'Profile',
        logout: 'Logout',
        login: 'Login',
        register: 'Register'
      },
      dashboard: {
        citoyen: 'Citizen Space',
        chef: 'Service Manager Space',
        technicien: 'Technician Space',
        mainDoeuvre: 'Labor Space',
        admin: 'Admin Space',
        welcome: 'Welcome',
        manageRequests: 'Manage your reports and track their status'
      },
      demande: {
        create: 'Create Request',
        list: 'Requests List',
        edit: 'Edit',
        delete: 'Delete',
        details: 'Details',
        status: 'Status',
        date: 'Date',
        description: 'Description',
        location: 'Location',
        priority: 'Priority',
        type: 'Type'
      },
      categories: {
        'Eau & Assainissement': 'Water & Sanitation',
        'Éclairage public': 'Public Lighting',
        'Voirie': 'Roads',
        'Propreté urbaine': 'Urban Cleanliness',
        'Espaces verts': 'Green Spaces',
        'Sécurité routière': 'Road Safety',
        'Gestion des déchets': 'Waste Management',
        'Transports': 'Transportation',
        'Logement': 'Housing',
        'Autre': 'Other'
      },
      intervention: {
        create: 'Create Intervention',
        list: 'Interventions List',
        edit: 'Edit',
        delete: 'Delete',
        plan: 'Plan',
        assign: 'Assign',
        start: 'Start',
        finish: 'Finish',
        status: 'Status',
        date: 'Date',
        technician: 'Technician'
      },
      common: {
        save: 'Save',
        cancel: 'Cancel',
        edit: 'Edit',
        delete: 'Delete',
        add: 'Add',
        close: 'Close',
        back: 'Back',
        next: 'Next',
        previous: 'Previous',
        search: 'Search',
        filter: 'Filter',
        export: 'Export',
        import: 'Import',
        download: 'Download',
        upload: 'Upload',
        loading: 'Loading...',
        error: 'Error',
        success: 'Success',
        warning: 'Warning',
        noData: 'No data',
        yes: 'Yes',
        no: 'No'
      },
      notifications: {
        unread: 'Unread',
        all: 'Notifications',
        read: 'Read',
        newMessage: 'New message',
        markAsRead: 'Mark as read'
      },
      visiteur: {
        home: 'Home',
        demandesTerminees: 'Completed Requests',
        faq: 'FAQ',
        about: 'About',
        connection: 'Login',
        platformTitle: 'elBaladiya.tn',
        platformSubtitle: 'Municipal Digital Platform',
        platformDescription: 'Connect citizens and municipalities for transparent and efficient management of urban services',
        whatIs: 'What is elBaladiya.tn?',
        whatIsDescription: 'elBaladiya.tn is an innovative platform that allows citizens to submit urban intervention requests and track their processing in real time. Municipalities can efficiently plan and manage interventions through an intelligent resource validation system.',
        discoverServices: 'Discover Services >',
        registerMunicipality: 'Register as Municipality >',
        impact: 'Our Impact',
        demandesTraitees: 'Processed Requests',
        totalDemandes: 'Total Requests',
        citizenDemands: 'Completed Citizen Requests',
        loading: 'Loading...',
        noDemands: 'No completed requests available at the moment.',
        viewDetails: 'View Details >',
        viewAllDemands: 'View all Completed Requests >',
        usefulLinks: 'Useful Links',
        contact: 'Contact',
        contactMessage: 'For any questions, contact us via the municipality registration form.',
        copyright: '© 2024 elBaladiya.tn - All rights reserved',
        request: 'Request',
        demandesTermineesPage: 'Completed Citizen Requests',
        demandesTermineesSubtitle: 'Review requests that have been successfully processed',
        filters: 'Filters',
        category: 'Category',
        allCategories: 'All Categories',
        dateDebut: 'Start Date',
        dateFin: 'End Date',
        reset: 'Reset',
        loadingDemands: 'Loading requests...',
        noCriteriaMatch: 'No completed requests match your search criteria.',
        terminated: 'Completed',
        location: 'Location',
        faqPage: 'Frequently Asked Questions (FAQ)',
        faqSubtitle: 'Find answers to the most common questions',
        aboutPage: 'About elBaladiya.tn',
        mission: 'Our Mission',
        missionText: 'elBaladiya.tn aims to modernize municipal service management by creating a digital bridge between citizens and municipalities. We facilitate communication, transparency and efficiency in processing urban intervention requests.',
        values: 'Our Values',
        citizenParticipation: 'Citizen Participation',
        citizenParticipationText: 'We believe in the importance of giving voice to citizens and actively involving them in improving their urban environment.',
        transparency: 'Transparency',
        transparencyText: 'All citizens can view completed requests to ensure transparency and credibility of public services.',
        efficiency: 'Efficiency',
        efficiencyText: 'Our intelligent planning system optimizes resource use and reduces processing times.',
        techUsed: 'Technologies Used'
      }
    };
  }

  // ===== TRADUCTIONS ARABE =====
  private getArabicTranslations(): any {
    return {
      app: {
        title: 'نظام إدارة التدخلات',
        subtitle: 'نظام إدارة التدخلات الحضرية'
      },
      nav: {
        home: 'الرئيسية',
        interventions: 'التدخلات',
        demandes: 'الطلبات',
        planification: 'التخطيط',
        rapports: 'التقارير',
        admin: 'الإدارة',
        profile: 'الملف الشخصي',
        logout: 'تسجيل الخروج',
        login: 'تسجيل الدخول',
        register: 'إنشاء حساب'
      },
      dashboard: {
        citoyen: 'مساحة المواطن',
        chef: 'مساحة رئيس الخدمة',
        technicien: 'مساحة الفني',
        mainDoeuvre: 'مساحة القوى العاملة',
        admin: 'مساحة الإدارة',
        welcome: 'أهلا وسهلا',
        manageRequests: 'إدارة تقاريرك وتتبع حالتها'
      },
      demande: {
        create: 'إنشاء طلب',
        list: 'قائمة الطلبات',
        edit: 'تعديل',
        delete: 'حذف',
        details: 'التفاصيل',
        status: 'الحالة',
        date: 'التاريخ',
        description: 'الوصف',
        location: 'الموقع',
        priority: 'الأولوية',
        type: 'النوع'
      },
      categories: {
        'Eau & Assainissement': 'المياه والصرف الصحي',
        'Éclairage public': 'الإضاءة العامة',
        'Voirie': 'الطرق',
        'Propreté urbaine': 'النظافة الحضرية',
        'Espaces verts': 'المساحات الخضراء',
        'Sécurité routière': 'سلامة الطرق',
        'Gestion des déchets': 'إدارة النفايات',
        'Transports': 'النقل',
        'Logement': 'الإسكان',
        'Autre': 'أخرى'
      },
      intervention: {
        create: 'إنشاء تدخل',
        list: 'قائمة التدخلات',
        edit: 'تعديل',
        delete: 'حذف',
        plan: 'التخطيط',
        assign: 'تعيين',
        start: 'ابدأ',
        finish: 'إنهاء',
        status: 'الحالة',
        date: 'التاريخ',
        technician: 'الفني'
      },
      common: {
        save: 'حفظ',
        cancel: 'إلغاء',
        edit: 'تعديل',
        delete: 'حذف',
        add: 'إضافة',
        close: 'إغلاق',
        back: 'رجوع',
        next: 'التالي',
        previous: 'السابق',
        search: 'بحث',
        filter: 'تصفية',
        export: 'تصدير',
        import: 'استيراد',
        download: 'تحميل',
        upload: 'رفع',
        loading: 'جاري التحميل...',
        error: 'خطأ',
        success: 'نجاح',
        warning: 'تحذير',
        noData: 'لا توجد بيانات',
        yes: 'نعم',
        no: 'لا'
      },
      notifications: {
        unread: 'غير مقروءة',
        all: 'الإخطارات',
        read: 'مقروءة',
        newMessage: 'رسالة جديدة',
        markAsRead: 'وضع علامة كمقروءة'
      },
      visiteur: {
        home: 'الرئيسية',
        demandesTerminees: 'الطلبات المنجزة',
        faq: 'الأسئلة الشائعة',
        about: 'حول',
        connection: 'دخول',
        platformTitle: 'elBaladiya.tn',
        platformSubtitle: 'منصة البلدية الرقمية',
        platformDescription: 'ربط المواطنين والبلديات لإدارة شفافة وفعالة للخدمات الحضرية',
        whatIs: 'ما هي elBaladiya.tn؟',
        whatIsDescription: 'elBaladiya.tn هي منصة مبتكرة تسمح للمواطنين بتقديم طلبات التدخل الحضري ومتابعة معالجتها في الوقت الفعلي. يمكن للبلديات التخطيط وإدارة التدخلات بكفاءة من خلال نظام ذكي للتحقق من الموارد.',
        discoverServices: 'اكتشف الخدمات >',
        registerMunicipality: 'التسجيل كبلدية >',
        impact: 'تأثيرنا',
        demandesTraitees: 'الطلبات المعالجة',
        totalDemandes: 'إجمالي الطلبات',
        citizenDemands: 'طلبات المواطنين المنجزة',
        loading: 'جاري التحميل...',
        noDemands: 'لا توجد طلبات منجزة متاحة في الوقت الحالي.',
        viewDetails: 'عرض التفاصيل >',
        viewAllDemands: 'عرض جميع الطلبات المنجزة >',
        usefulLinks: 'روابط مفيدة',
        contact: 'اتصل',
        contactMessage: 'للتواصل معنا، يرجى استخدام نموذج التسجيل للبلدية.',
        copyright: '© 2024 elBaladiya.tn - جميع الحقوق محفوظة',
        request: 'طلب',
        demandesTermineesPage: 'طلبات المواطنين المنجزة',
        demandesTermineesSubtitle: 'راجع الطلبات التي تمت معالجتها بنجاح',
        filters: 'المرشحات',
        category: 'الفئة',
        allCategories: 'جميع الفئات',
        dateDebut: 'تاريخ البداية',
        dateFin: 'تاريخ النهاية',
        reset: 'إعادة تعيين',
        loadingDemands: 'جاري تحميل الطلبات...',
        noCriteriaMatch: 'لا توجد طلبات منجزة تتطابق مع معايير البحث.',
        terminated: 'منجزة',
        location: 'الموقع',
        faqPage: 'الأسئلة الشائعة',
        faqSubtitle: 'ابحث عن إجابات الأسئلة الأكثر شيوعا',
        aboutPage: 'حول elBaladiya.tn',
        mission: 'مهمتنا',
        missionText: 'تهدف elBaladiya.tn إلى تحديث إدارة الخدمات البلدية من خلال إنشاء جسر رقمي بين المواطنين والبلديات. نحن نسهل التواصل والشفافية والكفاءة في معالجة طلبات التدخل الحضري.',
        values: 'قيمنا',
        citizenParticipation: 'مشاركة المواطنين',
        citizenParticipationText: 'نحن نؤمن بأهمية إعطاء الصوت للمواطنين وإشراكهم بنشاط في تحسين بيئتهم الحضرية.',
        transparency: 'الشفافية',
        transparencyText: 'يمكن لجميع المواطنين عرض الطلبات المنجزة لضمان شفافية ومصداقية الخدمات العامة.',
        efficiency: 'الكفاءة',
        efficiencyText: 'يحسن نظام التخطيط الذكي لدينا استخدام الموارد ويقلل أوقات المعالجة.',
        techUsed: 'التقنيات المستخدمة'
      }
    };
  }
}
