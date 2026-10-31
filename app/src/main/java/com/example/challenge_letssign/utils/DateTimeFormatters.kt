package com.example.challenge_letssign.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormatters {
    private val dateTimePattern = SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.forLanguageTag("pt-BR"))

    fun formatTimestamp(timestampMillis: Long): String {
        return dateTimePattern.format(Date(timestampMillis))
    }
}
