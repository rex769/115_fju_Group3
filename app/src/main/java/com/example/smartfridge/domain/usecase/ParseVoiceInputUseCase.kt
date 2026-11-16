package com.example.smartfridge.domain.usecase

import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ParsedVoiceResult(
    val foodName: String,
    val expiryDateMillis: Long
)

class ParseVoiceInputUseCase {

    fun parse(input: String): ParsedVoiceResult? {
        if (input.isBlank()) return null

        val now = Calendar.getInstance()

        // 1. 匹配「X月Y日到期」或「X月Y號到期」
        val datePattern = Pattern.compile("(.+?)\\s*(\\d{1,2})月(\\d{1,2})[日號]?(?:到期|過期)?")
        val dateMatcher = datePattern.matcher(input)
        if (dateMatcher.find()) {
            val name = dateMatcher.group(1)?.trim() ?: ""
            val month = dateMatcher.group(2)?.toIntOrNull() ?: (now.get(Calendar.MONTH) + 1)
            val day = dateMatcher.group(3)?.toIntOrNull() ?: now.get(Calendar.DAY_OF_MONTH)

            val targetCal = Calendar.getInstance().apply {
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                if (before(now)) {
                    add(Calendar.YEAR, 1)
                }
            }

            return ParsedVoiceResult(
                foodName = name.ifEmpty { "語音輸入食材" },
                expiryDateMillis = targetCal.timeInMillis
            )
        }

        // 2. 匹配「N天到期」
        val daysPattern = Pattern.compile("(.+?)\\s*(\\d{1,3})天(?:到期|過期)?")
        val daysMatcher = daysPattern.matcher(input)
        if (daysMatcher.find()) {
            val name = daysMatcher.group(1)?.trim() ?: ""
            val days = daysMatcher.group(2)?.toIntOrNull() ?: 3

            val targetMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days.toLong())
            return ParsedVoiceResult(
                foodName = name.ifEmpty { "語音輸入食材" },
                expiryDateMillis = targetMillis
            )
        }

        return ParsedVoiceResult(
            foodName = input.trim(),
            expiryDateMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
        )
    }
}
