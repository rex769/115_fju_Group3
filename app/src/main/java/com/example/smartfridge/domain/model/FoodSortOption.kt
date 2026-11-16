package com.example.smartfridge.domain.model

enum class FoodSortField(val label: String) {
    EXPIRY_DATE("到期時間"),
    ADDED_DATE("購買時間")
}

enum class FoodSortOrder(val label: String) {
    ASCENDING("近的先"),
    DESCENDING("遠的先")
}
