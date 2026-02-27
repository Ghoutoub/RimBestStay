# Guide d'Architecture Rimbest (Backend & Frontend)

Ce document détaille l'architecture technique du projet Rimbest, incluant la hiérarchie des fichiers, le rôle de chaque couche et le catalogue complet des APIs REST.

---

## 🏗️ Architecture Backend (Spring Boot REST)

Le backend suit une architecture en couches standard pour les applications Java d'entreprise, assurant une séparation claire des responsabilités.

### 📁 Hiérarchie du Projet Backend
`rimbest/src/main/java/com/Rimbest/rimbest/`
- **`model/`** : Entités JPA représentant les tables de la base de données (User, Hotel, Chambre, Reservation).
- **`model/dto/`** : Objets de Transfert de Données (DTO) utilisés pour les échanges API. Ils isolent le modèle de données interne de l'interface publique.
- **`repository/`** : Interfaces étendant `JpaRepository` pour les opérations CRUD et les requêtes personnalisées vers MySQL.
- **`service/`** : Couche métier (Service) contenant la logique de validation, les calculs de prix et les règles métier complexes.
- **`controller/`** : Contrôleurs REST (`@RestController`) exposant les endpoints consommés par Angular.
- **`security/`** : Configuration Spring Security et gestion des tokens JWT.

### 📋 Catalogue des APIs REST

| Entité | Méthode | Endpoint | Fonctionnalité |
| :--- | :--- | :--- | :--- |
| **Auth** | POST | `/api/auth/register` | Inscription d'un nouveau client |
| | POST | `/api/auth/login` | Connexion et récupération du JWT |
| **Hotels** | GET | `/api/hotels` | Liste paginée avec filtres (ville, étoiles) |
| | GET | `/api/hotels/{id}` | Détails d'un hôtel spécifique |
| | POST | `/api/hotels` | Création d'un hôtel (Admin/Partenaire) |
| | PUT | `/api/hotels/{id}` | Mise à jour avec support image |
| **Chambres** | GET | `/api/chambres/{id}` | Détails d'une chambre |
| | GET | `/api/hotels/{id}/chambres` | Liste des chambres d'un hôtel |
| | POST | `/api/hotels/{id}/chambres` | Ajout d'une chambre à un hôtel |
| | GET | `/api/hotels/{id}/chambres/disponibles` | Chambres libres selon dates/capacité |
| **Reservations** | POST | `/api/reservations` | Création d'une réservation (Client) |
| | GET | `/api/reservations/client` | Liste des réservations du client connecté |
| | PUT | `/api/reservations/{id}/status` | Confirmation/Refus (Admin/Partenaire) |
| **Admin/Part** | GET | `/api/admin/users` | Gestion des utilisateurs (Admin) |
| | GET | `/api/partenaire/hotels` | Liste des hôtels du partenaire connecté |

---

## 🎨 Architecture Frontend (Angular 18+)

Le frontend est construit de manière modulaire, utilisant des services injectables pour la communication avec le backend.

### 📁 Hiérarchie du Projet Frontend
`rimbestfront/src/app/`
- **`core/services/`** : Contient la logique d'appel API. Chaque service (ex: `HotelService`) correspond à un domaine du backend.
    - `api.service.ts` : Service de base utilisant `HttpClient` pour centraliser les appels (GET, POST, etc.).
- **`core/interceptors/`** : `auth.interceptor.ts` intercepte chaque requête HTTP pour y injecter le token JWT présent dans le `localStorage`.
- **`pages/`** : Composants UI organisés par fonctionnalité :
    - `admin/` : Tableaux de bord et gestion globale.
    - `partenaire/` : Interface de gestion hôtelière.
    - `client/` : Réservations et profil.
    - `hotel/` & `chambre/` : Catalogues et détails.

### 🔄 Comment Angular consomme les APIs REST ?

1.  **Interception** : L'intercepteur récupère le JWT et l'ajoute au header `Authorization: Bearer <token>`.
2.  **Service Angular** : Un composant fait appel à une méthode de service (ex: `hotelService.getById(id)`).
3.  **HttpClient** : Le service utilise `HttpClient` d'Angular pour envoyer la requête vers le serveur Spring Boot.
4.  **Backend** : Le `RestController` reçoit la requête, valide le JWT, appelle le `Service` pour traiter les données et renvoie un JSON.
5.  **Souscription** : Le composant Angular "souscrit" (Subscribe) à l'Observable retourné par le service pour mettre à jour l'interface avec les données reçues.

---

## 🛠️ Outils de Développement utilisés
- **Backend** : Java 21, Spring Boot 3.5, Hibernate/JPA, MySQL.
- **Frontend** : Angular 18+, TypeScript, CSS Moderne, RxJS.
