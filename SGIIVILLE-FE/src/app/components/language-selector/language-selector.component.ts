import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslationService } from '../../services/translation.service';

@Component({
  selector: 'app-language-selector',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="language-selector">
      <button 
        class="lang-button"
        [class.active]="currentLanguage === 'fr'"
        (click)="setLanguage('fr')"
        title="Français">
        🇫🇷 FR
      </button>
      <button 
        class="lang-button"
        [class.active]="currentLanguage === 'en'"
        (click)="setLanguage('en')"
        title="English">
        🇬🇧 EN
      </button>
      <button 
        class="lang-button"
        [class.active]="currentLanguage === 'ar'"
        (click)="setLanguage('ar')"
        title="العربية">
        🇸🇦 AR
      </button>
    </div>
  `,
  styles: [`
    .language-selector {
      display: flex;
      gap: 8px;
      align-items: center;
      background: #f5f5f5;
      padding: 6px;
      border-radius: 6px;
      border: 1px solid #ddd;
    }

    .lang-button {
      padding: 6px 10px;
      border: 1px solid transparent;
      background: transparent;
      cursor: pointer;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;
      transition: all 0.3s ease;
      color: #666;
    }

    .lang-button:hover {
      background: #e0e0e0;
      color: #333;
    }

    .lang-button.active {
      background: #007bff;
      color: white;
      border-color: #0056b3;
      font-weight: 600;
    }

    /* Support RTL */
    [dir="rtl"] .language-selector {
      flex-direction: row-reverse;
    }
  `]
})
export class LanguageSelectorComponent implements OnInit {
  currentLanguage: string = 'fr';

  constructor(private translationService: TranslationService) {}

  ngOnInit(): void {
    this.currentLanguage = this.translationService.getCurrentLanguage();
    this.translationService.currentLanguage$.subscribe(lang => {
      this.currentLanguage = lang;
    });
  }

  setLanguage(language: string): void {
    this.translationService.setLanguage(language);
  }
}
