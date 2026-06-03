# StudyBoost

Study Flow es un sistema integral diseñado para optimizar el tiempo de estudio y maximizar la retención de información. Mediante la combinación de automatización, Inteligencia Artificial generativa y técnicas de aprendizaje científico, permite transformar cualquier material de estudio (físico o digital) en herramientas de aprendizaje activo.

## 📋 Tabla de Contenidos
1. [Arquitectura del Sistema](#arquitectura-del-sistema)
2. [Características Principales](#características-principales)
3. [Stack Tecnológico](#stack-tecnológico)
4. [Roadmap y Fases de Desarrollo](#roadmap-y-fases-de-desarrollo)
5. [Instalación y Configuración](#instalación-y-configuración)

---

## 🏛️ Arquitectura del Sistema

El proyecto sigue una arquitectura híbrida donde el procesamiento pesado de IA y OCR se delega al cliente móvil y a la nube de Google, mientras que el backend gestiona la persistencia segura:

1. **Frontend (Android):** Aplicación nativa que extrae el texto (PDFs o cámara), se comunica directamente con la API de Gemini para generar el contenido de estudio y gestiona el algoritmo de repetición espaciada localmente.
2. **Backend (.NET):** API REST que actúa como orquestador de persistencia. Gestiona la autenticación, los perfiles de usuario y el almacenamiento del historial de progreso a largo plazo.
3. **Capa de IA (Google Gemini):** Sustituye al antiguo módulo de Python. La app Android consume directamente el modelo Gemini 2.0 Flash a través de su SDK oficial para tareas de procesamiento de lenguaje natural (NLP).

**Flujo de ejecución clave:** `Usuario sube PDF o Escanea Foto` ➔ `App extrae texto (PDFBox/ML Kit)` ➔ `Llamada nativa a Gemini API` ➔ `App genera Resumen, Test y Flashcards` ➔ `Cálculo de retención (SM-2)` ➔ `Sincronización de progreso con Backend (.NET)`.

---

## ✨ Características Principales

* **Digitalización Versátil:** Subida de archivos PDF, texto plano (TXT/MD) y escaneo de apuntes físicos mediante OCR (Reconocimiento Óptico de Caracteres) integrado directamente en el dispositivo.
* **Procesamiento Inteligente:** Limpieza automática de texto extraído (guiones, saltos de línea huérfanos).
* **Contenido Generado por IA (Gemini 2.0 Flash):**
  * *Resúmenes estructurados* automáticos (mín. 3 párrafos).
  * *Baterías de Tests* interactivas (5 preguntas) con feedback.
  * *Flashcards* automáticas extrayendo conceptos clave.
* **Sistema de Aprendizaje Científico:** Implementación del algoritmo **SM-2 (SuperMemo 2)** de Repetición Espaciada. Prioriza dinámicamente las tarjetas a repasar según la retención del usuario (escala 0-5).
* **Autenticación y Progreso:** Sistema de registro/login seguro y panel visual (dashboard) para monitorizar el avance del estudio.

---

## 🛠️ Stack Tecnológico

| Componente | Tecnologías |
| :--- | :--- |
| **Frontend Móvil** | Android Studio, Kotlin, **Jetpack Compose**, Material Design 3 |
| **Procesamiento Documentos** | Google ML Kit (OCR On-Device), PDFBox (Extracción PDF) |
| **Inteligencia Artificial** | Google AI SDK para Android (Gemini 2.0 Flash) |
| **Backend API** | ASP.NET Web API, Entity Framework (C#) |
| **Base de Datos** | SQL Server / PostgreSQL *(Especificar la usada)* |

---

## 🗺️ Roadmap y Fases de Desarrollo

* **Fase 1: Base Arquitectónica y UI (Semana 1-2)**
  * *Objetivo:* Conexión cliente-servidor y base visual.
  * *Hito:* Login/Registro funcional en App conectando a la API (.NET). Interfaz base construida íntegramente con Jetpack Compose.
* **Fase 2: Gestión de Contenido y OCR (Semana 3-4)**
  * *Objetivo:* Entrada de datos.
  * *Hito:* Subida de PDFs con extracción de texto limpia e implementación de la cámara con escáner OCR local (ML Kit).
* **Fase 3: Integración de IA Generativa (Semana 5-6)**
  * *Objetivo:* Dotar de inteligencia al sistema.
  * *Hito:* Integración del SDK de Gemini 2.0 Flash. La app envía los textos y renderiza resúmenes, tests interactivos y flashcards.
* **Fase 4: Sistema Científico y Pulido (Semana 7+)**
  * *Objetivo:* Lógica de aprendizaje y visualización de datos.
  * *Hito:* Implementación matemática del algoritmo SM-2, guardado de progreso de los tests, panel de estadísticas del usuario y sincronización final con el backend.

---

## ⚙️ Instalación y Configuración

*(Nota: Rellenar con los comandos exactos cuando el entorno esté configurado)*

### Prerrequisitos
* Android Studio (versión mínima recomendada: Iguana o superior)
* .NET SDK (versión 8.0+)
* API Key de Google Gemini (Google AI Studio)

### Configuración del Backend (.NET)
1. Clonar el repositorio.
2. Configurar la cadena de conexión a la base de datos en `appsettings.json`.
3. Ejecutar las migraciones: `dotnet ef database update`
4. Iniciar el servidor: `dotnet run`

### Configuración del Frontend (Android)
1. Abrir el proyecto en Android Studio.
2. Configurar la constante `BASE_URL` en el cliente de red para que apunte al host local del backend.
3. **Gestión de Credenciales (Muy Importante):** Añadir la API Key de Gemini en el archivo `local.properties` (que debe estar en `.gitignore`) para inyectarla de forma segura vía `BuildConfig`:
```properties
   GEMINI_API_KEY=tu_clave_secreta_aqui
````
4. Sincronizar Gradle y compilar en el emulador o dispositivo físico.
