# GameDex 🎮

**GameDex** es una aplicación Android moderna diseñada para que los entusiastas de los videojuegos puedan descubrir nuevos títulos, gestionar su colección personal (Backlog) y compartir sus descubrimientos mediante códigos QR.

## ✨ Características

- **Explorar**: Descubre una amplia lista de juegos gratuitos directamente desde una API externa.
- **Mi Backlog**: Gestiona tus juegos y clasifícalos por estado: *Pendiente*, *Jugando* o *Terminado*.
- **Escaner QR**: Añade juegos a tu colección escaneando códigos QR generados por otros usuarios.
- **Perfil Personalizado**: Configura tu Gamertag, frase y foto de perfil (con persistencia local).
- **Compartir**: Genera códigos QR únicos para cada juego para compartirlos con tus amigos.
- **Modo Oscuro**: Interfaz optimizada con un diseño "gamer" moderno y soporte para tema oscuro.

## 📸 Capturas de Pantalla

| Explorar Juegos |

| <img width="612" height="1360" alt="image" src="https://github.com/user-attachments/assets/b2444d28-b2a2-44ce-8134-b84da1db25e5" /> | 

| Detalle del Juego | 
 
| <img width="612" height="1360" alt="image" src="https://github.com/user-attachments/assets/271056af-6bbb-4d47-b132-9ebebd178f1d" /> |

| Mi Backlog |

| <img width="612" height="1360" alt="image" src="https://github.com/user-attachments/assets/026909ea-7c84-4e40-a63d-54f1d07704c1" /> |

| Perfil de Jugador |

| <img width="612" height="1360" alt="image" src="https://github.com/user-attachments/assets/582ef635-687c-46de-ba51-b397eea33919" /> |

| Escáner QR |

| <img width="612" height="1360" alt="image" src="https://github.com/user-attachments/assets/c4b5d39d-8c83-4b62-bdb6-934ac1f2fd41" /> |

| Modo Oscuro |

| <img width="1224" height="2720" alt="image" src="https://github.com/user-attachments/assets/81935795-9b76-46f3-bd87-6a98a46f1469" /> |

## 🏗️ Arquitectura

La aplicación sigue los principios de **Clean Architecture** y el patrón de diseño **MVVM (Model-View-ViewModel)**:

- **UI (Compose)**: Interfaz declarativa moderna utilizando Jetpack Compose.
- **ViewModel**: Gestión del estado de la UI y comunicación con el repositorio utilizando `StateFlow` y `Coroutines`.
- **Repository**: Capa intermedia que decide el origen de los datos (Local vs Remoto).
- **Data (Local)**: 
    - **Room Database**: Persistencia de la colección de juegos.
    - **DataStore**: Almacenamiento de preferencias del usuario y datos del perfil.
- **Data (Remote)**:
    - **Retrofit**: Consumo de la API de videojuegos.

## 🌐 API Utilizada

La aplicación utiliza la **Free-To-Play Games API** (vía [FreeToGame](https://www.freetogame.com/api-doc)) para obtener información actualizada sobre los mejores títulos gratuitos del mercado.

## 🛠️ Tecnologías Utilizadas

- **Kotlin**: Lenguaje de programación principal.
- **Jetpack Compose**: Toolkit moderno para construir UIs nativas.
- **Room**: Abstracción sobre SQLite para la base de datos local.
- **DataStore**: Solución moderna para guardar pares clave-valor.
- **Retrofit & Gson**: Para peticiones de red y serialización JSON.
- **Coil**: Carga eficiente de imágenes desde internet.
- **ML Kit**: Escaneo de códigos QR de alto rendimiento.
- **CameraX**: Integración simplificada de la cámara.
- **Navigation Compose**: Gestión de la navegación entre pantallas.

---
Desarrollado con ❤️ para la comunidad gamer.
