import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './faq.component.html',
  styleUrl: './faq.component.css'
})
export class FaqComponent {
  faqs = [
    {
      question: 'Comment soumettre une demande ?',
      answer: 'Pour soumettre une demande, vous devez d\'abord créer un compte citoyen. Une fois connecté, vous pouvez créer une nouvelle demande en fournissant une description, une localisation et éventuellement des photos.'
    },
    {
      question: 'Comment suivre mes demandes ?',
      answer: 'Une fois connecté à votre compte citoyen, vous pouvez accéder à votre tableau de bord qui affiche toutes vos demandes avec leur état actuel (soumise, en attente, traitée, rejetée).'
    },
    {
      question: 'Comment fonctionnent les interventions ?',
      answer: 'Une intervention est créée par un chef de service à partir d\'une demande citoyenne. Le chef définit les ressources nécessaires (techniciens, équipements, matériels) et planifie l\'intervention. Un technicien est ensuite assigné pour exécuter l\'intervention.'
    },
    {
      question: 'Quelles données sont utilisées ?',
      answer: 'Nous collectons uniquement les données nécessaires au traitement de votre demande : nom, email, adresse, téléphone. Ces données sont protégées conformément au RGPD et ne sont jamais partagées avec des tiers sans votre consentement.'
    },
    {
      question: 'Quelle est la différence entre une demande et une intervention ?',
      answer: 'Une demande est créée par un citoyen pour signaler un problème. Une intervention est créée par un chef de service à partir d\'une demande. Une demande peut générer une intervention, mais ce n\'est pas automatique.'
    },
    {
      question: 'Combien de temps faut-il pour traiter une demande ?',
      answer: 'Le temps de traitement dépend de la complexité de la demande et de la disponibilité des ressources. Les demandes urgentes sont traitées en priorité. Vous pouvez suivre l\'état de votre demande dans votre tableau de bord.'
    }
  ];

  expandedIndex: number | null = null;

  toggleFaq(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }
}

