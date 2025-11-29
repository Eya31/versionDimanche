import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Role {
  name: string;
  description: string;
  permissions: string[];
  userCount: number;
}

@Component({
  selector: 'app-securite-roles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './securite-roles.component.html',
  styleUrls: ['./securite-roles.component.css']
})
export class SecuriteRolesComponent implements OnInit {
  roles: Role[] = [
    {
      name: 'ADMINISTRATEUR',
      description: 'Accès complet au système',
      permissions: ['*'],
      userCount: 0
    },
    {
      name: 'CHEF_SERVICE',
      description: 'Gestion des interventions et planification',
      permissions: ['interventions:read', 'interventions:write', 'demandes:read'],
      userCount: 0
    },
    {
      name: 'TECHNICIEN',
      description: 'Exécution des interventions',
      permissions: ['interventions:read', 'interventions:update'],
      userCount: 0
    },
    {
      name: 'CITOYEN',
      description: 'Soumission de demandes',
      permissions: ['demandes:create', 'demandes:read'],
      userCount: 0
    },
    {
      name: 'MAIN_DOEUVRE',
      description: 'Gestion des tâches d\'intervention',
      permissions: ['taches:read', 'taches:update'],
      userCount: 0
    }
  ];

  loading = false;

  constructor() { }

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(): void {
    this.loading = true;
    // TODO: Charger depuis l'API et compter les utilisateurs
    setTimeout(() => {
      this.loading = false;
    }, 500);
  }

  editRole(role: Role): void {
    // TODO: Ouvrir modal d'édition
    alert(`Édition du rôle ${role.name}`);
  }
}

