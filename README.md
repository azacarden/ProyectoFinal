# 💊 TuToma - Gestión Inteligente de Salud

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blueviolet.svg?style=flat&logo=kotlin)
![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg?style=flat&logo=android)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore-FFCA28.svg?style=flat&logo=firebase)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange.svg)

**TuToma** es una aplicación nativa para Android diseñada para digitalizar y simplificar la gestión de tratamientos médicos en pacientes crónicos, polimedicados o dependientes.

Nace con un objetivo claro: **sustituir las anotaciones a bolígrafo en las cajas de medicamentos** por un ecosistema digital colaborativo donde **Pacientes** y **Cuidadores** interactúan en tiempo real.

---

## ✨ Características Principales

El desarrollo se ha centrado en resolver la brecha digital de la tercera edad mediante ingeniería de software enfocada en la usabilidad:

* 👥 **Sistema de Roles Dual:** Interfaces y permisos adaptados según el registro inicial (Paciente o Cuidador).
* 🔗 **Vinculación por Código QR:** Los cuidadores pueden enlazar cuentas escaneando el código QR del paciente mediante la integración nativa de la librería *ZXing*, eliminando el uso de correos de invitación.
* 📡 **Estrategia Offline-First:** La base de datos local garantiza que las alarmas de medicación suenen en el segundo exacto (mediante `AlarmManager` nativo) incluso sin conexión a internet o con el dispositivo en modo Doze.
* ☁️ **Sincronización Transparente y Resiliente:** Los datos se sincronizan en segundo plano con **Cloud Firestore**. Si la red falla, la app guarda el estado en la base de datos local, reintentando la subida al recuperar la conexión.
* 🏥 **Integración API:** Consumo de la API oficial **CIMA** (AEMPS) mediante *Retrofit* para autocompletar fármacos, consultar vías de administración y consultar o descargar prospectos.

---

## 🏗️ Arquitectura del Sistema (MVVM & Single Source of Truth)

El proyecto está estructurado bajo la arquitectura **MVVM (Model-View-ViewModel)** y hace un uso intensivo de **Kotlin Coroutines** y **StateFlow** para garantizar una programación reactiva y segura.

La capa de repositorios actúa como la "Única Fuente de Verdad", determinando si los datos deben leerse del almacenamiento local (**Room SQLite**) o de la nube (**Firebase**).

### Flujo de Sincronización y Acceso a Datos

```mermaid
graph TD
    %% Capa UI
    UI[🖥️ Interfaz de Usuario<br/>Fragments] -->|Eventos| VM
    
    %% Capa Lógica
    VM[⚙️ ViewModel<br/>StateFlow / Coroutines] -->|Llamadas Asíncronas| Repo
    Repo[🏛️ Repositorio<br/>Single Source of Truth]
    
    %% Capa Datos
    Repo -->|1. Lectura/Escritura Inmediata| Room[(📦 Room SQLite<br/>Local DB)]
    Repo -.->|2. Sincronización Diferida| Fire[(☁️ Firebase Firestore<br/>Cloud DB)]
    Repo -->|Llamada API| Retro[🌐 Retrofit<br/>CIMA API]
    
    %% Flujo Reactivo
    Room -->|Flow Reactivo| VM
    VM -->|Actualiza| UI

    style UI fill:#D4EAE3,stroke:#5A9C8B
    style VM fill:#FFF,stroke:#7895CB
    style Repo fill:#FFF,stroke:#2C3E50,stroke-width:2px
    style Room fill:#F8F9FA,stroke:#2C3E50
    style Fire fill:#FFF9C4,stroke:#F57F17