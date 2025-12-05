import { Injectable, ApplicationRef, NgZone, ChangeDetectorRef, Injector } from '@angular/core';
import { TranslationService } from './translation.service';
import { Subscription } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ChangeDetectionService {
  private subscription: Subscription | null = null;

  constructor(
    private applicationRef: ApplicationRef,
    private ngZone: NgZone,
    private translationService: TranslationService
  ) {
    this.setupLanguageChangeDetection();
  }

  private setupLanguageChangeDetection(): void {
    // S'abonner aux changements de langue
    this.subscription = this.translationService.currentLanguage$.subscribe((lang: string) => {
      console.log(`[ChangeDetectionService] Détecté changement de langue à: ${lang}`);
      
      // Forcer la détection de changement de plusieurs manières
      // Méthode 1: applicationRef.tick()
      this.ngZone.run(() => {
        console.log(`[ChangeDetectionService] Appelant applicationRef.tick()`);
        this.applicationRef.tick();
      });
      
      // Méthode 2: Marquer tous les views pour vérification
      // (En cas de OnPush, nous pourrions avoir besoin de ceci)
      setTimeout(() => {
        this.applicationRef.tick();
      }, 0);
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
