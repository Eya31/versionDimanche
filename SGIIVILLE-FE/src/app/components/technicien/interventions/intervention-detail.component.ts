import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { TechnicienService } from '../../../services/technicien.service';
import { MainDOeuvreService } from '../../../services/main-doeuvre.service';
import { MainDOeuvreAgentService } from '../../../services/main-doeuvre-agent.service';
import { TacheService } from '../../../services/tache.service';
import { MainDOeuvreTacheService } from '../../../services/main-doeuvre-tache.service';
import { AuthService } from '../../../services/auth.service';
import { Intervention } from '../../../models/intervention.model';
import { MainDOeuvre, isVerificationValide } from '../../../models/main-doeuvre.model';
import { UpdateEtatInterventionRequest } from '../../../models/update-etat.model';
import { Tache, CreateTacheRequest, AssignerTacheRequest, VerifierTacheRequest } from '../../../models/tache.model';
import { normalizeText, safeToLowerCase } from '../../../utils/string.utils';

@Component({
  selector: 'app-intervention-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './intervention-detail.component.html',
  styleUrls: ['./intervention-detail.component.css']
})
export class InterventionDetailComponent implements OnInit {
  intervention: Intervention | null = null;
  mainDOeuvreListe: MainDOeuvre[] = [];
  mainDOeuvreListeFiltree: MainDOeuvre[] = [];
  mainDOeuvreAffectee: MainDOeuvre[] = [];
  
  // Filtres pour la main-d'œuvre
  rechercheMainDOeuvre = '';
  filtreCompetenceMainDOeuvre = '';
  filtreDisponibiliteMainDOeuvre = '';
  filtreHabilitationMainDOeuvre = '';
  competencesDisponibles = ['Électricité', 'Hydraulique', 'Mécanique', 'Plomberie', 'Maçonnerie', 'Peinture', 'Télécom'];
  habilitationsDisponibles = ['Électrique', 'CACES', 'Habilitation H0', 'Habilitation H1', 'Habilitation H2', 'Travail en hauteur'];
  selectedMainDOeuvreIds: number[] = []; // Pour sélection multiple
  
  // Calendrier de disponibilité
  showAvailabilityCalendar = false;
  selectedMainDOeuvreForCalendar: MainDOeuvre | null = null;
  allInterventionsForCalendar: Intervention[] = [];
  
  showUpdateEtat = false;
  updateEtatRequest: UpdateEtatInterventionRequest = {
    nouvelEtat: 'EN_COURS',
    tempsPasseMinutes: 0,
    commentaire: ''
  };

  selectedFiles: File[] = [];
  nouveauCommentaire = '';

  // Gestion des tâches
  taches: Tache[] = [];
  showCreateTache = false;
  nouvelleTache: CreateTacheRequest = {
    libelle: '',
    description: '',
    mainDOeuvreId: undefined,
    ordre: undefined
  };
  selectedTacheForAssign: Tache | null = null;
  selectedTacheForVerify: Tache | null = null;
  verificationComment: string = '';
  verificationValidee: boolean = true;
  selectedMainDOeuvreForAssign: { [tacheId: number]: number } = {}; // Objet pour stocker la sélection par tâche
  
  // Pour la main-d'œuvre : terminer une tâche
  selectedTacheForTerminer: Tache | null = null;
  terminerTacheRequest: { commentaire: string; tempsPasseMinutes?: number } = {
    commentaire: '',
    tempsPasseMinutes: undefined
  };

  // Détection du rôle
  isMainDOeuvre: boolean = false;
  isTechnicien: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private technicienService: TechnicienService,
    private mainDOeuvreService: MainDOeuvreService,
    private mainDOeuvreAgentService: MainDOeuvreAgentService,
    private tacheService: TacheService,
    private mainDOeuvreTacheService: MainDOeuvreTacheService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    // Détecter le rôle de l'utilisateur
    const user = this.authService.currentUserValue;
    if (user) {
      this.isMainDOeuvre = user.role === 'MAIN_DOEUVRE';
      this.isTechnicien = user.role === 'TECHNICIEN';
    }
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadIntervention(+id);
      // Charger la liste de main-d'œuvre disponible uniquement pour les techniciens
      // (pour l'affectation directe lors de la création des tâches)
      if (this.isTechnicien) {
        this.loadMainDOeuvre();
      }
      this.loadAllInterventions();
    }
  }

  loadAllInterventions(): void {
    if (this.isMainDOeuvre) {
      // Pour la main-d'œuvre, on ne charge pas toutes les interventions (pas nécessaire)
      this.allInterventionsForCalendar = [];
      return;
    }
    
    this.technicienService.getMyInterventions({}).subscribe({
      next: (data: Intervention[]) => {
        this.allInterventionsForCalendar = data || [];
      },
      error: (err: any) => {
        console.error('Erreur chargement toutes interventions:', err);
        this.allInterventionsForCalendar = [];
      }
    });
  }

  loadIntervention(id: number): void {
    const service = this.isMainDOeuvre 
      ? this.mainDOeuvreAgentService.getInterventionDetails(id)
      : this.technicienService.getInterventionDetails(id);

    service.subscribe({
      next: (data: Intervention) => {
        this.intervention = data;
        console.log('Intervention chargée, mainDOeuvreIds:', data.mainDOeuvreIds);
        if (data.mainDOeuvreIds && data.mainDOeuvreIds.length > 0) {
          this.loadMainDOeuvreAffectee(data.mainDOeuvreIds);
        } else {
          // S'assurer que la liste est vide si pas de main-d'œuvre affectée
          this.mainDOeuvreAffectee = [];
          console.log('Aucune main-d\'œuvre affectée à cette intervention');
        }
        this.loadTaches(id);
      },
      error: (err: any) => {
        console.error('Erreur chargement intervention:', err);
        alert('Erreur lors du chargement de l\'intervention. Vérifiez que vous avez accès à cette intervention.');
      }
    });
  }

  loadTaches(interventionId: number): void {
    const service = this.isMainDOeuvre
      ? this.mainDOeuvreTacheService.getTachesByIntervention(interventionId)
      : this.tacheService.getByIntervention(interventionId);

    service.subscribe({
      next: (data: Tache[]) => {
        this.taches = data || [];
        // Trier par ordre
        this.taches.sort((a: Tache, b: Tache) => (a.ordre || 0) - (b.ordre || 0));
        // Re-filtrer la main-d'œuvre disponible après chargement des tâches
        if (this.isTechnicien) {
          this.filtrerMainDOeuvreDisponible();
        }
      },
      error: (err: any) => {
        console.error('Erreur chargement tâches:', err);
        this.taches = [];
      }
    });
  }

  loadMainDOeuvre(): void {
    // Charger toutes les main-d'œuvre actives (sans filtre de disponibilité)
    // Le filtrage par disponibilité se fera côté client dans filtrerMainDOeuvreDisponible()
    this.mainDOeuvreService.getAll().subscribe({
      next: (data: MainDOeuvre[]) => {
        this.mainDOeuvreListe = data || [];
        console.log(`✅ ${this.mainDOeuvreListe.length} main-d'œuvre chargées`);
        
        // Log de débogage : compter les main-d'œuvre DISPONIBLE
        const disponibles = this.mainDOeuvreListe.filter(md => {
          const disp = (md.disponibilite || 'LIBRE').trim().toUpperCase();
          return disp === 'LIBRE';
        });
        console.log(`📊 Main-d'œuvre LIBRE: ${disponibles.length}`, 
          disponibles.map(md => `${md.nom} ${md.prenom || ''} (ID: ${md.id}, disponibilite: "${md.disponibilite}")`));
        
        this.filtrerMainDOeuvreDisponible();
      },
      error: (err: any) => {
        console.error('Erreur chargement main-d\'œuvre:', err);
        this.mainDOeuvreListe = [];
        this.mainDOeuvreListeFiltree = [];
      }
    });
  }

  filtrerMainDOeuvreDisponible(): void {
    const rechercheLower = normalizeText(this.rechercheMainDOeuvre);
    this.mainDOeuvreListeFiltree = this.mainDOeuvreListe.filter(md => {
      // Filtrer par recherche (sécurisé)
      const matchRecherche = !rechercheLower || 
        normalizeText(md.nom).includes(rechercheLower) ||
        normalizeText(md.prenom).includes(rechercheLower) ||
        normalizeText(md.matricule).includes(rechercheLower) ||
        (md.competence ? normalizeText(md.competence).includes(rechercheLower) : false);
      
      // Filtrer par compétence
      const matchCompetence = !this.filtreCompetenceMainDOeuvre ||
        md.competence === this.filtreCompetenceMainDOeuvre;
      
      // Filtrer par disponibilité
      // Par défaut, n'inclure QUE LIBRE (pas OCCUPE, ARCHIVE, etc.)
      // Normaliser la comparaison (trim + uppercase pour éviter les problèmes de casse/espaces)
      const disponibilite = (md.disponibilite || 'LIBRE').trim().toUpperCase();
      let matchDisponibilite = true;
      if (this.filtreDisponibiliteMainDOeuvre) {
        matchDisponibilite = disponibilite === this.filtreDisponibiliteMainDOeuvre.trim().toUpperCase();
      } else {
        // Par défaut, n'inclure QUE les main-d'œuvre avec statut LIBRE
        matchDisponibilite = disponibilite === 'LIBRE';
      }
      
      // Filtrer par habilitation (supprimé car habilitations n'existe plus dans le modèle)
      const matchHabilitation = !this.filtreHabilitationMainDOeuvre;
      
      // Exclure ceux déjà assignés à une tâche de cette intervention
      // SAUF si la tâche est vérifiée (VERIFIEE ou verifiee === true)
      // Dans ce cas, la main-d'œuvre peut être réassignée à une nouvelle tâche
      const tachesNonVerifiees = this.taches.filter(t => 
        t.mainDOeuvreId === md.id && 
        t.etat !== 'VERIFIEE' && 
        !t.verifiee
      );
      const dejaAssignee = tachesNonVerifiees.length > 0;
      
      const result = matchRecherche && matchCompetence && matchDisponibilite && matchHabilitation && !dejaAssignee;
      
      // Log de débogage pour les main-d'œuvre LIBRE qui ne passent pas le filtre
      if (disponibilite === 'LIBRE' && !result) {
        const raisons = [];
        if (!matchRecherche) raisons.push(`❌ Ne correspond pas à la recherche: "${this.rechercheMainDOeuvre}"`);
        if (!matchCompetence) raisons.push(`❌ Ne correspond pas à la compétence: "${this.filtreCompetenceMainDOeuvre}"`);
        if (!matchDisponibilite) raisons.push(`❌ Ne correspond pas à la disponibilité: "${this.filtreDisponibiliteMainDOeuvre}"`);
        if (!matchHabilitation) raisons.push(`❌ Ne correspond pas à l'habilitation: "${this.filtreHabilitationMainDOeuvre}"`);
        if (dejaAssignee) {
          const tachesNonVerifiees = this.taches.filter(t => 
            t.mainDOeuvreId === md.id && 
            t.etat !== 'VERIFIEE' && 
            !t.verifiee
          );
          raisons.push(`❌ Déjà assignée à ${tachesNonVerifiees.length} tâche(s) non vérifiée(s) de cette intervention`);
        }
        
        console.log(`⚠️ Main-d'œuvre LIBRE exclue: ${md.nom} ${md.prenom || ''} (ID: ${md.id})`, {
          raisons: raisons.length > 0 ? raisons : ['Raison inconnue'],
          details: {
            disponibilite: md.disponibilite,
            matchRecherche,
            matchCompetence,
            matchDisponibilite,
            matchHabilitation,
            dejaAssignee,
            rechercheMainDOeuvre: this.rechercheMainDOeuvre,
            filtreCompetenceMainDOeuvre: this.filtreCompetenceMainDOeuvre,
            filtreHabilitationMainDOeuvre: this.filtreHabilitationMainDOeuvre,
            taches: this.taches.map(t => ({ 
              id: t.id, 
              mainDOeuvreId: t.mainDOeuvreId, 
              etat: t.etat, 
              verifiee: t.verifiee 
            }))
          }
        });
      }
      
      return result;
    });
    
    // Log détaillé pour débogage
    const disponibles = this.mainDOeuvreListe.filter(md => (md.disponibilite || 'LIBRE').trim().toUpperCase() === 'LIBRE');
    console.log(`🔍 Filtrage: ${this.mainDOeuvreListe.length} main-d'œuvre totales → ${disponibles.length} LIBRE → ${this.mainDOeuvreListeFiltree.length} disponibles après filtrage`);
  }

  reinitialiserFiltresMainDOeuvre(): void {
    this.rechercheMainDOeuvre = '';
    this.filtreCompetenceMainDOeuvre = '';
    this.filtreDisponibiliteMainDOeuvre = '';
    this.filtreHabilitationMainDOeuvre = '';
    this.filtrerMainDOeuvreDisponible();
  }

  toggleCreateTacheForm(): void {
    this.showCreateTache = !this.showCreateTache;
    if (this.showCreateTache) {
      // Réinitialiser les filtres quand on ouvre le formulaire
      this.reinitialiserFiltresMainDOeuvre();
      // S'assurer que la liste est chargée
      if (this.mainDOeuvreListe.length === 0) {
        this.loadMainDOeuvre();
      }
    }
  }

  loadMainDOeuvreAffectee(ids: number[]): void {
    this.mainDOeuvreAffectee = [];
    if (!ids || ids.length === 0) {
      console.log('Aucune main-d\'œuvre affectée');
      this.cdr.detectChanges();
      return;
    }
    
    console.log('Chargement main-d\'œuvre affectées, IDs:', ids);
    
    // Charger toutes les main-d'œuvre en parallèle avec forkJoin
    const requests = ids.map(id => 
      this.mainDOeuvreService.getById(id).pipe(
        catchError(err => {
          console.error('Erreur chargement main-d\'œuvre affectée ID ' + id + ':', err);
          return of(null); // Retourner null en cas d'erreur
        })
      )
    );
    
    forkJoin(requests).subscribe({
      next: (results: (MainDOeuvre | null)[]) => {
        // Filtrer les résultats null et les doublons
        this.mainDOeuvreAffectee = results
          .filter((md): md is MainDOeuvre => md !== null && md !== undefined)
          .filter((md, index, self) => 
            index === self.findIndex(m => m.id === md.id) // Éviter les doublons
          );
        
        console.log('Toutes les main-d\'œuvre affectées chargées:', this.mainDOeuvreAffectee.length);
        console.log('Main-d\'œuvre affectées:', this.mainDOeuvreAffectee.map(md => `${md.nom} ${md.prenom || ''} (ID: ${md.id})`));
        
        // Forcer la détection de changement
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des main-d\'œuvre affectées:', err);
        this.mainDOeuvreAffectee = [];
        this.cdr.detectChanges();
      }
    });
  }

  commencer(): void {
    if (this.intervention) {
      this.technicienService.commencerIntervention(this.intervention.id).subscribe({
        next: () => {
          alert('Intervention démarrée');
          this.loadIntervention(this.intervention!.id);
        },
        error: (err: any) => alert('Erreur: ' + err.message)
      });
    }
  }

  mettreEnPause(): void {
    if (this.intervention) {
      this.technicienService.mettreEnPause(this.intervention.id).subscribe({
        next: () => {
          alert('Intervention mise en pause');
          this.loadIntervention(this.intervention!.id);
        },
        error: (err: any) => alert('Erreur: ' + err.message)
      });
    }
  }

  reprendre(): void {
    if (this.intervention) {
      this.technicienService.reprendreIntervention(this.intervention.id).subscribe({
        next: () => {
          alert('Intervention reprise');
          this.loadIntervention(this.intervention!.id);
        },
        error: (err: any) => alert('Erreur: ' + err.message)
      });
    }
  }

  terminer(): void {
    if (this.intervention) {
      this.router.navigate(['/technicien/intervention', this.intervention.id, 'rapport']);
    }
  }

  onFileSelected(event: any): void {
    this.selectedFiles = Array.from(event.target.files);
  }

  ajouterPhotos(): void {
    if (this.intervention && this.selectedFiles.length > 0) {
      this.technicienService.ajouterPhotos(this.intervention.id, this.selectedFiles).subscribe({
        next: () => {
          alert('Photos ajoutées');
          this.selectedFiles = [];
          this.loadIntervention(this.intervention!.id);
        },
        error: (err: any) => alert('Erreur: ' + err.message)
      });
    }
  }

  ajouterCommentaire(): void {
    if (this.intervention && this.nouveauCommentaire.trim()) {
      this.technicienService.ajouterCommentaire(this.intervention.id, this.nouveauCommentaire).subscribe({
        next: () => {
          alert('Commentaire ajouté');
          this.nouveauCommentaire = '';
          this.loadIntervention(this.intervention!.id);
        },
        error: (err: any) => alert('Erreur: ' + err.message)
      });
    }
  }

  updateEtat(): void {
    if (this.intervention) {
      this.technicienService.updateEtat(this.intervention.id, this.updateEtatRequest).subscribe({
        next: () => {
          alert('État mis à jour');
          this.showUpdateEtat = false;
          this.loadIntervention(this.intervention!.id);
        },
        error: (err: any) => alert('Erreur: ' + err.message)
      });
    }
  }

  affecterMainDOeuvre(mainDOeuvreId: number): void {
    if (!this.intervention) return;
    
    // Trouver le membre dans la liste
    const membre = this.mainDOeuvreListe.find(md => md.id === mainDOeuvreId);
    if (!membre) {
      alert('Membre non trouvé');
      return;
    }

    // Vérifier avant affectation
    if (!this.intervention) return;
    
    this.mainDOeuvreService.verifierAffectation(this.intervention.id, mainDOeuvreId).subscribe({
      next: (verification: any) => {
        if (!this.intervention) return;
        
        let message = `Affecter ${membre.nom} ${membre.prenom || ''} à cette intervention ?\n\n`;
        
        if (!isVerificationValide(verification)) {
          // Afficher les erreurs
          message += '❌ Vérifications échouées :\n';
          verification.erreurs.forEach((erreur: string) => {
            message += `  • ${erreur}\n`;
          });
          
          // Afficher les avertissements
          if (verification.avertissements && verification.avertissements.length > 0) {
            message += '\n⚠️ Avertissements :\n';
            verification.avertissements.forEach((avert: string) => {
              message += `  • ${avert}\n`;
            });
          }
          
          message += '\nVoulez-vous quand même procéder à l\'affectation ?';
          
          if (!confirm(message)) {
            return;
          }
        } else {
          // Toutes les vérifications sont OK
          message += '✅ Toutes les vérifications sont OK :\n';
          message += '  • Disponibilité : OK\n';
          message += '  • Compétences : OK\n';
          message += '  • Habilitations : OK\n';
          message += '  • Pas de conflit : OK\n';
          message += '  • Horaires : OK\n';
          
          if (verification.avertissements && verification.avertissements.length > 0) {
            message += '\n⚠️ Avertissements :\n';
            verification.avertissements.forEach((avert: string) => {
              message += `  • ${avert}\n`;
            });
          }
          
          if (!confirm(message)) {
            return;
          }
        }
        
        // Procéder à l'affectation
        if (!this.intervention) return;
        
        const request = {
          ouvrierIds: [mainDOeuvreId],
          interventionId: this.intervention.id
        };
        
        this.technicienService.affecterMainDOeuvre(this.intervention.id, request).subscribe({
          next: (updatedIntervention: Intervention) => {
            console.log('✅ Réponse affectation reçue:', updatedIntervention);
            console.log('✅ mainDOeuvreIds dans réponse:', updatedIntervention?.mainDOeuvreIds);
            alert('✅ Main-d\'œuvre affectée avec succès !\n\nUne notification a été envoyée au chef de service.');
            
            // Mettre à jour immédiatement avec les données reçues
            if (updatedIntervention) {
              this.intervention = updatedIntervention;
              if (updatedIntervention.mainDOeuvreIds && updatedIntervention.mainDOeuvreIds.length > 0) {
                console.log('📋 Chargement immédiat des main-d\'œuvre affectées, IDs:', updatedIntervention.mainDOeuvreIds);
                this.loadMainDOeuvreAffectee(updatedIntervention.mainDOeuvreIds);
              }
            }
            
            // Recharger complètement l'intervention après un court délai pour être sûr d'avoir les données à jour
            setTimeout(() => {
              console.log('🔄 Rechargement complet de l\'intervention après affectation...');
              this.technicienService.getInterventionDetails(this.intervention!.id).subscribe({
                next: (reloadedIntervention: Intervention) => {
                  console.log('✅ Intervention rechargée après affectation:', reloadedIntervention);
                  console.log('✅ mainDOeuvreIds dans intervention rechargée:', reloadedIntervention.mainDOeuvreIds);
                  
                  this.intervention = reloadedIntervention;
                  
                  if (reloadedIntervention.mainDOeuvreIds && reloadedIntervention.mainDOeuvreIds.length > 0) {
                    console.log('📋 Rechargement des main-d\'œuvre affectées, IDs:', reloadedIntervention.mainDOeuvreIds);
                    this.loadMainDOeuvreAffectee(reloadedIntervention.mainDOeuvreIds);
                  } else {
                    console.log('⚠️ Aucun mainDOeuvreIds dans l\'intervention rechargée');
                    this.mainDOeuvreAffectee = [];
                    this.cdr.detectChanges();
                  }
                },
                error: (reloadErr: any) => {
                  console.error('❌ Erreur rechargement intervention:', reloadErr);
                }
              });
            }, 300); // Petit délai pour laisser le backend terminer
            
            this.loadMainDOeuvre(); // Recharger pour mettre à jour les disponibilités
          },
          error: (err: any) => {
            console.error('Erreur affectation:', err);
            let errorMessage = '❌ Erreur lors de l\'affectation';
            
            if (err.status === 400 && err.error?.erreurs) {
              errorMessage += '\n\nErreurs détectées :\n';
              err.error.erreurs.forEach((erreur: string) => {
                errorMessage += `  • ${erreur}\n`;
              });
            } else if (err.error?.message) {
              errorMessage += ': ' + err.error.message;
            }
            
            alert(errorMessage);
          }
        });
      },
      error: (err: any) => {
        console.error('Erreur vérification:', err);
        alert('❌ Erreur lors de la vérification : ' + (err.error?.message || err.message || 'Erreur inconnue'));
      }
    });
  }

  desaffecterMainDOeuvre(mainDOeuvreId: number): void {
    if (!this.intervention) return;
    
    const membre = this.mainDOeuvreAffectee.find(md => md.id === mainDOeuvreId);
    const nomMembre = membre ? `${membre.nom} ${membre.prenom || ''}` : 'ce membre';
    
    if (confirm(`Désaffecter ${nomMembre} de cette intervention ?\n\nLa disponibilité sera remise à "Disponible" et une notification sera envoyée au chef de service.`)) {
      this.technicienService.desaffecterMainDOeuvre(this.intervention.id, mainDOeuvreId).subscribe({
        next: (updatedIntervention: Intervention) => {
          console.log('✅ Réponse désaffectation reçue:', updatedIntervention);
          console.log('✅ mainDOeuvreIds dans réponse:', updatedIntervention?.mainDOeuvreIds);
          alert('✅ Main-d\'œuvre désaffectée avec succès');
          
          // Retirer immédiatement de la liste
          this.mainDOeuvreAffectee = this.mainDOeuvreAffectee.filter(md => md.id !== mainDOeuvreId);
          
          // Mettre à jour l'intervention avec les nouvelles données
          if (updatedIntervention) {
            this.intervention = updatedIntervention;
            if (updatedIntervention.mainDOeuvreIds && updatedIntervention.mainDOeuvreIds.length > 0) {
              // Recharger pour s'assurer que tout est à jour
              console.log('📋 Rechargement des main-d\'œuvre affectées après désaffectation, IDs:', updatedIntervention.mainDOeuvreIds);
              this.loadMainDOeuvreAffectee(updatedIntervention.mainDOeuvreIds);
            } else {
              // Aucune main-d'œuvre restante
              this.mainDOeuvreAffectee = [];
              this.cdr.detectChanges();
            }
          } else {
            // Recharger l'intervention si elle n'est pas retournée
            setTimeout(() => {
              this.loadIntervention(this.intervention!.id);
            }, 300);
          }
          this.loadMainDOeuvre(); // Recharger pour mettre à jour les disponibilités
        },
        error: (err: any) => {
          console.error('Erreur désaffectation:', err);
          alert('❌ Erreur lors de la désaffectation: ' + (err.error?.message || err.message || 'Erreur inconnue'));
        }
      });
    }
  }

  getDisponibiliteLabel(disponibilite: string): string {
    switch(disponibilite?.toUpperCase()) {
      case 'DISPONIBLE': return '✅ Disponible';
      case 'OCCUPE': return '🔄 Occupé';
      case 'CONFLIT': return '⚠️ Conflit d\'horaires';
      case 'EN_CONGE': return '🏖️ En congé';
      case 'ABSENT': return '🏥 Absent';
      case 'HORS_HABILITATION': return '⛔ Hors habilitation';
      default: return disponibilite || 'Inconnu';
    }
  }

  toggleSelectionMainDOeuvre(id: number): void {
    const index = this.selectedMainDOeuvreIds.indexOf(id);
    if (index > -1) {
      this.selectedMainDOeuvreIds.splice(index, 1);
    } else {
      this.selectedMainDOeuvreIds.push(id);
    }
  }

  isSelected(id: number): boolean {
    return this.selectedMainDOeuvreIds.includes(id);
  }

  affecterSelectionMultiple(): void {
    if (this.selectedMainDOeuvreIds.length === 0) {
      alert('⚠️ Veuillez sélectionner au moins un agent');
      return;
    }

    if (!this.intervention) return;

    const request = {
      ouvrierIds: this.selectedMainDOeuvreIds,
      interventionId: this.intervention.id
    };

    this.technicienService.affecterMainDOeuvre(this.intervention.id, request).subscribe({
      next: (updatedIntervention: Intervention) => {
        console.log('✅ Réponse affectation multiple reçue:', updatedIntervention);
        console.log('✅ mainDOeuvreIds dans réponse:', updatedIntervention?.mainDOeuvreIds);
        alert(`✅ ${this.selectedMainDOeuvreIds.length} agent(s) affecté(s) avec succès !\n\nUne notification a été envoyée au chef de service.`);
        this.selectedMainDOeuvreIds = [];
        
        // Mettre à jour immédiatement avec les données reçues
        if (updatedIntervention) {
          this.intervention = updatedIntervention;
          if (updatedIntervention.mainDOeuvreIds && updatedIntervention.mainDOeuvreIds.length > 0) {
            console.log('📋 Chargement immédiat des main-d\'œuvre affectées, IDs:', updatedIntervention.mainDOeuvreIds);
            this.loadMainDOeuvreAffectee(updatedIntervention.mainDOeuvreIds);
          }
        }
        
        // Recharger complètement l'intervention après un court délai
        setTimeout(() => {
          this.loadIntervention(this.intervention!.id);
        }, 300);
        
        this.loadMainDOeuvre(); // Recharger pour mettre à jour les disponibilités
      },
      error: (err: any) => {
        console.error('Erreur affectation multiple:', err);
        let errorMessage = '❌ Erreur lors de l\'affectation';
        if (err.status === 400 && err.error?.erreurs) {
          errorMessage += '\n\nErreurs détectées :\n';
          err.error.erreurs.forEach((erreur: string) => {
            errorMessage += `  • ${erreur}\n`;
          });
        } else if (err.error?.message) {
          errorMessage += ': ' + err.error.message;
        }
        alert(errorMessage);
      }
    });
  }

  creerNouvelleFiche(): void {
    this.router.navigate(['/technicien/main-doeuvre'], { 
      queryParams: { action: 'create' } 
    });
  }

  showCalendarForMainDOeuvre(mainDOeuvre: MainDOeuvre): void {
    this.selectedMainDOeuvreForCalendar = mainDOeuvre;
    this.showAvailabilityCalendar = true;
  }

  hideAvailabilityCalendar(): void {
    this.showAvailabilityCalendar = false;
    this.selectedMainDOeuvreForCalendar = null;
  }

  onSlotSelected(event: { date: Date; time: string }): void {
    if (this.selectedMainDOeuvreForCalendar) {
      alert(`Créneau sélectionné : ${event.date.toLocaleDateString('fr-FR')} à ${event.time}\n\nVous pouvez maintenant affecter ${this.selectedMainDOeuvreForCalendar.nom} à cette intervention.`);
    }
  }

  // ==================== GESTION DES TÂCHES ====================

  creerTache(): void {
    if (!this.intervention || !this.nouvelleTache.libelle) {
      alert('⚠️ Veuillez remplir au moins le libellé de la tâche');
      return;
    }

    if (!this.nouvelleTache.mainDOeuvreId) {
      alert('⚠️ Veuillez sélectionner une main-d\'œuvre disponible pour cette tâche');
      return;
    }

    const mainDOeuvreId = this.nouvelleTache.mainDOeuvreId;
    const mainDOeuvreName = this.getMainDOeuvreNameFromList(mainDOeuvreId);

    // Vérifier si la main-d'œuvre est déjà affectée à l'intervention
    const isAlreadyAffected = this.intervention.mainDOeuvreIds && 
                             this.intervention.mainDOeuvreIds.includes(mainDOeuvreId);

    // Fonction pour créer la tâche après l'affectation (si nécessaire)
    const createTask = () => {
      this.nouvelleTache.ordre = this.taches.length + 1;
      this.tacheService.create(this.intervention!.id, this.nouvelleTache).subscribe({
        next: () => {
          alert(`✅ Tâche créée avec succès et assignée à ${mainDOeuvreName} !\n\n${!isAlreadyAffected ? 'La main-d\'œuvre a été automatiquement affectée à l\'intervention.' : ''}`);
          this.showCreateTache = false;
          this.nouvelleTache = { libelle: '', description: '', mainDOeuvreId: undefined, ordre: undefined };
          
          // Recharger les données
          this.loadTaches(this.intervention!.id);
          this.loadIntervention(this.intervention!.id);
          this.loadMainDOeuvre();
        },
        error: (err: any) => {
          console.error('Erreur création tâche:', err);
          alert('❌ Erreur lors de la création de la tâche: ' + (err.error?.message || err.message));
        }
      });
    };

    // Si la main-d'œuvre n'est pas déjà affectée, l'affecter d'abord
    if (!isAlreadyAffected) {
      const request = {
        ouvrierIds: [mainDOeuvreId],
        interventionId: this.intervention.id
      };

      this.technicienService.affecterMainDOeuvre(this.intervention.id, request).subscribe({
        next: (updatedIntervention: Intervention) => {
          console.log('✅ Main-d\'œuvre affectée à l\'intervention:', updatedIntervention);
          
          // Mettre à jour l'intervention avec les nouvelles données
          if (updatedIntervention) {
            this.intervention = updatedIntervention;
          }
          
          // Créer la tâche maintenant que la main-d'œuvre est affectée
          createTask();
        },
        error: (err: any) => {
          console.error('Erreur affectation main-d\'œuvre:', err);
          let errorMessage = '❌ Erreur lors de l\'affectation de la main-d\'œuvre';
          
          if (err.status === 400 && err.error?.erreurs) {
            errorMessage += '\n\nErreurs détectées :\n';
            err.error.erreurs.forEach((erreur: string) => {
              errorMessage += `  • ${erreur}\n`;
            });
          } else if (err.error?.message) {
            errorMessage += ': ' + err.error.message;
          }
          
          alert(errorMessage);
        }
      });
    } else {
      // La main-d'œuvre est déjà affectée, créer directement la tâche
      createTask();
    }
  }

  getMainDOeuvreNameFromList(mainDOeuvreId: number): string {
    const md = this.mainDOeuvreListe.find(m => m.id === mainDOeuvreId);
    return md ? `${md.nom} ${md.prenom || ''}` : `ID: ${mainDOeuvreId}`;
  }

  assignerTache(tache: Tache, mainDOeuvreId?: number): void {
    const idToUse = mainDOeuvreId || this.selectedMainDOeuvreForAssign[tache.id];
    if (!idToUse) {
      alert('⚠️ Veuillez sélectionner une main-d\'œuvre');
      return;
    }

    if (!this.intervention) return;

    // Vérifier si la main-d'œuvre est déjà affectée à l'intervention
    const isAlreadyAffected = this.intervention.mainDOeuvreIds && 
                             this.intervention.mainDOeuvreIds.includes(idToUse);

    // Fonction pour assigner la tâche après l'affectation (si nécessaire)
    const assignTask = () => {
      const request: AssignerTacheRequest = { mainDOeuvreId: idToUse };
      this.tacheService.assigner(tache.id, request).subscribe({
        next: () => {
          const mainDOeuvreName = this.getMainDOeuvreNameFromList(idToUse);
          alert(`✅ Tâche assignée avec succès à ${mainDOeuvreName} !\n\n${!isAlreadyAffected ? 'La main-d\'œuvre a été automatiquement affectée à l\'intervention.' : ''}`);
          delete this.selectedMainDOeuvreForAssign[tache.id];
          
          // Recharger les données
          this.loadTaches(this.intervention!.id);
          this.loadIntervention(this.intervention!.id);
          this.loadMainDOeuvre();
        },
        error: (err: any) => {
          console.error('Erreur assignation tâche:', err);
          alert('❌ Erreur lors de l\'assignation: ' + (err.error?.message || err.message));
        }
      });
    };

    // Si la main-d'œuvre n'est pas déjà affectée, l'affecter d'abord
    if (!isAlreadyAffected) {
      const request = {
        ouvrierIds: [idToUse],
        interventionId: this.intervention.id
      };

      this.technicienService.affecterMainDOeuvre(this.intervention.id, request).subscribe({
        next: (updatedIntervention: Intervention) => {
          console.log('✅ Main-d\'œuvre affectée à l\'intervention:', updatedIntervention);
          
          // Mettre à jour l'intervention avec les nouvelles données
          if (updatedIntervention) {
            this.intervention = updatedIntervention;
          }
          
          // Assigner la tâche maintenant que la main-d'œuvre est affectée
          assignTask();
        },
        error: (err: any) => {
          console.error('Erreur affectation main-d\'œuvre:', err);
          let errorMessage = '❌ Erreur lors de l\'affectation de la main-d\'œuvre';
          
          if (err.status === 400 && err.error?.erreurs) {
            errorMessage += '\n\nErreurs détectées :\n';
            err.error.erreurs.forEach((erreur: string) => {
              errorMessage += `  • ${erreur}\n`;
            });
          } else if (err.error?.message) {
            errorMessage += ': ' + err.error.message;
          }
          
          alert(errorMessage);
        }
      });
    } else {
      // La main-d'œuvre est déjà affectée, assigner directement la tâche
      assignTask();
    }
  }

  ouvrirVerification(tache: Tache): void {
    this.selectedTacheForVerify = tache;
    this.verificationComment = '';
    this.verificationValidee = true;
  }

  verifierTache(): void {
    if (!this.selectedTacheForVerify) return;

    const request: VerifierTacheRequest = {
      commentaire: this.verificationComment,
      validee: this.verificationValidee
    };

    this.tacheService.verifier(this.selectedTacheForVerify.id, request).subscribe({
      next: () => {
        alert(this.verificationValidee ? '✅ Tâche vérifiée et validée !' : '⚠️ Tâche marquée à refaire');
        this.selectedTacheForVerify = null;
        this.loadTaches(this.intervention!.id);
      },
      error: (err: any) => {
        console.error('Erreur vérification tâche:', err);
        alert('❌ Erreur lors de la vérification: ' + (err.error?.message || err.message));
      }
    });
  }

  terminerIntervention(): void {
    if (!this.intervention || this.taches.length === 0) {
      alert('⚠️ Pas de tâches dans cette intervention');
      return;
    }

    // Vérifier que TOUTES les tâches sont terminées ET vérifiées
    const tachesNonTerminees = this.taches.filter(t => t.etat !== 'TERMINEE' && t.etat !== 'VERIFIEE');
    const tachesNonVerifiees = this.taches.filter(t => !t.verifiee);

    if (tachesNonTerminees.length > 0) {
      let message = `❌ Impossible de terminer l'intervention\n\n`;
      message += `${tachesNonTerminees.length} tâche(s) non terminée(s) :\n`;
      tachesNonTerminees.forEach(t => {
        message += `  • "${t.libelle}" - État: ${this.getEtatTacheLabel(t.etat)}\n`;
      });
      alert(message);
      return;
    }

    if (tachesNonVerifiees.length > 0) {
      let message = `❌ Impossible de terminer l'intervention\n\n`;
      message += `${tachesNonVerifiees.length} tâche(s) non vérifiée(s) :\n`;
      tachesNonVerifiees.forEach(t => {
        message += `  • "${t.libelle}"\n`;
      });
      message += `\nVeuillez vérifier toutes les tâches avant de terminer.`;
      alert(message);
      return;
    }

    // Toutes les vérifications sont OK
    if (!confirm('✅ Toutes les tâches sont terminées et vérifiées.\n\nVoulez-vous terminer cette intervention et passer au rapport final ?')) {
      return;
    }

    // Rediriger directement vers le rapport
    this.router.navigate(['/technicien/intervention', this.intervention.id, 'rapport']);
  }

  getEtatTacheLabel(etat: string): string {
    switch (etat) {
      case 'A_FAIRE': return '⏳ À faire';
      case 'EN_COURS': return '🔧 En cours';
      case 'TERMINEE': return '✅ Terminée';
      case 'VERIFIEE': return '✓ Vérifiée';
      default: return etat;
    }
  }

  getEtatTacheClass(etat: string): string {
    switch (etat) {
      case 'A_FAIRE': return 'etat-a-faire';
      case 'EN_COURS': return 'etat-en-cours';
      case 'TERMINEE': return 'etat-terminee';
      case 'VERIFIEE': return 'etat-verifiee';
      default: return '';
    }
  }

  getMainDOeuvreName(mainDOeuvreId: number | undefined): string {
    if (!mainDOeuvreId) return 'Non assignée';
    // Chercher d'abord dans la liste affectée (pour compatibilité)
    let md = this.mainDOeuvreAffectee.find(m => m.id === mainDOeuvreId);
    // Sinon chercher dans la liste complète
    if (!md) {
      md = this.mainDOeuvreListe.find(m => m.id === mainDOeuvreId);
    }
    return md ? `${md.nom} ${md.prenom || ''}` : `ID: ${mainDOeuvreId}`;
  }

  getSelectedMainDOeuvre(tacheId: number): number | undefined {
    return this.selectedMainDOeuvreForAssign[tacheId];
  }

  setSelectedMainDOeuvre(tacheId: number, mainDOeuvreId: number | undefined): void {
    if (mainDOeuvreId) {
      this.selectedMainDOeuvreForAssign[tacheId] = mainDOeuvreId;
    } else {
      delete this.selectedMainDOeuvreForAssign[tacheId];
    }
  }

  peutTerminerIntervention(): boolean {
    // Il faut au moins une tâche
    if (!this.intervention || this.taches.length === 0) return false;
    
    // TOUTES les tâches doivent être terminées (TERMINEE ou VERIFIEE)
    const toutesTerminees = this.taches.every(t => 
      t.etat === 'TERMINEE' || t.etat === 'VERIFIEE'
    );
    
    // TOUTES les tâches doivent être vérifiées
    const toutesVerifiees = this.taches.every(t => t.verifiee === true);
    
    return toutesTerminees && toutesVerifiees;
  }

  // ===== GETTERS POUR LE TEMPLATE - RÉSUMÉ DE TÂCHES =====
  
  get tachesNonTerminees(): Tache[] {
    return this.taches.filter(t => t.etat !== 'TERMINEE' && t.etat !== 'VERIFIEE');
  }

  get tachesNonVerifiees(): Tache[] {
    return this.taches.filter(t => !t.verifiee);
  }

  get tachesTerminees(): number {
    return this.taches.filter(t => t.etat === 'TERMINEE' || t.etat === 'VERIFIEE').length;
  }

  get tachesVerifiees(): number {
    return this.taches.filter(t => t.verifiee).length;
  }

  retour(): void {
    if (this.isMainDOeuvre) {
      this.router.navigate(['/main-doeuvre']);
    } else {
      this.router.navigate(['/technicien']);
    }
  }

  // ==================== MÉTHODES POUR MAIN-D'ŒUVRE ====================

  commencerTacheMainDOeuvre(tache: Tache): void {
    if (!confirm('Voulez-vous commencer cette tâche ?\n\nLe technicien sera notifié.')) {
      return;
    }

    this.mainDOeuvreTacheService.commencer(tache.id).subscribe({
      next: () => {
        alert('✅ Tâche commencée !\n\nLe technicien a été notifié.');
        if (tache.interventionId && this.intervention) {
          this.loadTaches(this.intervention.id);
        }
      },
      error: (err: any) => {
        console.error('Erreur début tâche:', err);
        alert('❌ Erreur lors du début de la tâche: ' + (err.error?.message || err.message));
      }
    });
  }

  ouvrirTerminerTacheMainDOeuvre(tache: Tache): void {
    this.selectedTacheForTerminer = tache;
    this.terminerTacheRequest = {
      commentaire: '',
      tempsPasseMinutes: undefined
    };
  }

  terminerTacheMainDOeuvre(): void {
    if (!this.selectedTacheForTerminer) return;

    if (!confirm('Voulez-vous marquer cette tâche comme terminée ?\n\nLe technicien devra vérifier votre travail.')) {
      return;
    }

    this.mainDOeuvreTacheService.terminer(this.selectedTacheForTerminer.id, this.terminerTacheRequest).subscribe({
      next: () => {
        alert('✅ Tâche marquée comme terminée !\n\nLe technicien a été notifié et va vérifier votre travail.');
        if (this.selectedTacheForTerminer!.interventionId && this.intervention) {
          this.loadTaches(this.intervention.id);
        }
        this.selectedTacheForTerminer = null;
      },
      error: (err: any) => {
        console.error('Erreur terminaison tâche:', err);
        alert('❌ Erreur lors de la terminaison: ' + (err.error?.message || err.message));
      }
    });
  }

  getBadgeClass(etat: string): string {
    switch(etat) {
      case 'TERMINEE': return 'badge-success';
      case 'EN_COURS': return 'badge-info';
      case 'SUSPENDUE': return 'badge-warning';
      case 'EN_ATTENTE': return 'badge-warning';
      case 'REPORTEE': return 'badge-secondary';
      default: return 'badge-secondary';
    }
  }

  getEtatLabel(etat: string): string {
    switch(etat) {
      case 'EN_ATTENTE': return 'En Attente';
      case 'EN_COURS': return 'En Cours';
      case 'SUSPENDUE': return 'Suspendue';
      case 'TERMINEE': return 'Terminée';
      case 'REPORTEE': return 'Reportée';
      default: return etat;
    }
  }

  getPrioriteLabel(priorite: string): string {
    switch(priorite?.toUpperCase()) {
      case 'URGENTE': return '🔴 Urgente';
      case 'CRITIQUE': return '🔴 Critique';
      case 'HAUTE': return '🟠 Haute';
      case 'MOYENNE': return '🟡 Moyenne';
      case 'PLANIFIEE': return '🟡 Planifiée';
      case 'BASSE': return '⚪ Basse';
      default: return '🟡 Normale';
    }
  }

  getPrioriteColor(priorite: string): string {
    switch(priorite?.toUpperCase()) {
      case 'URGENTE':
      case 'CRITIQUE':
        return '#F44336';
      case 'HAUTE':
        return '#FF9800';
      case 'MOYENNE':
      case 'PLANIFIEE':
        return '#2196F3';
      case 'BASSE':
        return '#9E9E9E';
      default:
        return '#2196F3';
    }
  }
}

