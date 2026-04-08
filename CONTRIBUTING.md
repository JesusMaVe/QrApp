# Guia para Contribuidores

## Como contribuir

1. Hacer fork del repositorio
2. Crear una rama para la feature (`git checkout -b feature/nombre`)
3. Realizar cambios con tests correspondientes
4. Verificar que el codigo compile: `./gradlew compileDebugKotlin`
5. Hacer commit y push
6. Crear un Pull Request

## Convenciones de desarrollo

- **Compose First**: Todos los componentes de UI deben construirse usando Jetpack Compose
- **Patron MVVM**: Separacion estricta de responsabilidades entre UI (Screens) y logica (ViewModels)
- **Estado reactivo**: Usar `StateFlow` en ViewModels para exponer estado a la UI
- **Manejo de permisos**: Permisos de camara gestionados reactivamente usando Accompanist Permissions
- **Lifecycle de CameraX**: Siempre asegurar que los casos de uso de CameraX esten correctamente bound al LifecycleOwner
- Usar `remember` y `animate*AsState` para transiciones de UI performantes
- Preferir funciones de extension cuando sea apropiado
- Documentar funciones publicas complejas
- Seguir las convenciones de Kotlin existentes en el proyecto
