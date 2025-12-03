import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TechnicienService } from '../../../services/technicien.service';
import { TacheService } from '../../../services/tache.service';
import { MainDOeuvreService } from '../../../services/main-doeuvre.service';
import { DemandeService } from '../../../services/demande.service';
import { Intervention } from '../../../models/intervention.model';
import { Tache } from '../../../models/tache.model';
import { MainDOeuvre } from '../../../models/main-doeuvre.model';
import { Demande } from '../../../models/demande.model';
import { RapportFinalRequest, RessourceUtilisee, EquipementUtilise } from '../../../models/rapport-final.model';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import jsPDF from 'jspdf';
import QRCode from 'qrcode';

@Component({
  selector: 'app-rapport-final',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './rapport-final.component.html',
  styleUrls: ['./rapport-final.component.css']
})
export class RapportFinalComponent implements OnInit {
  intervention: Intervention | null = null;
  demande: Demande | null = null;
  taches: Tache[] = [];
  mainDOeuvreMap: Map<number, MainDOeuvre> = new Map();
  technicienInfo: { nom: string; prenom: string } | null = null;
  chefServiceInfo: { nom: string; prenom: string } | null = null;
  rapport: RapportFinalRequest = {
    resultatObtenu: '',
    tempsTotalMinutes: 0,
    ressourcesUtilisees: [],
    equipementsUtilises: [],
    problemesRencontres: '',
    photoIds: [],
    signatureElectronique: '',
    commentairePersonnalise: '',
    analyseEtRecommandations: ''
  };

  nouvelleRessource: RessourceUtilisee = {
    ressourceId: 0,
    type: '',
    quantite: 0
  };

  nouvelEquipement: EquipementUtilise = {
    equipementId: 0,
    type: '',
    dureeUtilisationMinutes: 0
  };

  selectedFiles: File[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private technicienService: TechnicienService,
    private tacheService: TacheService,
    private mainDOeuvreService: MainDOeuvreService,
    private demandeService: DemandeService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadIntervention(+id);
    }
  }

  loadIntervention(id: number): void {
    // Charger l'intervention, les tâches et la demande en parallèle
    forkJoin({
      intervention: this.technicienService.getInterventionDetails(id),
      taches: this.tacheService.getByIntervention(id)
    }).subscribe({
      next: (data: { intervention: Intervention; taches: Tache[] }) => {
        this.intervention = data.intervention;
        this.taches = data.taches || [];
        
        if (data.intervention.tempsPasseMinutes) {
          this.rapport.tempsTotalMinutes = data.intervention.tempsPasseMinutes;
        }
        if (data.intervention.photoIds) {
          this.rapport.photoIds = data.intervention.photoIds;
        }
        
        // Charger la demande associée si disponible
        if (data.intervention.demandeId) {
          this.loadDemande(data.intervention.demandeId);
        }
        
        // Charger les informations du technicien et du chef de service
        this.loadTechnicienInfo(data.intervention.technicienId);
        this.loadChefServiceInfo(data.intervention.chefServiceId);
        
        // Charger les informations de main-d'œuvre pour chaque tâche
        this.loadMainDOeuvreForTaches();
      },
      error: (err: any) => {
        console.error('Erreur chargement intervention:', err);
        alert('Erreur lors du chargement des données');
      }
    });
  }

  loadDemande(demandeId: number): void {
    this.demandeService.getDemandeById(demandeId).pipe(
      catchError((err: any) => {
        console.error('Erreur chargement demande:', err);
        return of(null);
      })
    ).subscribe({
      next: (demande: Demande | null) => {
        this.demande = demande;
      }
    });
  }

  loadTechnicienInfo(technicienId?: number): void {
    // Charger le profil du technicien actuel (celui qui génère le rapport)
    this.technicienService.getProfil().pipe(
      catchError((err: any) => {
        console.error('Erreur chargement profil technicien:', err);
        return of(null);
      })
    ).subscribe({
      next: (profil: any) => {
        if (profil) {
          this.technicienInfo = { nom: profil.nom || '', prenom: profil.prenom || '' };
        }
      }
    });
  }

  loadChefServiceInfo(chefServiceId?: number): void {
    if (!chefServiceId) {
      return;
    }
    // TODO: Implémenter récupération du chef de service par ID
    // Pour l'instant, on laisse null
    this.chefServiceInfo = null;
  }

  loadMainDOeuvreForTaches(): void {
    const mainDOeuvreIds = new Set<number>();
    this.taches.forEach(tache => {
      if (tache.mainDOeuvreId) {
        mainDOeuvreIds.add(tache.mainDOeuvreId);
      }
    });

    if (mainDOeuvreIds.size === 0) {
      return;
    }

    const requests = Array.from(mainDOeuvreIds).map(id =>
      this.mainDOeuvreService.getById(id, false).pipe(
        catchError((err: any) => {
          console.error(`Erreur chargement main-d'œuvre ${id}:`, err);
          return of(null);
        })
      )
    );

    forkJoin(requests).subscribe({
      next: (results: (MainDOeuvre | null)[]) => {
        results.forEach((md, index) => {
          if (md) {
            const id = Array.from(mainDOeuvreIds)[index];
            this.mainDOeuvreMap.set(id, md);
          }
        });
      },
      error: (err: any) => {
        console.error('Erreur chargement main-d\'œuvre:', err);
      }
    });
  }

  getMainDOeuvreName(mainDOeuvreId?: number): string {
    if (!mainDOeuvreId) return 'Non assignée';
    const md = this.mainDOeuvreMap.get(mainDOeuvreId);
    if (!md) return `ID: ${mainDOeuvreId}`;
    return `${md.nom} ${md.prenom || ''}`.trim() || `ID: ${mainDOeuvreId}`;
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatDuree(minutes?: number): string {
    if (!minutes) return '0 min';
    const heures = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (heures > 0) {
      return `${heures}h ${mins}min`;
    }
    return `${mins}min`;
  }

  formatDateTime(date: Date): string {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year} à ${hours}:${minutes}`;
  }

  getCodificationOfficielle(interventionId: number): string {
    const year = new Date().getFullYear();
    const idStr = String(interventionId).padStart(6, '0');
    return `INT-${year}-${idStr}`;
  }

  async generateQRCodeData(interventionId: number): Promise<string> {
    const baseUrl = window.location.origin;
    const interventionUrl = `${baseUrl}/technicien/intervention/${interventionId}`;
    const timestamp = new Date().toISOString();
    const qrData = JSON.stringify({
      interventionId: interventionId,
      url: interventionUrl,
      timestamp: timestamp,
      codification: this.getCodificationOfficielle(interventionId)
    });
    
    try {
      const qrCodeDataUrl = await QRCode.toDataURL(qrData, {
        width: 150,
        margin: 2,
        color: {
          dark: '#000000',
          light: '#FFFFFF'
        }
      });
      return qrCodeDataUrl;
    } catch (error) {
      console.error('Erreur génération QR Code:', error);
      return '';
    }
  }

  calculateCoutTotal(): number {
    let coutTotal = this.intervention?.budget || 0;
    // Ajouter le coût de la main-d'œuvre (exemple: 50 DT/heure)
    const tauxHoraireMO = 50;
    const heuresMO = (this.rapport.tempsTotalMinutes || 0) / 60;
    coutTotal += heuresMO * tauxHoraireMO;
    return Math.round(coutTotal * 100) / 100;
  }

  async genererPDF(): Promise<void> {
    if (!this.intervention) {
      alert('Aucune intervention chargée');
      return;
    }

    const doc = new jsPDF();
    let yPosition = 20;
    const pageHeight = doc.internal.pageSize.height;
    const margin = 20;
    const maxWidth = doc.internal.pageSize.width - 2 * margin;

    // Fonction pour ajouter une nouvelle page si nécessaire
    const checkPageBreak = (requiredSpace: number = 20) => {
      if (yPosition + requiredSpace > pageHeight - margin) {
        doc.addPage();
        yPosition = 20;
      }
    };

    // Fonction utilitaire pour ajouter du texte avec gestion de page
    const addText = (text: string, x: number, y: number, options: any = {}): number => {
      checkPageBreak(8);
      const splitText = doc.splitTextToSize(text, maxWidth - (x - margin));
      splitText.forEach((line: string) => {
        if (y > pageHeight - margin) {
          doc.addPage();
          y = margin;
        }
        doc.text(line, x, y, options);
        y += 6;
      });
      return y;
    };

    // Générer le QR Code
    const qrCodeDataUrl = await this.generateQRCodeData(this.intervention.id);
    const codification = this.getCodificationOfficielle(this.intervention.id);

    // ========== PAGE DE GARDE ==========
    doc.setFillColor(59, 130, 246);
    doc.rect(0, 0, doc.internal.pageSize.width, 50, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(24);
    doc.setFont('helvetica', 'bold');
    doc.text('RAPPORT FINAL', margin, 30);
    doc.setFontSize(18);
    doc.text('D\'INTERVENTION', margin, 40);
    
    yPosition = 70;
    doc.setTextColor(0, 0, 0);
    doc.setFontSize(16);
    doc.setFont('helvetica', 'bold');
    doc.text(`Codification: ${codification}`, margin, yPosition);
    yPosition += 10;
    doc.setFontSize(14);
    doc.setFont('helvetica', 'normal');
    doc.text(`Intervention #${this.intervention.id}`, margin, yPosition);
    yPosition += 15;

    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    
    // Informations du technicien
    if (this.technicienInfo) {
      doc.setFont('helvetica', 'bold');
      doc.text('Technicien responsable:', margin, yPosition);
      doc.setFont('helvetica', 'normal');
      yPosition += 7;
      doc.text(`${this.technicienInfo.nom} ${this.technicienInfo.prenom}`.trim(), margin + 5, yPosition);
      yPosition += 10;
    }

    // Informations du chef de service
    if (this.chefServiceInfo) {
      doc.setFont('helvetica', 'bold');
      doc.text('Chef de service:', margin, yPosition);
      doc.setFont('helvetica', 'normal');
      yPosition += 7;
      doc.text(`${this.chefServiceInfo.nom} ${this.chefServiceInfo.prenom}`.trim(), margin + 5, yPosition);
      yPosition += 10;
    }

    doc.setFont('helvetica', 'normal');
    doc.text(`Date de génération: ${this.formatDateTime(new Date())}`, margin, yPosition);
    yPosition += 15;

    // Ajouter le QR Code
    if (qrCodeDataUrl) {
      try {
        doc.addImage(qrCodeDataUrl, 'PNG', doc.internal.pageSize.width - margin - 50, yPosition - 40, 50, 50);
      } catch (error) {
        console.error('Erreur ajout QR Code:', error);
      }
    }

    doc.addPage();
    yPosition = 20;

    // ========== 1. INFORMATIONS GÉNÉRALES DE L'INTERVENTION ==========
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(44, 62, 80);
    doc.text('1. INFORMATIONS GÉNÉRALES DE L\'INTERVENTION', margin, yPosition);
    yPosition += 12;

    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(0, 0, 0);
    
    const interventionDetails = [
      ['Identifiant:', `#${this.intervention.id}`],
      ['Description:', this.intervention.description || 'Non spécifiée'],
      ['Type d\'intervention:', this.intervention.typeIntervention || 'Non spécifié'],
      ['Priorité:', this.getPrioriteLabel(this.intervention.priorite)],
      ['État final:', this.getEtatLabel(this.intervention.etat)],
      ['Date planifiée:', this.formatDate(this.intervention.datePlanifiee)],
      ['Date de début:', this.formatDate(this.intervention.dateDebut)],
      ['Date de fin:', this.formatDate(this.intervention.dateFin)],
      ['Durée totale:', this.formatDuree(this.intervention.tempsPasseMinutes)],
      ['Budget alloué:', `${this.intervention.budget} DT`]
    ];

    interventionDetails.forEach(([label, value]) => {
      checkPageBreak(8);
      doc.setFont('helvetica', 'bold');
      doc.text(label, margin, yPosition);
      doc.setFont('helvetica', 'normal');
      const textLines = doc.splitTextToSize(value, maxWidth - 60);
      doc.text(textLines, margin + 60, yPosition);
      yPosition += textLines.length * 6 + 2;
    });

    // Localisation géographique
    if (this.intervention.localisation) {
      checkPageBreak(10);
      doc.setFont('helvetica', 'bold');
      doc.text('Localisation géographique:', margin, yPosition);
      yPosition += 6;
      doc.setFont('helvetica', 'normal');
      doc.text(`Latitude: ${this.intervention.localisation.latitude}`, margin + 5, yPosition);
      yPosition += 6;
      doc.text(`Longitude: ${this.intervention.localisation.longitude}`, margin + 5, yPosition);
      if (this.intervention.localisation.address) {
        yPosition += 6;
        doc.text(`Adresse: ${this.intervention.localisation.address}`, margin + 5, yPosition);
      }
      yPosition += 8;
    }

    // ========== 2. DÉTAILS DE LA DEMANDE ASSOCIÉE ==========
    if (this.demande) {
      checkPageBreak(30);
      yPosition += 5;
      doc.setFontSize(18);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(44, 62, 80);
      doc.text('2. DÉTAILS DE LA DEMANDE ASSOCIÉE', margin, yPosition);
      yPosition += 12;

      doc.setFontSize(11);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(0, 0, 0);

      const demandeDetails = [
        ['Identifiant demande:', `#${this.demande.id}`],
        ['Description du problème:', this.demande.description || 'Non spécifiée'],
        ['Date de soumission:', this.formatDate(this.demande.dateSoumission)],
        ['Adresse:', this.demande.address || (this.demande.localisation?.address) || 'Non spécifiée'],
        ['Catégorie:', this.demande.category || 'Non spécifiée'],
        ['Sous-catégorie:', this.demande.subCategory || 'Non spécifiée'],
        ['Priorité citoyenne:', this.demande.priority || 'Non spécifiée'],
        ['Signalement anonyme:', this.demande.isAnonymous ? 'Oui' : 'Non']
      ];

      demandeDetails.forEach(([label, value]) => {
        checkPageBreak(8);
        doc.setFont('helvetica', 'bold');
        doc.text(label, margin, yPosition);
        doc.setFont('helvetica', 'normal');
        const textLines = doc.splitTextToSize(value, maxWidth - 70);
        doc.text(textLines, margin + 70, yPosition);
        yPosition += textLines.length * 6 + 2;
      });

      // Photos initiales de la demande
      if (this.demande.photos && this.demande.photos.length > 0) {
        checkPageBreak(10);
        yPosition += 5;
        doc.setFont('helvetica', 'bold');
        doc.text('Photos initiales:', margin, yPosition);
        yPosition += 6;
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(10);
        this.demande.photos.forEach((photo, index) => {
          checkPageBreak(6);
          doc.text(`  • Photo ${index + 1}: ${photo.nom || `Photo #${photo.idPhoto}`}`, margin + 5, yPosition);
          yPosition += 6;
        });
        yPosition += 5;
      }
    }

    // ========== 3. LISTE DES TÂCHES RÉALISÉES ==========
    checkPageBreak(30);
    yPosition += 5;
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(44, 62, 80);
    doc.text('3. LISTE DES TÂCHES RÉALISÉES', margin, yPosition);
    yPosition += 12;

    if (this.taches.length === 0) {
      doc.setFontSize(11);
      doc.setFont('helvetica', 'italic');
      doc.text('Aucune tâche enregistrée pour cette intervention.', margin, yPosition);
      yPosition += 10;
    } else {
      this.taches.forEach((tache, index) => {
        checkPageBreak(50);
        doc.setFontSize(13);
        doc.setFont('helvetica', 'bold');
        doc.text(`3.${index + 1}. ${tache.libelle}`, margin, yPosition);
        yPosition += 10;

        doc.setFontSize(10);
        doc.setFont('helvetica', 'normal');
        const tacheDetails = [
          ['Description:', tache.description || 'Non spécifiée'],
          ['État:', this.getEtatTacheLabel(tache.etat)],
          ['Date de création:', this.formatDate(tache.dateCreation)],
          ['Date de début:', this.formatDate(tache.dateDebut)],
          ['Date de fin:', this.formatDate(tache.dateFin)],
          ['Durée totale:', this.formatDuree(tache.tempsPasseMinutes)],
          ['Ouvrier affecté:', this.getMainDOeuvreName(tache.mainDOeuvreId)],
          ['Commentaire technicien:', tache.commentaireTechnicien || 'Aucun'],
          ['Commentaire main-d\'œuvre:', tache.commentaireMainDOeuvre || 'Aucun']
        ];

        tacheDetails.forEach(([label, value]) => {
          checkPageBreak(8);
          doc.setFont('helvetica', 'bold');
          doc.text(`  ${label}`, margin + 5, yPosition);
          doc.setFont('helvetica', 'normal');
          const textLines = doc.splitTextToSize(value, maxWidth - 80);
          doc.text(textLines, margin + 50, yPosition);
          yPosition += textLines.length * 5 + 2;
        });
        yPosition += 8;
      });
    }

    // ========== 4. MAIN-D'ŒUVRE MOBILISÉE ==========
    checkPageBreak(30);
    yPosition += 5;
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(44, 62, 80);
    doc.text('4. MAIN-D\'ŒUVRE MOBILISÉE', margin, yPosition);
    yPosition += 12;

    // Collecter toutes les main-d'œuvre uniques avec leurs tâches
    const mainDOeuvreWithTasks = new Map<number, { mainDOeuvre: MainDOeuvre; taches: Tache[]; dureeTotale: number }>();
    
    this.taches.forEach(tache => {
      if (tache.mainDOeuvreId) {
        const md = this.mainDOeuvreMap.get(tache.mainDOeuvreId);
        if (md) {
          if (!mainDOeuvreWithTasks.has(tache.mainDOeuvreId)) {
            mainDOeuvreWithTasks.set(tache.mainDOeuvreId, {
              mainDOeuvre: md,
              taches: [],
              dureeTotale: 0
            });
          }
          const entry = mainDOeuvreWithTasks.get(tache.mainDOeuvreId)!;
          entry.taches.push(tache);
          entry.dureeTotale += tache.tempsPasseMinutes || 0;
        }
      }
    });

    if (mainDOeuvreWithTasks.size === 0) {
      doc.setFontSize(11);
      doc.setFont('helvetica', 'italic');
      doc.text('Aucune main-d\'œuvre mobilisée pour cette intervention.', margin, yPosition);
      yPosition += 10;
    } else {
      let index = 1;
      mainDOeuvreWithTasks.forEach((entry, mainDOeuvreId) => {
        const md = entry.mainDOeuvre;
        checkPageBreak(60);
        
        doc.setFontSize(12);
        doc.setFont('helvetica', 'bold');
        doc.text(`4.${index}. ${md.nom} ${md.prenom || ''}`.trim(), margin, yPosition);
        yPosition += 8;

        doc.setFontSize(10);
        doc.setFont('helvetica', 'normal');
        const mdDetails = [
          ['Matricule:', md.matricule || 'Non spécifié'],
          ['Compétence:', md.competence || 'Aucune'],
          ['Nombre de tâches:', `${entry.taches.length}`],
          ['Durée totale de travail:', this.formatDuree(entry.dureeTotale)]
        ];

        mdDetails.forEach(([label, value]) => {
          checkPageBreak(8);
          doc.setFont('helvetica', 'bold');
          doc.text(`  ${label}`, margin + 5, yPosition);
          doc.setFont('helvetica', 'normal');
          const textLines = doc.splitTextToSize(value, maxWidth - 80);
          doc.text(textLines, margin + 50, yPosition);
          yPosition += textLines.length * 5 + 2;
        });

        // Tâches assignées
        if (entry.taches.length > 0) {
          checkPageBreak(10);
          yPosition += 5;
          doc.setFont('helvetica', 'bold');
          doc.text('  Tâches assignées:', margin + 5, yPosition);
          yPosition += 6;
          doc.setFont('helvetica', 'normal');
          entry.taches.forEach((tache, tacheIndex) => {
            checkPageBreak(6);
            doc.text(`    • ${tache.libelle} (${this.formatDuree(tache.tempsPasseMinutes)})`, margin + 10, yPosition);
            yPosition += 6;
          });
        }

        yPosition += 8;
        index++;
      });
    }

    // ========== 5. RESSOURCES MATÉRIELLES UTILISÉES ==========
    checkPageBreak(30);
    yPosition += 5;
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(44, 62, 80);
    doc.text('5. RESSOURCES MATÉRIELLES UTILISÉES', margin, yPosition);
    yPosition += 12;

    if (this.rapport.ressourcesUtilisees.length === 0) {
      doc.setFontSize(11);
      doc.setFont('helvetica', 'italic');
      doc.text('Aucune ressource matérielle utilisée pour cette intervention.', margin, yPosition);
      yPosition += 10;
    } else {
      doc.setFontSize(10);
      this.rapport.ressourcesUtilisees.forEach((res, index) => {
        checkPageBreak(12);
        doc.setFont('helvetica', 'bold');
        doc.text(`5.${index + 1}. ${res.type}`, margin, yPosition);
        yPosition += 7;
        doc.setFont('helvetica', 'normal');
        doc.text(`  Quantité consommée: ${res.quantite}`, margin + 5, yPosition);
        yPosition += 6;
        if (res.reference) {
          doc.text(`  Référence: ${res.reference}`, margin + 5, yPosition);
          yPosition += 6;
        }
        if (res.numeroLot) {
          doc.text(`  Numéro de lot: ${res.numeroLot}`, margin + 5, yPosition);
          yPosition += 6;
        }
        yPosition += 3;
      });
    }

    // ========== 6. ÉQUIPEMENTS UTILISÉS ==========
    checkPageBreak(30);
    yPosition += 5;
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(44, 62, 80);
    doc.text('6. ÉQUIPEMENTS UTILISÉS', margin, yPosition);
    yPosition += 12;

    if (this.rapport.equipementsUtilises.length === 0) {
      doc.setFontSize(11);
      doc.setFont('helvetica', 'italic');
      doc.text('Aucun équipement utilisé pour cette intervention.', margin, yPosition);
      yPosition += 10;
    } else {
      doc.setFontSize(10);
      this.rapport.equipementsUtilises.forEach((eq, index) => {
        checkPageBreak(12);
        doc.setFont('helvetica', 'bold');
        doc.text(`6.${index + 1}. ${eq.type}`, margin, yPosition);
        yPosition += 7;
        doc.setFont('helvetica', 'normal');
        doc.text(`  Durée d'utilisation: ${this.formatDuree(eq.dureeUtilisationMinutes)}`, margin + 5, yPosition);
        yPosition += 6;
        if (eq.reference) {
          doc.text(`  Référence: ${eq.reference}`, margin + 5, yPosition);
          yPosition += 6;
        }
        yPosition += 3;
      });
    }

    // ========== 7. PREUVES VISUELLES (PHOTOS) ==========
    checkPageBreak(30);
    yPosition += 5;
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(44, 62, 80);
    doc.text('7. PREUVES VISUELLES (PHOTOS)', margin, yPosition);
    yPosition += 12;

    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    
    // Photos avant intervention (de la demande)
    if (this.demande && this.demande.photos && this.demande.photos.length > 0) {
      doc.setFont('helvetica', 'bold');
      doc.text('Photos avant intervention:', margin, yPosition);
      yPosition += 7;
      doc.setFont('helvetica', 'normal');
      doc.setFontSize(10);
      this.demande.photos.forEach((photo, index) => {
        checkPageBreak(6);
        doc.text(`  • ${photo.nom || `Photo avant ${index + 1}`} (ID: ${photo.idPhoto})`, margin + 5, yPosition);
        yPosition += 6;
      });
      yPosition += 5;
    }

    // Photos après intervention (du rapport)
    if (this.rapport.photoIds && this.rapport.photoIds.length > 0) {
      checkPageBreak(10);
      doc.setFont('helvetica', 'bold');
      doc.text('Photos après intervention:', margin, yPosition);
      yPosition += 7;
      doc.setFont('helvetica', 'normal');
      doc.setFontSize(10);
      this.rapport.photoIds.forEach((photoId, index) => {
        checkPageBreak(6);
        doc.text(`  • Photo après ${index + 1} (ID: ${photoId})`, margin + 5, yPosition);
        yPosition += 6;
      });
      yPosition += 5;
    }

    if ((!this.demande || !this.demande.photos || this.demande.photos.length === 0) && 
        (!this.rapport.photoIds || this.rapport.photoIds.length === 0)) {
      doc.setFont('helvetica', 'italic');
      doc.text('Aucune photo disponible pour cette intervention.', margin, yPosition);
      yPosition += 10;
    }

    // ========== 8. PROBLÈMES RENCONTRÉS ==========
    if (this.rapport.problemesRencontres && this.rapport.problemesRencontres.trim()) {
      checkPageBreak(30);
      yPosition += 5;
      doc.setFontSize(18);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(44, 62, 80);
      doc.text('8. PROBLÈMES RENCONTRÉS', margin, yPosition);
      yPosition += 12;

      doc.setFontSize(11);
      doc.setFont('helvetica', 'normal');
      const problemesLines = doc.splitTextToSize(this.rapport.problemesRencontres, maxWidth);
      problemesLines.forEach((line: string) => {
        checkPageBreak(8);
        doc.text(line, margin, yPosition);
        yPosition += 6;
      });
      yPosition += 5;
    }

    // ========== 9. RÉSULTAT OBTENU ==========
    checkPageBreak(30);
    yPosition += 5;
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(44, 62, 80);
    doc.text('9. RÉSULTAT OBTENU', margin, yPosition);
    yPosition += 12;

    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    const resultatLines = doc.splitTextToSize(this.rapport.resultatObtenu || 'Non spécifié', maxWidth);
    resultatLines.forEach((line: string) => {
      checkPageBreak(8);
      doc.text(line, margin, yPosition);
      yPosition += 6;
    });
    yPosition += 5;

    // ========== 10. COMMENTAIRE PERSONNALISÉ (TECHNICIEN) ==========
    if (this.rapport.commentairePersonnalise && this.rapport.commentairePersonnalise.trim()) {
      checkPageBreak(30);
      yPosition += 5;
      doc.setFontSize(18);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(44, 62, 80);
      doc.text('10. COMMENTAIRE PERSONNALISÉ (TECHNICIEN)', margin, yPosition);
      yPosition += 12;

      doc.setFontSize(11);
      doc.setFont('helvetica', 'normal');
      const commentaireLines = doc.splitTextToSize(this.rapport.commentairePersonnalise, maxWidth);
      commentaireLines.forEach((line: string) => {
        checkPageBreak(8);
        doc.text(line, margin, yPosition);
        yPosition += 6;
      });
      yPosition += 5;
    }

    // ========== 11. ANALYSE ET RECOMMANDATIONS ==========
    if (this.rapport.analyseEtRecommandations && this.rapport.analyseEtRecommandations.trim()) {
      checkPageBreak(30);
      yPosition += 5;
      doc.setFontSize(18);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(44, 62, 80);
      doc.text('11. ANALYSE ET RECOMMANDATIONS', margin, yPosition);
      yPosition += 12;

      doc.setFontSize(11);
      doc.setFont('helvetica', 'normal');
      const analyseLines = doc.splitTextToSize(this.rapport.analyseEtRecommandations, maxWidth);
      analyseLines.forEach((line: string) => {
        checkPageBreak(8);
        doc.text(line, margin, yPosition);
        yPosition += 6;
      });
      yPosition += 5;
    }

    // ========== 12. COÛT TOTAL ESTIMÉ ==========
    checkPageBreak(30);
    yPosition += 5;
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(44, 62, 80);
    doc.text('12. COÛT TOTAL ESTIMÉ', margin, yPosition);
    yPosition += 12;

    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    const coutTotal = this.calculateCoutTotal();
    const budgetInitial = this.intervention.budget || 0;
    const heuresMO = (this.rapport.tempsTotalMinutes || 0) / 60;
    const tauxHoraireMO = 50; // DT/heure

    doc.setFont('helvetica', 'bold');
    doc.text('Budget initial:', margin, yPosition);
    doc.setFont('helvetica', 'normal');
    doc.text(`${budgetInitial} DT`, margin + 60, yPosition);
    yPosition += 8;

    doc.setFont('helvetica', 'bold');
    doc.text('Coût main-d\'œuvre:', margin, yPosition);
    doc.setFont('helvetica', 'normal');
    doc.text(`${heuresMO.toFixed(2)} heures × ${tauxHoraireMO} DT/heure = ${(heuresMO * tauxHoraireMO).toFixed(2)} DT`, margin + 60, yPosition);
    yPosition += 8;

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(12);
    doc.text('COÛT TOTAL ESTIMÉ:', margin, yPosition);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(14);
    doc.text(`${coutTotal} DT`, margin + 80, yPosition);
    yPosition += 10;

    // ========== 13. VALIDATION ADMINISTRATIVE ==========
    checkPageBreak(40);
    yPosition += 5;
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(44, 62, 80);
    doc.text('13. VALIDATION ADMINISTRATIVE', margin, yPosition);
    yPosition += 15;

    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    
    // Case à cocher pour Chef de Service
    doc.rect(margin, yPosition, 5, 5);
    doc.setFont('helvetica', 'bold');
    doc.text('Validé par le Chef de Service', margin + 10, yPosition + 4);
    yPosition += 10;
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.text('Nom: _________________________', margin + 10, yPosition);
    yPosition += 6;
    doc.text('Date: _________________________', margin + 10, yPosition);
    yPosition += 6;
    doc.text('Signature: ____________________', margin + 10, yPosition);
    yPosition += 12;

    // Case à cocher pour Administrateur
    doc.setFontSize(11);
    doc.rect(margin, yPosition, 5, 5);
    doc.setFont('helvetica', 'bold');
    doc.text('Validé par l\'Administrateur', margin + 10, yPosition + 4);
    yPosition += 10;
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.text('Nom: _________________________', margin + 10, yPosition);
    yPosition += 6;
    doc.text('Date: _________________________', margin + 10, yPosition);
    yPosition += 6;
    doc.text('Signature: ____________________', margin + 10, yPosition);
    yPosition += 12;

    // ========== 14. SIGNATURE ÉLECTRONIQUE ==========
    checkPageBreak(40);
    yPosition += 10;
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(44, 62, 80);
    doc.text('14. SIGNATURE ÉLECTRONIQUE', margin, yPosition);
    yPosition += 12;

    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    doc.text('Nom et prénom:', margin, yPosition);
    yPosition += 8;
    doc.setFont('helvetica', 'bold');
    doc.text(this.rapport.signatureElectronique || 'Non signé', margin, yPosition);
    yPosition += 8;
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    doc.text(`Date de signature: ${this.formatDateTime(new Date())}`, margin, yPosition);
    yPosition += 8;
    doc.text(`Date de génération du rapport: ${this.formatDateTime(new Date())}`, margin, yPosition);

    // Télécharger le PDF
    const fileName = `rapport-intervention-${this.intervention.id}-${new Date().toISOString().split('T')[0]}.pdf`;
    doc.save(fileName);
  }

  getPrioriteLabel(priorite?: string): string {
    if (!priorite) return 'N/A';
    switch(priorite.toUpperCase()) {
      case 'URGENTE': return 'Urgente';
      case 'PLANIFIEE': return 'Planifiée';
      case 'NORMALE': return 'Normale';
      case 'CRITIQUE': return 'Critique';
      default: return priorite;
    }
  }

  getEtatLabel(etat?: string): string {
    if (!etat) return 'N/A';
    switch(etat.toUpperCase()) {
      case 'TERMINEE': return 'Terminée';
      case 'EN_COURS': return 'En cours';
      case 'SUSPENDUE': return 'Suspendue';
      case 'EN_ATTENTE': return 'En attente';
      case 'REPORTEE': return 'Reportée';
      default: return etat;
    }
  }

  getEtatTacheLabel(etat?: string): string {
    if (!etat) return 'N/A';
    switch(etat.toUpperCase()) {
      case 'TERMINEE': return 'Terminée';
      case 'VERIFIEE': return 'Vérifiée';
      case 'EN_COURS': return 'En cours';
      case 'EN_ATTENTE': return 'En attente';
      default: return etat;
    }
  }

  ajouterRessource(): void {
    if (this.nouvelleRessource.ressourceId && this.nouvelleRessource.quantite > 0) {
      this.rapport.ressourcesUtilisees.push({...this.nouvelleRessource});
      this.nouvelleRessource = { ressourceId: 0, type: '', quantite: 0 };
    }
  }

  supprimerRessource(index: number): void {
    this.rapport.ressourcesUtilisees.splice(index, 1);
  }

  ajouterEquipement(): void {
    if (this.nouvelEquipement.equipementId && this.nouvelEquipement.dureeUtilisationMinutes > 0) {
      this.rapport.equipementsUtilises.push({...this.nouvelEquipement});
      this.nouvelEquipement = { equipementId: 0, type: '', dureeUtilisationMinutes: 0 };
    }
  }

  supprimerEquipement(index: number): void {
    this.rapport.equipementsUtilises.splice(index, 1);
  }

  onFileSelected(event: any): void {
    this.selectedFiles = Array.from(event.target.files);
  }

  soumettreRapport(): void {
    if (!this.intervention) return;

    if (!this.rapport.resultatObtenu.trim()) {
      alert('Veuillez remplir le résultat obtenu');
      return;
    }

    if (confirm('Soumettre le rapport final et clôturer l\'intervention ?')) {
      this.technicienService.soumettreRapportFinal(this.intervention.id, this.rapport).subscribe({
        next: () => {
          alert('Rapport final soumis avec succès');
          this.router.navigate(['/technicien']);
        },
        error: (err: any) => alert('Erreur: ' + err.message)
      });
    }
  }

  retour(): void {
    if (this.intervention) {
      this.router.navigate(['/technicien/intervention', this.intervention.id]);
    }
  }
}

