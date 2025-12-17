# Systeme-de-gestion-d-alerte-municipale---projet-1
STACK : Spring boot, Spring Security, JPA Hibernate, MySQL, Thymeleaf - projet par binome: siwar labassi et ons fitouri


## Description du projet

Ce projet est une **application web de gestion des incidents urbains** dans le cadre d’une *ville intelligente*. Elle permet aux citoyens de signaler des problèmes quotidiens (infrastructures, propreté, sécurité, éclairage public, etc.) et aux services municipaux de les traiter efficacement via un workflow structuré.

L’objectif est d’améliorer la **réactivité des autorités locales**, d’assurer un **suivi transparent des incidents** et de collecter des données utiles pour des analyses urbaines futures.

---

## Technologies utilisées

### Back-end

* **Spring Boot**
* **Spring MVC**
* **Spring Data JPA**
* **Spring Security**
* **Hibernate**
* **Maven**

### Base de données

* **MySQL** 

### Front-end

* **Thymeleaf** (vues HTML dynamiques)
* **HTML / CSS / JavaScript**

### Autres outils & services

* **API Leaflet** (géolocalisation)
* **Spring Mail** (notifications par email)
* **Chart.js** (statistiques et graphiques)
* **iText PDF** (export PDF)
* **Git & GitHub** (versionnement)

---

## Fonctionnalités principales

### 👤 Gestion des utilisateurs

* Inscription sécurisée avec validation
* Authentification et autorisation (http session et Spring Security pour OAuth2 seconnecter avec google)
* Gestion des rôles :

  * **CITOYEN**
  * **AGENT_MUNICIPAL**
  * **ADMINISTRATEUR**

### 📝 Déclaration d’incidents

* Création d’un incident avec :

  * Description
  * Catégorie
  * Localisation
  * Upload d’images
* Consultation de l’historique des incidents

### 🔄 Workflow des incidents

Chaque incident suit un cycle de vie :

1. Signalé
2. Pris en charge
3. En résolution
4. Résolu
5. Clôturé

Les transitions sont contrôlées selon le rôle de l’utilisateur.

### 🔔 Notifications

* Envoi automatique d’emails lors des changements de statut
* Notifications aux agents municipaux

### 📊 Tableaux de bord & statistiques

* Tableaux de bord personnalisés par rôle
* Statistiques par :

  * nombre par type d’incident
  * nombre incidents par quartier
 
  
* Export des rapports en **PDF / CSV**

### 🔍 Recherche & filtrage

* Recherche par catégorie, statut, date, localisation
* Pagination et tri dynamiques

---

## 🔐 Sécurité

* Authentification via google **Spring Security OAuth2**
* Chiffrement des mots de passe (**BCrypt**)
* Protection CSRF
* Gestion fine des autorisations par rôle via les sessions


## 🧩 Modèle de données 

* Utilisateur
* Incident
* Quartier
* Notification
* Rapport
* CategorieIncident
* MunicipalService
* FiltreIncident

---

## ▶️ Lancement du projet

### Prérequis

* Java 17+
* Maven
* MySQL

### Étapes

```bash
# Cloner le projet
git clone https://github.com/Siwar1609/Systeme-de-gestion-d-alerte-municipale---projet-1.git

# Accéder au dossier
cd Systeme-de-gestion-d-alerte-municipale---projet-1

# Lancer l’application
mvn spring-boot:run
```

L’application sera accessible sur :

```
http://localhost:8080
```

---

## 👥 Équipe & collaboration

Projet académique réalisé dans le cadre du module **Développement Web Avancé**.

Gestion du code source avec **Git/GitHub** (branches, pull requests, merge).

---

## 📄 Licence

Projet à usage **pédagogique et académique**.
