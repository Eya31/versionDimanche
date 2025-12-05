import { Pipe, PipeTransform, ChangeDetectorRef } from '@angular/core';
import { TranslationService } from '../services/translation.service';

@Pipe({
  name: 'translate',
  standalone: true,
  pure: false  // IMPORTANT: pure: false force le re-render à chaque changement
})
export class TranslatePipe implements PipeTransform {
  private lastLanguage: string = '';

  constructor(
    private translationService: TranslationService,
    private cdr: ChangeDetectorRef
  ) {}

  transform(key: string, defaultValue?: string): string {
    const currentLang = this.translationService.getCurrentLanguage();
    
    // Si la langue a changé, forcer le re-render
    if (currentLang !== this.lastLanguage) {
      console.log(`[TranslatePipe] Langue changée: ${this.lastLanguage} → ${currentLang} pour clé: ${key}`);
      this.lastLanguage = currentLang;
      this.cdr.markForCheck();
    }
    
    // Récupérer et retourner la traduction
    const result = this.translationService.translate(key, defaultValue);
    console.log(`[TranslatePipe] ${key} (${currentLang}): ${result}`);
    return result;
  }
}

