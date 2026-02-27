import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <!-- Hero Section -->
    <section class="hero-premium mb-5 overflow-hidden position-relative rounded-5">
      <div class="container py-5 position-relative style-z-1">
        <div class="row align-items-center py-5">
          <div class="col-lg-6 mb-5 mb-lg-0 animate__animated animate__fadeInLeft">
            <h1 class="display-3 fw-bold text-white mb-4">
              Bienvenue sur <span class="text-warning">RIMBestStay</span>
            </h1>
            <p class="lead text-white-50 mb-4 fs-4">
              Explorez le Maroc d'une manière unique. Trouvez les meilleurs hôtels, comparez les offres et réservez votre prochain séjour en quelques clics.
            </p>
            <div class="d-flex flex-wrap gap-3 mt-5">
              <a *ngIf="!isLoggedIn()" routerLink="/inscription" class="btn btn-warning btn-lg px-5 py-3 rounded-pill fw-bold shadow-lg">
                <i class="bi bi-person-plus me-2"></i>Créer un compte
              </a>
              <a *ngIf="isLoggedIn()" routerLink="/hotel/list" class="btn btn-warning btn-lg px-5 py-3 rounded-pill fw-bold shadow-lg">
                <i class="bi bi-search me-2"></i>Explorer les Hôtels
              </a>
              <a *ngIf="!isLoggedIn()" routerLink="/login" class="btn btn-outline-light btn-lg px-5 py-3 rounded-pill fw-bold border-2">
                <i class="bi bi-box-arrow-in-right me-2"></i>Se connecter
              </a>
            </div>
          </div>
          <div class="col-lg-6 d-none d-lg-block animate__animated animate__fadeInRight">
             <div class="hero-image-wrap ps-lg-5">
                <img src="assets/images/hero-building.png" alt="Luxury Hotel" class="img-fluid rounded-4 shadow-2xl floating-anim" onerror="this.src='https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80'">
             </div>
          </div>
        </div>
      </div>
      <!-- Background Abstract Shapes -->
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
    </section>

    <!-- Why Us / Features -->
    <section class="py-5 mb-5">
      <div class="container text-center mb-5">
        <h6 class="text-primary fw-bold text-uppercase ls-wide">Pourquoi choisir RIMBestStay ?</h6>
        <h2 class="display-5 fw-bold mt-2">Une Expérience de Réservation Inégalée</h2>
      </div>
      <div class="container">
        <div class="row g-4">
          <div class="col-md-4 animate__animated animate__fadeInUp" style="animation-delay: 0.1s">
            <div class="card h-100 border-0 shadow-soft p-4 rounded-4 transition-hover">
              <div class="bg-primary bg-opacity-10 p-3 rounded-circle d-inline-flex mb-4">
                <i class="bi bi-search fs-1 text-primary"></i>
              </div>
              <h4 class="fw-bold mb-3">Recherche Intelligente</h4>
              <p class="text-muted">
                Trouvez exactement ce que vous cherchez grâce à nos filtres avancés : ville, budget, équipements et classification par étoiles.
              </p>
            </div>
          </div>
          <div class="col-md-4 animate__animated animate__fadeInUp" style="animation-delay: 0.2s">
            <div class="card h-100 border-0 shadow-soft p-4 rounded-4 transition-hover">
              <div class="bg-success bg-opacity-10 p-3 rounded-circle d-inline-flex mb-4">
                <i class="bi bi-shield-check fs-1 text-success"></i>
              </div>
              <h4 class="fw-bold mb-3">Garantie de Sécurité</h4>
              <p class="text-muted">
                Vos données sont notre priorité. Chaque transaction est sécurisée et vos informations personnelles sont cryptées.
              </p>
            </div>
          </div>
          <div class="col-md-4 animate__animated animate__fadeInUp" style="animation-delay: 0.3s">
            <div class="card h-100 border-0 shadow-soft p-4 rounded-4 transition-hover">
              <div class="bg-info bg-opacity-10 p-3 rounded-circle d-inline-flex mb-4">
                <i class="bi bi-headset fs-1 text-info"></i>
              </div>
              <h4 class="fw-bold mb-3">Assistance 24/7</h4>
              <p class="text-muted">
                Une question ? Notre équipe de support est disponible jour et nuit pour vous accompagner dans votre voyage.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="py-5 bg-light rounded-5 mx-2 mx-md-5 mb-5 shadow-inner">
      <div class="container py-4">
        <div class="row g-4 text-center">
          <div class="col-6 col-md-3">
            <h3 class="display-4 fw-bold text-primary mb-0 counter">500+</h3>
            <p class="text-muted text-uppercase small ls-wide">Hôtels Partenaires</p>
          </div>
          <div class="col-6 col-md-3">
            <h3 class="display-4 fw-bold text-success mb-0 counter">10K+</h3>
            <p class="text-muted text-uppercase small ls-wide">Réservations / mois</p>
          </div>
          <div class="col-6 col-md-3">
            <h3 class="display-4 fw-bold text-warning mb-0 counter">98%</h3>
            <p class="text-muted text-uppercase small ls-wide">Satisfaction Client</p>
          </div>
          <div class="col-6 col-md-3">
            <h3 class="display-4 fw-bold text-info mb-0 counter">24/7</h3>
            <p class="text-muted text-uppercase small ls-wide">Service Support</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-5">
      <div class="container pb-5">
        <div class="bg-premium-gradient rounded-5 p-5 text-center text-white shadow-2xl position-relative overflow-hidden">
          <div class="position-relative style-z-1">
            <h2 class="display-5 fw-bold mb-4">Prêt à Planifier Votre Prochaine Aventure ?</h2>
            <p class="lead mb-5 opacity-75">
              Rejoignez des milliers de clients satisfaits qui font confiance à RIMBestStay pour leurs séjours partout au Maroc.
            </p>
            <div class="d-flex justify-content-center flex-wrap gap-4">
              <a routerLink="/search" class="btn btn-light btn-lg px-5 py-3 rounded-pill fw-bold text-primary shadow-lg border-0">
                Lancer une Recherche
              </a>
              <a *ngIf="!isLoggedIn()" routerLink="/inscription" class="btn btn-outline-light btn-lg px-5 py-3 rounded-pill fw-bold border-2">
                S'inscrire Gratuitement
              </a>
            </div>
          </div>
          <!-- Decorative SVG -->
          <div class="position-absolute bottom-0 end-0 opacity-10">
            <i class="bi bi-compass display-1" style="font-size: 20rem; transform: translate(30%, 30%) rotate(-15deg);"></i>
          </div>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .hero-premium {
      background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
      min-height: 500px;
    }
    .hero-image-wrap img {
      transition: transform 0.3s ease;
    }
    .shadow-2xl {
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
    }
    .shadow-soft {
      box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
    }
    .shadow-inner {
      box-shadow: inset 0 2px 4px 0 rgba(0,0,0,0.06);
    }
    .transition-hover:hover {
      transform: translateY(-10px);
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
    }
    .floating-anim {
      animation: floating 3s ease-in-out infinite;
    }
    @keyframes floating {
      0% { transform: translateY(0px); }
      50% { transform: translateY(-20px); }
      100% { transform: translateY(0px); }
    }
    .shape {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.05);
      z-index: 0;
    }
    .shape-1 {
      width: 400px;
      height: 400px;
      top: -100px;
      right: -100px;
    }
    .shape-2 {
      width: 300px;
      height: 300px;
      bottom: -50px;
      left: -100px;
    }
    .style-z-1 { z-index: 1; }
    .ls-wide { letter-spacing: 0.1em; }
  `]
})
export class HomeComponent {
  private authService = inject(AuthService);

  isLoggedIn() {
    return this.authService.isLoggedIn();
  }
}
