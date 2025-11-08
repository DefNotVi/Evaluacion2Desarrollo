# Evaluación 2: Aplicación Móvil con Jetpack Compose y Xano para la API

## 1. Caso elegido y alcance

- **Caso:** TravelGo SPA

- **Alcance EP3:** Diseño/UI, validaciones, navegación, gestión de estado (local y global), persistencia de sesión y URI de avatar, recursos nativos (cámara/galería) y consumo de API con manejo de token ( /auth/me).

## 2. Requisitos y ejecución

**Stack:**

- **Framework:** Android SDK (API 33+)

- **Librerías:** Jetpack Compose, Kotlin Coroutines & Flow, Retrofit/OkHttp, DataStore.

**Instalación:**

- **Clonar Repositorio:** https://github.com/DefNotVi/Evaluacion2Desarrollo.git

- **Abrir en el Android Studio:** Abrir la carpeta del proyecto en Android Studio

- **Sincronizar dependencias:** Sincronizar el proyecto con Gradle si no lo está

**Ejecución:**

- Presiona el botón de ejecutar

- **Perfiles de Uso:**
- **Usuario Nuevo:** Abre la app y ve a la pantalla de registro dandole al boton "¿No tienes cuenta? Regístrate", crea una cuenta e inicia sesion
- **Usuario Existente:** Abre la app e inicia sesión, la próxima vez que se abra la app, se va a redirigir directamente a la pantalla principal hasta que se cierre la sesión

## 3. Arquitectura y flujo

**Estructura carpetas:**

- **ui/:** Contiene las pantallas “Composables” y sus respectivos ViewModels (login, profile), es basicamente la capa de presentación

- **repository/:** Abstrae el origen de los datos (red o local), incluye UserRepository, AuthRepository y AvatarRepository

- **data/local/:** Gestiona la persistencia local de la sesión (SessionManager con DataStore) y el URI del Avatar

- **data/remote/:** Define la comunicación con la API (ApiService), modelos de datos (DTOs) y el interceptor de autenticación (AuthInterceptor)

- **navigation/:** Contiene la navegación con rutas, Compose y Navhost

**Gestión de estado:**

- **Estrategia:** MVVM (Model-View-ViewModel).

- **Flujo de Datos (Unidireccional):** La UI observa el StateFlow del ViewModel. El ViewModel inicia la carga de datos en el Repository. El Repository retorna el dato al ViewModel, que actualiza el StateFlow (Trigger -> Event -> State -> UI).

**Navegación:**

- **Stack:** Se utiliza la navegación de Jetpack Compose (Compose Navigation). El flujo principal respeta el backstack: Tras el login, el usuario es dirigido a Profile/Dashboard, y el botón de cerrar sesión lo devuelve a la pantalla de Login cerrando la sesión actual.

## 4. Funcionalidades

- **Formulario validado:** Formulario de Login/Registro, valida campos de email y password (requeridos), bloqueando el envío si no son válidos (como por ej, email inválido o contraseña vacía)

- **Navegación y backstack:** Flujo de la aplicación con transiciones suaves y gestión correcta del inicio de sesión

- **Gestión de estado:** Se manejan los estados de Carga (isLoading), Éxito (datos de perfil) y Error (mensajes visibles en la UI) sincronizados con la respuesta del Repository

- **Persistencia local (CRUD):**  Sesión: Token JWT persistido en DataStore (SessionManager)

- **Avatar:** URI de la imagen de perfil almacenada en DataStore (AvatarRepository)

- **Recursos nativos:** Integración de Cámara/Galería para seleccionar la imagen de perfil. Incluye el manejo de permisos y un fallback con mensaje al usuario si el permiso es denegado.

- **Animaciones con propósito:** Transiciones y efectos de carga (progress indicators) para mejorar el feedback al usuario durante la espera.

- **Consumo de API** ( `/me`): Implementación del llamado al endpoint de perfil /auth/me con un AuthInterceptor para adjuntar el token de autenticación de forma automática en todas las peticiones protegidas

## 5. Endpoints

**Base URL:** `https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW`

| Método    | Ruta          | Body                              | Respuesta

| ------    | ------------  | ----------------------------------| -----------------------------------
 
| POST      | /auth/signup  | { email, password}                | 201 {authToken, user: { id, email,…}

| POST      | /auth/login   | { email, password }               | 200 {authToken, user: { id, email,…}

| GET       | /auth/me      | (requiere header Authorization: Bearer ) | 200 { id, email, name, avatarUrl?,…} 

**Endpoints sin estar en una “tabla”:**

- POST /auth/signup

- Body: { email, password, name}

- Respuesta (Ejemplo): 201 { authToken, user: { id, email,…}}

- POST /auth/login

- Body: { email, password}

- Respuesta (Ejemplo): 200 { authToken, user: { id, email,…}}

- GET /auth/me

- Requerimiento: Header Authorization: Bearer (el token)

- Respuesta (Ejemplo): 200 { id, email, name, avatarUrl?} 

## 6. User flows

**Flujo principal (Login exitoso):**

- El usuario ingresa email y password en la pantalla de Login y presiona "Ingresar" (o puede optar por registrarse dandole a "¿No tienes cuenta? Regístrate")

- El AuthRepository llama a POST /auth/login

- Si es exitoso, el token es capturado y guardado en SessionManager

- La navegación redirige a la pantalla de Perfil/Dashboard

- El ProfileViewModel llama a fetchProfile(), el cual utiliza el token guardado a través del AuthInterceptor para obtener los datos del usuario

- La pantalla de perfil muestra los datos y el avatar (si está guardado localmente)

**Casos de Error:**

- Error de credenciales: El AuthViewModel captura el error del API (por ej, 400 o 401) y muestra un mensaje de error en la pantalla de Login

- Error de red/servidor: Las peticiones de la API están envueltas en try-catch. En caso de timeout o falta de conexión, se actualiza el estado a error, mostrando un mensaje al usuario