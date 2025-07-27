package com.develop.traiscore.core

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.develop.traiscore.R

enum class DefaultCategoryExer(
    @StringRes val titleCat: Int,
    @DrawableRes val imageCat: Int
) {
    CHEST(R.string.category_chest,    R.drawable.chest_pic),
    GLUTES(R.string.category_glutes,  R.drawable.glutes_pic),
    BACK(R.string.category_back,      R.drawable.back_pic),
    LEGS(R.string.category_legs,      R.drawable.legs_pic),
    ARMS(R.string.category_arms,      R.drawable.arms_pic),
    SHOULDERS(R.string.category_shoulders, R.drawable.shoulders_pic),
    CORE(R.string.category_core,      R.drawable.core_pic)
}