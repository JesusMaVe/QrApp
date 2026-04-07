package com.example.qrapplication.barcode

import com.example.qrapplication.model.ContentType
import com.google.mlkit.vision.barcode.common.Barcode

object ContentTypeDetector {

    fun detect(barcode: Barcode): ContentType {
        return when (barcode.valueType) {
            Barcode.TYPE_URL -> ContentType.URL
            Barcode.TYPE_CONTACT_INFO -> ContentType.CONTACT
            Barcode.TYPE_WIFI -> ContentType.WIFI
            Barcode.TYPE_EMAIL -> ContentType.EMAIL
            Barcode.TYPE_PHONE -> ContentType.PHONE
            Barcode.TYPE_PRODUCT -> ContentType.PRODUCT
            Barcode.TYPE_TEXT -> ContentType.TEXT
            else -> {
                val rawValue = barcode.rawValue ?: return ContentType.UNKNOWN
                when {
                    rawValue.startsWith("http://") || rawValue.startsWith("https://") -> ContentType.URL
                    rawValue.startsWith("mailto:") -> ContentType.EMAIL
                    rawValue.startsWith("tel:") -> ContentType.PHONE
                    rawValue.startsWith("WIFI:") -> ContentType.WIFI
                    rawValue.startsWith("BEGIN:VCARD") || rawValue.startsWith("BEGIN:CONTACT") -> ContentType.CONTACT
                    rawValue.matches(Regex("^[0-9]+$")) -> ContentType.PRODUCT
                    else -> ContentType.TEXT
                }
            }
        }
    }
}
