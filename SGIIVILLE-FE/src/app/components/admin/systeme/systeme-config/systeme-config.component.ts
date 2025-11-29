import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-systeme-config',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './systeme-config.component.html',
  styleUrls: ['./systeme-config.component.css']
})
export class SystemeConfigComponent implements OnInit {
  config: any = {
    appName: 'SGII-Ville',
    maxFileSize: 200,
    sessionTimeout: 30,
    emailNotifications: true,
    smtpHost: 'smtp.gmail.com',
    smtpPort: 587
  };

  loading = false;
  saving = false;

  constructor() { }

  ngOnInit(): void {
    this.loadConfig();
  }

  loadConfig(): void {
    this.loading = true;
    // TODO: Charger depuis l'API
    setTimeout(() => {
      this.loading = false;
    }, 500);
  }

  saveConfig(): void {
    this.saving = true;
    // TODO: Sauvegarder via l'API
    setTimeout(() => {
      this.saving = false;
      alert('Configuration sauvegardée avec succès');
    }, 1000);
  }

  resetConfig(): void {
    if (confirm('Êtes-vous sûr de vouloir réinitialiser la configuration ?')) {
      this.loadConfig();
    }
  }
}

