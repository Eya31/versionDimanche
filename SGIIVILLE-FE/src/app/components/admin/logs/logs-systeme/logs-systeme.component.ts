import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface SystemLog {
  id: number;
  level: 'info' | 'warning' | 'error';
  message: string;
  timestamp: Date;
  component: string;
}

@Component({
  selector: 'app-logs-systeme',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './logs-systeme.component.html',
  styleUrls: ['./logs-systeme.component.css']
})
export class LogsSystemeComponent implements OnInit {
  logs: SystemLog[] = [];
  filteredLogs: SystemLog[] = [];
  loading = false;

  filterLevel: string = 'all';
  searchTerm: string = '';

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

    if (this.filterLevel !== 'all') {
      filtered = filtered.filter(l => l.level === this.filterLevel);
    }

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(l =>
        l.message.toLowerCase().includes(term) ||
        l.component.toLowerCase().includes(term)
      );
    }

    this.filteredLogs = filtered;
  }
}

