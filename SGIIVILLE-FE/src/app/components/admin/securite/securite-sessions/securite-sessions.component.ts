import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Session {
  id: string;
  userId: number;
  userName: string;
  ipAddress: string;
  userAgent: string;
  loginTime: Date;
  lastActivity: Date;
  active: boolean;
}

@Component({
  selector: 'app-securite-sessions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './securite-sessions.component.html',
  styleUrls: ['./securite-sessions.component.css']
})
export class SecuriteSessionsComponent implements OnInit {
  sessions: Session[] = [];
  loading = false;

  constructor() { }

  ngOnInit(): void {
    this.loadSessions();
  }

  loadSessions(): void {
    this.loading = true;
    // TODO: Charger depuis l'API
    setTimeout(() => {
      this.sessions = [];
      this.loading = false;
    }, 500);
  }

  terminateSession(session: Session): void {
    if (!confirm(`Terminer la session de ${session.userName} ?`)) {
      return;
    }
    // TODO: Terminer la session via l'API
    this.sessions = this.sessions.filter(s => s.id !== session.id);
  }

  terminateAllSessions(): void {
    if (!confirm('Terminer toutes les sessions actives ?')) {
      return;
    }
    // TODO: Terminer toutes les sessions
    this.sessions = [];
  }
}

