package com.example.qrapplication.model

enum class ContentType(
    val displayName: String,
    val actionLabel: String
) {
    URL("URL", "Abrir enlace"),
    CONTACT("Contacto", "Agregar contacto"),
    PRODUCT("Producto", "Buscar producto"),
    TEXT("Texto", "Copiar texto"),
    WIFI("Red WiFi", "Conectar"),
    EMAIL("Email", "Enviar email"),
    PHONE("Teléfono", "Llamar"),
    UNKNOWN("Desconocido", "Copiar")
}
