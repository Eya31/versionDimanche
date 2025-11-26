import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { DemandeListComponent } from './components/demande-list/demande-list.component';
import { InterventionListComponent } from './components/intervention-list/intervention-list.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { ChefDashboardComponent } from './components/chef-dashboard/chef-dashboard.component';        // ← déjà là
import { TechnicienDashboardComponent } from './components/technicien-dashboard/technicien-dashboard.component';
import { CitoyenDashboardComponent } from './components/citoyen-dashboard/citoyen-dashboard.component';
import { authGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'intervention-list', component: ChefDashboardComponent, canActivate: [authGuard] },

  // DASHBOARDS PAR RÔLE – LE PLUS IMPORTANT
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [authGuard, roleGuard(['ADMINISTRATEUR'])]
  },
  {
    path: 'chef',
    component: ChefDashboardComponent,
    canActivate: [authGuard, roleGuard(['CHEF_SERVICE'])]
  },
  // ROUTES TECHNICIEN AVEC LAYOUT COMMUN
  {
    path: 'technicien',
    loadComponent: () => import('./components/technicien-layout/technicien-layout.component').then(m => m.TechnicienLayoutComponent),
    canActivate: [authGuard, roleGuard(['TECHNICIEN'])],
    children: [
      {
        path: '',
        loadComponent: () => import('./components/technicien-dashboard/technicien-dashboard.component').then(m => m.TechnicienDashboardComponent)
      },
      // T2 - Tableau de Bord du Technicien
      {
        path: 'dashboard',
        loadComponent: () => import('./components/technicien-dashboard/technicien-dashboard.component').then(m => m.TechnicienDashboardComponent)
      },
      {
        path: 'T2',
        redirectTo: '',
        pathMatch: 'full'
      },
      // T3 - Gestion d'une Intervention
      {
        path: 'intervention/:id',
        loadComponent: () => import('./components/intervention-detail/intervention-detail.component').then(m => m.InterventionDetailComponent)
      },
      {
        path: 'T3/:id',
        loadComponent: () => import('./components/intervention-detail/intervention-detail.component').then(m => m.InterventionDetailComponent)
      },
      {
        path: 'intervention/:id/rapport',
        loadComponent: () => import('./components/rapport-final/rapport-final.component').then(m => m.RapportFinalComponent)
      },
      {
        path: 'profil',
        loadComponent: () => import('./components/technicien-profil/technicien-profil.component').then(m => m.TechnicienProfilComponent)
      },
      {
        path: 'main-doeuvre',
        loadComponent: () => import('./components/main-doeuvre-gestion/main-doeuvre-gestion.component').then(m => m.MainDOeuvreGestionComponent)
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
    loadComponent: () => import('./components/intervention-detail/intervention-detail.component').then(m => m.InterventionDetailComponent),
    canActivate: [authGuard, roleGuard(['MAIN_DOEUVRE'])]
  },

  // Routes classiques (gardées pour compatibilité)
  { path: 'demandes', component: DemandeListComponent, canActivate: [authGuard] },
  { path: 'interventions', component: InterventionListComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: '/login' }
];
