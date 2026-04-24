# SafeTag - Review Service

Ce microservice gère la consultation, l'ajout et la modération (à venir) des avis laissés sur les professionnels de santé.

## ⚙️ Fonctionnement et Règles de gestion

* **Identifiant Praticien (RPPS) :** Tout praticien est identifié par son numéro RPPS.
* **Validation stricte :** Le numéro RPPS doit obligatoirement contenir **exactement 11 chiffres**. Toute requête avec un format invalide est rejetée en amont (Erreur 400).
* **Pagination :** Les listes d'avis sont paginées pour garantir de bonnes performances.

---

## 🚀 Endpoints de l'API

### 1. Consulter les avis d'un praticien

Récupère la liste paginée des avis pour un professionnel de santé donné.

* **URL :** `/api/v1/reviews/practitioner/{rppsId}`
* **Méthode :** `GET`

**Paramètres de chemin (Path Variables) :**
* `rppsId` (String) : Numéro RPPS du praticien (11 chiffres).

**Paramètres de requête (Query Params) optionnels :**
* `page` (int) : Numéro de la page (défaut: 0).
* `size` (int) : Nombre d'éléments par page (défaut: 10).

**Réponses attendues :**

* **🟢 200 OK** : Retourne la liste paginée des avis.
* **🟠 400 Bad Request** : Le `rppsId` est mal formaté (ex: longueur différente de 11 chiffres ou caractères non numériques).
* **🔴 500 Internal Server Error** : Problème côté serveur ou base de données.

---

## 🛠️ Stack Technique
* Java / Spring Boot
* API REST
