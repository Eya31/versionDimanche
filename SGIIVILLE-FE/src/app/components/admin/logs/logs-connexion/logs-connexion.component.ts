import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface LogEntry {
  id: number;
  userId: number;
  userName: string;
  action: string;
  timestamp: Date;
  ipAddress: string;
  status: 'success' | 'failed';
}

@Component({
  selector: 'app-logs-connexion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './logs-connexion.component.html',
  styleUrls: ['./logs-connexion.component.css']
})
export class LogsConnexionComponent implements OnInit {
  logs: LogEntry[] = [];
  filteredLogs: LogEntry[] = [];
  loading = false;

  // Filtres
  searchTerm: string = '';
  filterStatus: string = 'all';
  dateDebut: string = '';
  dateFin: string = '';

  // Pagination
  currentPage = 1;
  itemsPerPage = 50;

  constructor() { }

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.loading = true;
    // TODO: Charger depuis l'API
    setTimeout(() => {
      this.logs = [
        {
          id: 1,
          userId: 1,
          userName: 'Admin',
          action: 'Connexion',
          timestamp: new Date(),
          ipAddress: '192.168.1.1',
          status: 'success'
        }
      ];
      this.applyFilters();
      this.loading = false;
    }, 500);
  }

  applyFilters(): void {
    let filtered = [...this.logs];

    if (this.filterStatus !== 'all') {
      filtered = filtered.filter(l => l.status === this.filterStatus);
    }

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(l =>
        l.userName.toLowerCase().includes(term) ||
        l.ipAddress.includes(term)
      );
    }

    this.filteredLogs = filtered;
    this.currentPage = 1;
  }

  get paginatedLogs(): LogEntry[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredLogs.slice(start, start + this.itemsPerPage);
  }

  exportLogs(): void {
    // TODO: Exporter les logs
    alert('Export des logs en cours...');
  }
}

