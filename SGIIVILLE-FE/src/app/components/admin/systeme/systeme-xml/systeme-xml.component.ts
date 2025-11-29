import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-systeme-xml',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './systeme-xml.component.html',
  styleUrls: ['./systeme-xml.component.css']
})
export class SystemeXmlComponent implements OnInit {
  xmlFiles: any[] = [];
  loading = false;
  selectedFile: any = null;

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.loadXmlFiles();
  }

  loadXmlFiles(): void {
    this.loading = true;
    // Liste des fichiers XML du système
    this.xmlFiles = [
      { name: 'systeme-gestion-interventions.xml', size: '0 KB', lastModified: new Date() },
      { name: 'demandes.xml', size: '0 KB', lastModified: new Date() },
      { name: 'interventions.xml', size: '0 KB', lastModified: new Date() },
      { name: 'utilisateurs.xml', size: '0 KB', lastModified: new Date() },
      { name: 'techniciens.xml', size: '0 KB', lastModified: new Date() },
      { name: 'equipements.xml', size: '0 KB', lastModified: new Date() },
      { name: 'ressources.xml', size: '0 KB', lastModified: new Date() },
      { name: 'notifications.xml', size: '0 KB', lastModified: new Date() }
    ];
    this.loading = false;
  }

  validateXml(fileName: string): void {
    this.loading = true;
    // TODO: Appel API pour valider le XML
    setTimeout(() => {
      this.loading = false;
      alert(`Validation de ${fileName} : OK`);
    }, 1000);
  }

  exportXml(fileName: string): void {
    // TODO: Télécharger le fichier XML
    alert(`Export de ${fileName} en cours...`);
  }

  importXml(fileName: string): void {
    // TODO: Importer un fichier XML
    alert(`Import de ${fileName} en cours...`);
  }
}

