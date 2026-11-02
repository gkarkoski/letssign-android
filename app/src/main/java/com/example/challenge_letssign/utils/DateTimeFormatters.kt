package com.example.challenge_letssign.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormatters {
    private val dateTimePattern = SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.forLanguageTag("pt-BR"))
    private val dateOnlyPattern = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
    private val dateTimeAsPattern = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))

    fun formatTimestamp(timestampMillis: Long): String {
        return dateTimePattern.format(Date(timestampMillis))
    }

    fun formatDateOnly(timestampMillis: Long): String {
        return dateOnlyPattern.format(Date(timestampMillis))
    }

    fun formatTimestampWithAs(timestampMillis: Long): String {
        return dateTimeAsPattern.format(Date(timestampMillis))
    }
}
