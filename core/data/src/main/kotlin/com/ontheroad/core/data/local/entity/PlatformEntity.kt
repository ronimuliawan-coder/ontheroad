package com.ontheroad.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ontheroad.core.model.Category
import com.ontheroad.core.model.Platform

@Entity(tableName = "platforms")
data class PlatformEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val isCustom: Boolean
) {
    fun toDomain(): Platform = Platform(
        id = id,
        name = name,
        colorHex = colorHex,
        isCustom = isCustom
    )

    companion object {
        fun fromDomain(platform: Platform): PlatformEntity = PlatformEntity(
            id = platform.id,
            name = platform.name,
            colorHex = platform.colorHex,
            isCustom = platform.isCustom
        )
    }
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String
) {
    fun toDomain(): Category = Category(
        id = id,
        name = name,
        iconName = iconName
    )

    companion object {
        fun fromDomain(category: Category): CategoryEntity = CategoryEntity(
            id = category.id,
            name = category.name,
            iconName = category.iconName
        )
    }
}
