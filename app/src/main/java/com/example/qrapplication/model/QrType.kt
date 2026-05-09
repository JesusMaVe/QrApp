package com.example.qrapplication.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Tipos de contenido QR representados como sealed class para garantizar
 * un manejo exhaustivo de casos (exhaustive when).
 *
 * Proporciona información contextual para cada tipo de QR incluyendo
 * icono, nombre descriptivo y comportamiento asociado.
 */
sealed class QrType(
    val displayName: String,
    val icon: ImageVector,
    val actionLabel: String
) {
    /**
     * QR que contiene una URL o enlace web.
     * Comportamiento típico: abrir en navegador.
     */
    data object Url : QrType(
        displayName = "Enlace",
        icon = Icons.Default.Link,
        actionLabel = "Abrir enlace"
    )

    /**
     * QR que contiene configuración de red Wi-Fi.
     * Incluye SSID, tipo de autenticación y contraseña.
     */
    data object WiFi : QrType(
        displayName = "Wi-Fi",
        icon = Icons.Default.Wifi,
        actionLabel = "Conectar"
    )

    /**
     * QR que contiene texto plano u otro contenido no estructurado.
     * Comportamiento típico: copiar al portapapeles.
     */
    data object Text : QrType(
        displayName = "Texto",
        icon = Icons.Default.TextFields,
        actionLabel = "Copiar texto"
    )

    /**
     * QR que contiene un email (mailto:).
     * Incluye dirección, asunto opcional y cuerpo opcional.
     */
    data object Email : QrType(
        displayName = "Email",
        icon = Icons.Default.Cloud,
        actionLabel = "Enviar email"
    )

    /**
     * Tipo de QR no reconocido o no soportado.
     */
    data object Unknown : QrType(
        displayName = "Desconocido",
        icon = Icons.Default.TextFields,
        actionLabel = "Copiar"
    )

    companion object {
        /**
         * Analiza una cadena de contenido y determina el tipo de QR.
         *
         * @param content El contenido decodificado del QR
         * @return El QrType correspondiente al contenido
         */
        fun fromContent(content: String): QrType = when {
            content.startsWith("WIFI:") -> WiFi
            content.startsWith("mailto:", ignoreCase = true) -> Email
            content.startsWith("http://", ignoreCase = true) -> Url
            content.startsWith("https://", ignoreCase = true) -> Url
            content.startsWith("www.", ignoreCase = true) -> Url
            else -> Text
        }

        /**
         * Convierte un ContentType (enum existente) a QrType (sealed class).
         * Mantiene compatibilidad con el modelo de datos existente.
         *
         * @param contentType El ContentType a convertir
         * @return El QrType equivalente
         */
        fun fromContentType(contentType: ContentType): QrType = when (contentType) {
            ContentType.URL -> Url
            ContentType.WIFI -> WiFi
            ContentType.TEXT -> Text
            ContentType.EMAIL -> Email
            ContentType.CONTACT,
            ContentType.PRODUCT,
            ContentType.PHONE -> Text
            ContentType.UNKNOWN -> Unknown
        }
    }
}

/**
 * Función de extensión para determinar el icono apropiado según el tipo de contenido.
 *
 * Usage:
 * ```
 * val contentType: ContentType = ContentType.URL
 * val icon: ImageVector = contentType.iconForType()  // Returns Link icon
 * ```
 *
 * @receiver ContentType - El tipo de contenido del QR
 * @return ImageVector - El icono apropiado para el tipo de contenido
 */
fun ContentType.iconForType(): ImageVector = when (this) {
    ContentType.URL -> Icons.Default.Link
    ContentType.WIFI -> Icons.Default.Wifi
    ContentType.EMAIL -> Icons.Default.Cloud
    ContentType.TEXT -> Icons.Default.TextFields
    ContentType.CONTACT -> Icons.Default.Cloud
    ContentType.PRODUCT -> Icons.Default.Cloud
    ContentType.PHONE -> Icons.Default.Cloud
    ContentType.UNKNOWN -> Icons.Default.TextFields
}

/**
 * Función de extensión alternativa que opera directamente sobre String
 * para determinar el icono sin necesidad de parsear primero el tipo.
 *
 * Usage:
 * ```
 * val qrContent: String = "https://example.com"
 * val icon: ImageVector = qrContent.iconForQrContent()  // Returns Link icon
 * ```
 *
 * @receiver String - El contenido del QR como texto
 * @return ImageVector - El icono apropiado según el contenido
 */
fun String.iconForQrContent(): ImageVector = QrType.fromContent(this).icon