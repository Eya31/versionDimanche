import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-systeme-backup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './systeme-backup.component.html',
  styleUrls: ['./systeme-backup.component.css']
})
export class SystemeBackupComponent implements OnInit {
  backups: any[] = [];
  loading = false;
  creating = false;

  backupConfig = {
    autoBackup: true,
    backupFrequency: 'daily', // daily, weekly, monthly
    retentionDays: 30
  };

  constructor() { }

  ngOnInit(): void {
    this.loadBackups();
  }

  loadBackups(): void {
    this.loading = true;
    // TODO: Charger depuis l'API
    setTimeout(() => {
      this.backups = [
        { 
          id: 1, 
          date: new Date('2024-12-01'), 
          size: '15.2 MB', 
          status: 'completed',
          type: 'automatique'
        },
        { 
          id: 2, 
          date: new Date('2024-11-30'), 
          size: '14.8 MB', 
          status: 'completed',
          type: 'manuelle'
        }
      ];
      this.loading = false;
    }, 500);
  }

  createBackup(): void {
    this.creating = true;
    // TODO: Créer un backup via l'API
    setTimeout(() => {
      this.creating = false;
      alert('Sauvegarde créée avec succès');
      this.loadBackups();
    }, 2000);
  }

  restoreBackup(backup: any): void {
    if (!confirm(`Êtes-vous sûr de vouloir restaurer la sauvegarde du ${backup.date.toLocaleDateString('fr-FR')} ?`)) {
      return;
    }
    // TODO: Restaurer via l'API
    alert('Restauration en cours...');
  }

  deleteBackup(backup: any): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette sauvegarde ?')) {
      return;
    }
    // TODO: Supprimer via l'API
    this.backups = this.backups.filter(b => b.id !== backup.id);
  }

  downloadBackup(backup: any): void {
    // TODO: Télécharger le backup
    alert(`Téléchargement de la sauvegarde du ${backup.date.toLocaleDateString('fr-FR')}...`);
  }
}

