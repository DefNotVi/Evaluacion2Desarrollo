# Examen transversal: Aplicación Móvil con Jetpack Compose, MongoAtlas y render para la API
# [TravelGO SPA](https://github.com/DefNotVi/Evaluacion2Desarrollo/releases/tag/0.1.0)
- **[Por Vicente Escobar](https://github.com/DefNotVi/Evaluacion2Desarrollo/commits/master/)**

## 1. Caso elegido y alcance

- **Caso:** TravelGo SPA

- **Alcance EP3:** Diseño/UI, validaciones, navegación, gestión de estado (local y global), persistencia de sesión y URI de avatar, recursos nativos (cámara/galería) y consumo de API con manejo de token ( /auth/me), roles de Cliente y Administrador, pruebas unitarias, uso de backend (render).

## 2. Requisitos y ejecución

**Stack:**

- **Framework:** Android SDK (API 33+)

- **Librerías:** Jetpack Compose, Kotlin Coroutines & Flow, Retrofit/OkHttp, DataStore, MockK/Junit.

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

- **ui/:** Contiene las pantallas “Composables” y sus respectivos ViewModels, es basicamente la capa de presentación

- **repository/:** Abstrae el origen de los datos (red o local), incluye PackageRepository, AuthRepository y AvatarRepository

- **data/local/:** Gestiona la persistencia local de la sesión (SessionManager con DataStore), el URI del Avatar, el id del usuario, el email del usuario y su respectivo rol

- **data/remote/:** Define la comunicación con la API (ApiService), modelos de datos (DTOs) y el interceptor de autenticación (AuthInterceptor)

- **navigation/:** Contiene la navegación con rutas, Compose y Navhost

**Gestión de estado:**

- **Estrategia:** MVVM (Model-View-ViewModel).

- **Flujo de Datos (Unidireccional):** La UI observa el StateFlow del ViewModel. El ViewModel inicia la carga de datos en el Repository. El Repository retorna el dato al ViewModel, que actualiza el StateFlow (Trigger -> Event -> State -> UI).

**Navegación:**

- **Stack:** Se utiliza la navegación de Jetpack Compose (Compose Navigation). El flujo principal respeta el backstack: Tras el login, el usuario es dirigido a Profile/Dashboard, y el botón de cerrar sesión lo devuelve a la pantalla de Login cerrando la sesión actual.

## 4. Funcionalidades

- **Autenticación Completa:** Flujos de Login y Registro validados (email, password) con manejo de token JWT

- **Persistencia de Sesión:** Token JWT, ID, Email y Rol persisten en el DataStore (SessionManager)

- **Vista de Paquetes:** Muestra la lista de paquetes turísticos obtenidos del backend apenas se  logea el cliente 

- **Filtros Dinámicos:** Filtrado por texto (nombre/destino) y filtrado por Categoría en la pantalla de paquetes

- **Perfil y Edición:** Carga y Muestra los datos del perfil (nombre, email, ID, etc). Incluye edición de campos :D

- **Recursos Nativos:** Uso de Cámara y/o Galería para cambiar la imagen de perfil

- **Admin:** Pantalla dedicada para usuarios con role: ADMIN con opciones para gestionar usuarios y crear paquetes

- **Manejo de Errores y otras cositas:** Muestra estados de Carga visualmente, Éxito y mensajes de Error específicos (como por ej: "Error de red", "Credenciales inválidas")

- **Pruebas Unitarias:** Implementadas en AuthRepository para validar la lógica de sesión y autenticación entre otros, esto debido a que allí se concentran las funciones principales
- ## 5.[Endpoints](https://github.com/DefNotVi/Evaluacion2Desarrollo/blob/master/app/src/main/java/com/gwagwa/evaluacion2/data/remote/ApiService.kt)

**Base URL:** `https://travelgo-api-1.onrender.com/api/`

| Método    | Ruta         			    | Body                             	       | Respuesta

| ------    | ------------  			    | ----------------------------------       | -----------------------------------

| POST      | auth/register			    | { email, password, name}                 | 201 {authToken, user: { id, email, role...}

| POST      | auth/login   			    | { email, password }                      | 200 {authToken, user: { id, email, role...}

| GET       | cliente-profile/me     		    | (requiere header Authorization: Bearer ) | 200 { _id, email, name, avatarUrl?,..} 

| GET	    | paquete-turístico/disponibles         | (requiere header Authorization: Bearer)  | 200 { _id, nombre, descripción...}

| PUT       | cliente-profile/me       	  	    | { nombre, teléfono, dirección...}        | 404 { message, error, statuscode} (debería poder actualizar el perfil pero no lo hace la api)

| GET       | auth/users              		    | (requiere rol de admin y Token)          | 200 (devuelve una lista de usuarios como el cliente-profile/me)

| POST	    | paquete-turístico                     | { nombre, descripción, destino...}       | 201 { success, message, data}

## 6. User flows

**Flujo principal (Login exitoso usuario):**

- El usuario ingresa credenciales en Login y presiona "Ingresar".
- El "AuthRepository" llama a "POST auth/login".
- Si es exitoso, el token, ID, Email y Rol son capturados y guardados en el "SessionManager".
- La navegación redirige al "PackageListScreen".
- El ViewModel carga el perfil (authRepository.getProfile()) usando el token, el cual es inyectado por el "AuthInterceptor".

- **Casos de Error:**

- **Error de Credenciales/401:** El ViewModel captura el error de la API (generalmente 400 o 401) y muestra un mensaje en la pantalla de Login ("Credenciales inválidas").
- **Error de Red/Timeout:** Las peticiones están envueltas en "try-catch". Si hay un "IOException", se actualiza el estado a error, mostrando un mensaje al usuario (como: "Error de red al cargar el perfil").
- **Error 404 al Guardar Perfil:** Se muestra el mensaje "Error: Fallo al guardar: Fallo al actualizar el perfil: HTTP 404" (Problema confirmado de Backend). La funcionalidad de guardado está afectada gracias al backend no funcionando bien.
