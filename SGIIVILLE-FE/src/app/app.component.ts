import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LanguageSelectorComponent } from './components/language-selector/language-selector.component';
import { ChangeDetectionService } from './services/change-detection.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, LanguageSelectorComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'SGIIVILLE-FE';

  constructor(private changeDetectionService: ChangeDetectionService) {}

  ngOnInit(): void {
    // Le service de détection de changement est initialisé automatiquement
    // via le constructeur, mais on peut le référencer ici pour clarté
  }
}

