package com.example.smartfridge.domain.model

enum class ExpiryStatus(val label: String) {
    RED("快到期/已過期"),
    YELLOW("需注意"),
    GREEN("新鮮")
}
