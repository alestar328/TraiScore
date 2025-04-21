package com.develop.traiscore.core

import com.develop.traiscore.R

object DefaultCategoryImageMapper {
    fun getImageFor(category: DefaultCategoryExer): Int {
        return when (category) {
            DefaultCategoryExer.CHEST -> R.drawable.chest_pic
            DefaultCategoryExer.GLUTES -> R.drawable.glutes_pic
            DefaultCategoryExer.BACK -> R.drawable.back_pic
            DefaultCategoryExer.LEGS -> R.drawable.legs_pic
            DefaultCategoryExer.ARMS -> R.drawable.arms_pic
            DefaultCategoryExer.SHOULDERS -> R.drawable.shoulders_pic
            DefaultCategoryExer.CORE -> R.drawable.core_pic
        }
    }
}