# 🏃‍♂️ Runny

**Runny** is a multiplayer running application that combines the core functionality of **Strava** with the competitive, territory-based mechanics of **paper.io**.

Instead of running only for distance or time, users play a **real-world strategy game**: by running through their city and forming **closed polygons**, they can claim territories on the map. These regions contribute to their score and can be **stolen by other players**, making every run a tactical challenge.

---

## 🎮 Concept & Gameplay

- Users run in real-world locations
- GPS paths are converted into **geometrical shapes**
- When a **closed polygon** is formed:
    - The enclosed area becomes the player’s territory
- The app is **multiplayer**
    - Other players can invade and steal regions
- The goal is to **control the largest total area** on the map

Runny transforms running into a **competitive, location-based game**.

---

## 🏗️ Architecture Overview

Runny is built as a **backend-focused application**, designed for scalability, security, and geospatial processing.

- **Backend:** Spring Boot
- **Database:** PostgreSQL + PostGIS
- **Hosting:** Render (Dockerized)
- **API-first approach:** Swagger

---

## 🛠️ Tech Stack

### Backend
- Spring Boot
- Hibernate (ORM)
    - Custom spatial dialect for geometry support
- JWT Authentication
- Swagger / OpenAPI for endpoint documentation

### Database
- PostgreSQL
- PostGIS extension for geospatial data (coordinates, polygons)
- Hosted on **Supabase**
- Liquibase
    - Schema migrations
    - Entity versioning

### DevOps & Deployment
- Docker
    - Containerized backend
    - Essential for deployment
- Render
    - Free-tier hosting platform

### Version Control & Workflow
- Git with structured branching:
    - `master` – stable production-ready code (updated after each sprint)
    - `develop` – integration branch
    - `feature/*` – individual development branches
- Code reviews before merging

### Code Quality & Testing
- SonarQube
    - Code quality & coverage analysis
    - Applied only on `master` branch (paid service)
- Unit testing
- Mocking for isolated tests

---

## 🔐 Security

- JWT Tokens
    - Stateless authentication
    - Secure access to protected endpoints

---

## 🧪 Testing Strategy

- Extensive unit tests
- Mocked services and repositories
- Focus on business logic, security, and data consistency

---

## 🚀 Future Improvements

Planned features and enhancements:

- 📧 **Personalized Email Notifications**
    - Users will receive emails when one of their regions is captured

- 🤖 **LLM-Powered Email Generation**
    - A custom LLM generates personalized, context-aware messages

- 📨 **RabbitMQ**
    - Message queue for communication between:
        - Main backend server
        - LLM server
    - Ensures asynchronous, scalable processing

---
## 📽️ Demo & Videos

Below are demonstrations of the application:

### 🎬 Video 1 – Story-Based Demo
A narrative walkthrough showing how a user discovers and starts using Runny.  
👉 [Watch the video](https://drive.google.com/file/d/1WVMePqdd5pOhboK9GI1o98J-TyW0uzuK/view?usp=sharing)

---

### 🎬 Video 2 – Gameplay Scenario
A demo focusing on competitive gameplay, territory conquest, and multiplayer interaction.  
👉 [Watch the video](https://drive.google.com/file/d/1De7sdG3SZTymJ0aOVKiyqLQcibv4Sb9C/view?usp=sharing)

---

### 🎬 Video 3 – Technical Walkthrough
A technical explanation of how the application works internally.  
👉 [Watch the video](https://drive.google.com/file/d/1jo24BzctZ_8dmhX08iUsH9ptkmAemjlZ/view?usp=sharing)
---

## 📌 Summary

Runny is not just a running app — it’s a **real-world multiplayer strategy game** powered by geospatial data, modern backend technologies, and a strong architectural foundation.
