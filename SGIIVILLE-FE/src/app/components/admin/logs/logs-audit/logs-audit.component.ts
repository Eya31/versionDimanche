import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface AuditLog {
  id: number;
  userId: number;
  userName: string;
  action: string;
  entity: string;
  entityId: number;
  timestamp: Date;
  details: string;
}

@Component({
  selector: 'app-logs-audit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './logs-audit.component.html',
  styleUrls: ['./logs-audit.component.css']
})
export class LogsAuditComponent implements OnInit {
  logs: AuditLog[] = [];
  filteredLogs: AuditLog[] = [];
  loading = false;

  searchTerm: string = '';
  filterEntity: string = 'all';

  constructor() { }

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.loading = true;
    // TODO: Charger depuis l'API
    setTimeout(() => {
      this.logs = [];
      this.applyFilters();
      this.loading = false;
    }, 500);
  }

  applyFilters(): void {
    let filtered = [...this.logs];

    if (this.filterEntity !== 'all') {
      filtered = filtered.filter(l => l.entity === this.filterEntity);
    }

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(l =>
        l.userName.toLowerCase().includes(term) ||
        l.action.toLowerCase().includes(term) ||
        l.details.toLowerCase().includes(term)
      );
    }

    this.filteredLogs = filtered;
  }
}

