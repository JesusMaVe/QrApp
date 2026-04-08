package com.example.qrapplication.barcode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import com.example.qrapplication.model.ContentType

object ActionHandler {

    fun execute(context: Context, content: String, contentType: ContentType) {
        when (contentType) {
            ContentType.URL -> openUrl(context, ensureUrlScheme(content))
            ContentType.EMAIL -> openEmail(context, content)
            ContentType.PHONE -> openDialer(context, content)
            ContentType.TEXT -> copyToClipboard(context, content, "Texto escaneado")
            ContentType.PRODUCT -> searchProduct(context, content)
            ContentType.WIFI -> copyToClipboard(context, content, "Red WiFi")
            ContentType.CONTACT -> openContact(context, content)
            ContentType.UNKNOWN -> copyToClipboard(context, content, "Contenido escaneado")
        }
    }

    private fun openUrl(context: Context, url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openEmail(context: Context, content: String) {
        runCatching {
            val address = content.removePrefix("mailto:").removePrefix("MAILTO:")
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$address")
            }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "No hay app de email disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openDialer(context: Context, content: String) {
        runCatching {
            val number = content.removePrefix("tel:").removePrefix("TEL:")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchProduct(context: Context, content: String) {
        runCatching {
            val query = Uri.encode(content)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "No se pudo buscar el producto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openContact(context: Context, content: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "No se pudo abrir la app de contactos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(context: Context, content: String, label: String) {
        runCatching {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Error al copiar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ensureUrlScheme(url: String): String {
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }
    }
}
