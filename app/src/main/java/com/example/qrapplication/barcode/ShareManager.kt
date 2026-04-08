package com.example.qrapplication.barcode

import android.content.Context
import android.content.Intent
import android.widget.Toast

object ShareManager {

    fun shareText(context: Context, content: String, title: String = "Compartir") {
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
            }
            context.startActivity(Intent.createChooser(intent, title))
        }.onFailure {
            Toast.makeText(context, "No se pudo compartir", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareUrl(context: Context, url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
                putExtra(Intent.EXTRA_SUBJECT, "URL compartida")
            }
            context.startActivity(Intent.createChooser(intent, "Compartir enlace"))
        }.onFailure {
            Toast.makeText(context, "No se pudo compartir", Toast.LENGTH_SHORT).show()
        }
    }
}