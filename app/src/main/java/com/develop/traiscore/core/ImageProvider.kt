package com.develop.traiscore.core

import com.develop.traiscore.R

object ImageProvider {
    fun getImageForType(type: String): Int {
        return when (type.lowercase()) {
            "espalda", "tirón" -> R.drawable.back_pic
            "pecho", "empuje" -> R.drawable.chest_pic
            "pierna", "patas" -> R.drawable.legs_pic
            "gluteos", "culo" -> R.drawable.glutes_pic
            "brazos" -> R.drawable.arms_pic
            else -> R.drawable.back_pic
        }
    }
}