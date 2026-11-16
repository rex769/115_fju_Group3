package com.example.smartfridge.domain.model

enum class ItemActionState(val label: String) {
    IN_FRIDGE("在庫"),
    CONSUMED("已食用"),
    DISCARDED("已丟棄")
}
