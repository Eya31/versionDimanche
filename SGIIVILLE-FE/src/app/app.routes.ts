import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { DemandeListComponent } from './components/demande-list/demande-list.component';
import { InterventionListComponent } from './components/intervention-list/intervention-list.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { ChefDashboardComponent } from './components/chef-dashboard/chef-dashboard.component';        // ← déjà là
// Technicien components moved to technicien folder
import { CitoyenDashboardComponent } from './components/citoyen-dashboard/citoyen-dashboard.component';
import { VisiteurHomeComponent } from './components/visiteur/visiteur-home/visiteur-home.component';
import { authGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  // ROUTES PUBLIQUES (Interface Visiteur)
  { path: '', component: VisiteurHomeComponent },
  { path: 'demandes-terminees', loadComponent: () => import('./components/visiteur/demandes-terminees/demandes-terminees.component').then(m => m.DemandesTermineesComponent) },
  { path: 'demande/:id', loadComponent: () => import('./components/visiteur/demande-detail-public/demande-detail-public.component').then(m => m.DemandeDetailPublicComponent) },
  { path: 'faq', loadComponent: () => import('./components/visiteur/faq/faq.component').then(m => m.FaqComponent) },
  { path: 'about', loadComponent: () => import('./components/visiteur/about/about.component').then(m => m.AboutComponent) },
  { path: 'municipalite-inscription', loadComponent: () => import('./components/visiteur/municipalite-inscription/municipalite-inscription.component').then(m => m.MunicipaliteInscriptionComponent) },
  
  // ROUTES AUTHENTIFICATION
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'intervention-list', component: ChefDashboardComponent, canActivate: [authGuard] },

  // DASHBOARDS PAR RÔLE – LE PLUS IMPORTANT
  {
    path: 'admin',
    loadComponent: () => import('./components/admin/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    canActivate: [authGuard, roleGuard(['ADMINISTRATEUR'])],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./components/admin/admin-overview/admin-overview.component').then(m => m.AdminOverviewComponent)
      },
      // Interventions routes
      {
        path: 'interventions/carte',
        loadComponent: () => import('./components/admin/interventions/intervention-carte/intervention-carte.component').then(m => m.InterventionCarteComponent)
      },
      {
        path: 'interventions/liste',
        loadComponent: () => import('./components/admin/interventions/intervention-liste/intervention-liste.component').then(m => m.InterventionListeComponent)
      },
      {
        path: 'interventions/stats',
        loadComponent: () => import('./components/admin/interventions/intervention-stats/intervention-stats.component').then(m => m.InterventionStatsComponent)
      },
      {
        path: 'interventions/:id',
        loadComponent: () => import('./components/admin/interventions/intervention-detail/intervention-detail.component').then(m => m.InterventionDetailComponent)
      },
      // Users routes
      {
        path: 'users',
        loadComponent: () => import('./components/admin/users/users-list/users-list.component').then(m => m.UsersListComponent)
      },
      {
        path: 'users/techniciens',
        loadComponent: () => import('./components/admin/users/techniciens-list/techniciens-list.component').then(m => m.TechniciensListComponent)
      },
      {
        path: 'users/chefs',
        loadComponent: () => import('./components/admin/users/chefs-list/chefs-list.component').then(m => m.ChefsListComponent)
      },
      {
        path: 'users/add',
        loadComponent: () => import('./components/admin/users/user-form/user-form.component').then(m => m.UserFormComponent)
      },
      {
        path: 'users/:id/edit',
        loadComponent: () => import('./components/admin/users/user-edit/user-edit.component').then(m => m.UserEditComponent)
      },
      // Main d'œuvre routes
      {
        path: 'main-doeuvre',
        loadComponent: () => import('./components/admin/main-doeuvre/main-doeuvre-list/main-doeuvre-list.component').then(m => m.MainDoeuvreListComponent)
      },
      {
        path: 'main-doeuvre/add',
        loadComponent: () => import('./components/admin/main-doeuvre/main-doeuvre-form/main-doeuvre-form.component').then(m => m.MainDoeuvreFormComponent)
      },
      {
        path: 'main-doeuvre/:id',
        loadComponent: () => import('./components/admin/main-doeuvre/main-doeuvre-detail/main-doeuvre-detail.component').then(m => m.MainDoeuvreDetailComponent)
      },
      // Notifications routes
      {
        path: 'notifications/unread',
        loadComponent: () => import('./components/admin/notifications/notifications-unread/notifications-unread.component').then(m => m.NotificationsUnreadComponent)
      },
      {
        path: 'notifications/history',
        loadComponent: () => import('./components/admin/notifications/notifications-history/notifications-history.component').then(m => m.NotificationsHistoryComponent)
      },
      // Statistiques
      {
        path: 'statistiques',
        loadComponent: () => import('./components/admin/statistiques/statistiques.component').then(m => m.StatistiquesComponent)
      },
      // Système
      {
        path: 'systeme/config',
        loadComponent: () => import('./components/admin/systeme/systeme-config/systeme-config.component').then(m => m.SystemeConfigComponent)
      },
      {
        path: 'systeme/xml',
        loadComponent: () => import('./components/admin/systeme/systeme-xml/systeme-xml.component').then(m => m.SystemeXmlComponent)
      },
      {
        path: 'systeme/backup',
        loadComponent: () => import('./components/admin/systeme/systeme-backup/systeme-backup.component').then(m => m.SystemeBackupComponent)
      },
      // Logs
      {
        path: 'logs/connexion',
        loadComponent: () => import('./components/admin/logs/logs-connexion/logs-connexion.component').then(m => m.LogsConnexionComponent)
      },
      {
        path: 'logs/audit',
        loadComponent: () => import('./components/admin/logs/logs-audit/logs-audit.component').then(m => m.LogsAuditComponent)
      },
      {
        path: 'logs/systeme',
        loadComponent: () => import('./components/admin/logs/logs-systeme/logs-systeme.component').then(m => m.LogsSystemeComponent)
      },
      // Sécurité
      {
        path: 'securite/sessions',
        loadComponent: () => import('./components/admin/securite/securite-sessions/securite-sessions.component').then(m => m.SecuriteSessionsComponent)
      },
      {
        path: 'securite/roles',
        loadComponent: () => import('./components/admin/securite/securite-roles/securite-roles.component').then(m => m.SecuriteRolesComponent)
      },
      // Matériel & Stocks
      {
        path: 'materiel/stocks',
        loadComponent: () => import('./components/admin/materiel/materiel-stocks/materiel-stocks.component').then(m => m.MaterielStocksComponent)
      },
      {
        path: 'materiel/demandes',
        loadComponent: () => import('./components/admin/materiel/demandes-materiel/demandes-materiel.component').then(m => m.DemandesMaterielComponent)
      },
      // Analytics & KPIs
      {
        path: 'analytics',
        loadComponent: () => import('./components/admin/analytics/analytics-kpis/analytics-kpis.component').then(m => m.AnalyticsKpisComponent)
      }
    ]
  },

  {
    path: 'chef',
    component: ChefDashboardComponent,
    canActivate: [authGuard, roleGuard(['CHEF_SERVICE'])]
  },
  // ROUTES TECHNICIEN AVEC LAYOUT COMMUN
  {
    path: 'technicien',
    loadComponent: () => import('./components/technicien/technicien-layout/technicien-layout.component').then(m => m.TechnicienLayoutComponent),
    canActivate: [authGuard, roleGuard(['TECHNICIEN'])],
    children: [
      {
        path: '',
        loadComponent: () => import('./components/technicien/technicien-dashboard/technicien-dashboard.component').then(m => m.TechnicienDashboardComponent)
      },
      // T2 - Tableau de Bord du Technicien
      {
        path: 'dashboard',
        loadComponent: () => import('./components/technicien/technicien-dashboard/technicien-dashboard.component').then(m => m.TechnicienDashboardComponent)
      },
      {
        path: 'T2',
        redirectTo: '',
        pathMatch: 'full'
      },
      // T3 - Gestion d'une Intervention
      {
        path: 'intervention/:id',
        loadComponent: () => import('./components/technicien/interventions/intervention-detail.component').then(m => m.InterventionDetailComponent)
      },
      {
        path: 'T3/:id',
        loadComponent: () => import('./components/technicien/interventions/intervention-detail.component').then(m => m.InterventionDetailComponent)
      },
      {
        path: 'intervention/:id/rapport',
        loadComponent: () => import('./components/technicien/interventions/rapport-final.component').then(m => m.RapportFinalComponent)
      },
      {
        path: 'profil',
        loadComponent: () => import('./components/technicien/technicien-profil/technicien-profil.component').then(m => m.TechnicienProfilComponent)
      },
      {
        path: 'main-doeuvre',
        loadComponent: () => import('./components/technicien/main-doeuvre/main-doeuvre-gestion.component').then(m => m.MainDOeuvreGestionComponent)
      },
      // Mes interventions
      {
        path: 'interventions',
        loadComponent: () => import('./components/technicien/interventions/interventions-liste/interventions-liste.component').then(m => m.InterventionsListeComponent)
      },
      // Interventions en cours
      {
        path: 'interventions/en-cours',
        loadComponent: () => import('./components/technicien/interventions/interventions-en-cours/interventions-en-cours.component').then(m => m.InterventionsEnCoursComponent)
      },
      // Rapports finaux
      {
        path: 'rapports-finaux',
        loadComponent: () => import('./components/technicien/interventions/rapports-finaux/rapports-finaux.component').then(m => m.RapportsFinauxComponent)
      }
    ]
  },
  {
    path: 'citoyen',
    // ou 'dashboard' si tu préfères
    component: CitoyenDashboardComponent,
    canActivate: [authGuard, roleGuard(['CITOYEN'])]
  },
  // ROUTES MAIN-D'ŒUVRE
  {
    path: 'main-doeuvre',
    loadComponent: () => import('./components/main-doeuvre-dashboard/main-doeuvre-dashboard.component').then(m => m.MainDOeuvreDashboardComponent),
    canActivate: [authGuard, roleGuard(['MAIN_DOEUVRE'])]
  },
  {
    path: 'main-doeuvre/intervention/:id',
    loadComponent: () => import('./components/technicien/interventions/intervention-detail.component').then(m => m.InterventionDetailComponent),
    canActivate: [authGuard, roleGuard(['MAIN_DOEUVRE'])]
  },

  // Routes classiques (gardées pour compatibilité)
  { path: 'demandes', component: DemandeListComponent, canActivate: [authGuard] },
  { path: 'interventions', component: InterventionListComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: '/' }
];
