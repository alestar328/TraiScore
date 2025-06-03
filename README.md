📱 Documentación de Pantallas - TraiScore App
LoginScreen
Propósito
Pantalla de autenticación y registro de usuarios con formulario completo de perfil.
![Login Screen](url){width=300px}

Funcionalidades
✅ Login con email/contraseña
✅ Autenticación con Google
✅ Recuperación de contraseña
✅ Registro completo (nombre, apellido, fecha nacimiento, género, tipo de cuenta)
Estados Principales
Estado	Descripción
isNewUser	Modo registro/completar perfil
isRegistering	Tracking durante proceso de registro
AddExerciseBottomSheet
Propósito
Modal para agregar y editar ejercicios en entrenamientos.

Funcionalidades
🏋️ Selección de ejercicio con dropdown filtrable
⚖️ Entrada de peso y repeticiones
📊 Control deslizante RIR (Rate of Perceived Exertion)
✅ Validación de formulario en tiempo real
Modos de Operación
Crear: Nuevo ejercicio
Editar: Modificar ejercicio existente
ExercisesScreen
Propósito
Vista principal de ejercicios registrados con funcionalidades de gestión.

Funcionalidades
📅 Lista de entrenamientos agrupados por fecha
🔍 Búsqueda y filtrado de ejercicios
✏️ Edición/eliminación de entradas
📱 Navegación con bottom sheet para edición
Componentes Clave
WorkoutCardList: Lista de tarjetas de entrenamiento
FilterableDropdown: Barra de búsqueda dinámica
StatScreen
Propósito
Pantalla de estadísticas y seguimiento del progreso del usuario.

Pestañas
🏆 Mis Records
1RM: Una repetición máxima
Máximas repeticiones: Record personal
Gráficos: Progreso de peso/repeticiones
Total: Peso total levantado
📏 Mis Medidas
Gráficos corporales: Evolución temporal
Métricas: Comparación de cambios
Resumen: Estadísticas de progreso
Componentes Visuales
LineChartView: Gráficos de línea para progreso
CircularProgressView: Métricas circulares
MyClients
Propósito
Gestión de clientes para entrenadores con actualizaciones en tiempo real.

Funcionalidades
👥 Lista en tiempo real de clientes vinculados
🔢 Contador de clientes activos
📧 Navegación a sistema de invitaciones
🔄 Actualización manual con botón refresh
Estados de la UI
Estado	Descripción	Acción
Loading	Cargando datos	Mostrar spinner
Error	Error de conexión	Botón retry
Empty	Sin clientes	Botón agregar cliente
Success	Lista cargada	Mostrar clientes
RoutineMenuScreen
Propósito
Menú principal para gestión de rutinas de entrenamiento.

Funcionalidades
📋 Lista de rutinas guardadas
🗑️ Swipe-to-delete con confirmación
📂 Importar rutinas desde archivos
➕ Crear nueva rutina
Modos de Vista
Entrenador: Gestión completa de rutinas
Cliente: Rutinas asignadas (solo lectura)
Interacciones
Acción	Resultado
Tap	Abrir rutina
Swipe ←	Eliminar rutina
FAB	Agregar/importar rutina
🔄 Patrones de Diseño Comunes
Estado de UI Estándar
kotlin
when {
    isLoading -> CircularProgressIndicator()
    error != null -> ErrorMessage()
    data.isEmpty() -> EmptyState()
    else -> Content()
}
Listeners en Tiempo Real
Firestore addSnapshotListener para actualizaciones automáticas
Cleanup en DisposableEffect para evitar memory leaks
Estado reactivo con StateFlow y collectAsState()
Navegación
Bottom Sheets para formularios modales
NavController para navegación entre pantallas
Deep linking para acceso directo a contenido
🛠️ Tecnologías Utilizadas
UI: Jetpack Compose
Estado: ViewModel + StateFlow
Base de datos: Firestore (tiempo real)
Inyección: Hilt
Navegación: Navigation Compose
