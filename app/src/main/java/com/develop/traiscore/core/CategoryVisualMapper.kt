package com.develop.traiscore.core

object CategoryVisualMapper {
    val visualCategories = DefaultCategoryExer.entries.map { category ->
        VisualCategory(
            category = category,
            nameResId = category.stringResId,
            imageResId = DefaultCategoryImageMapper.getImageFor(category)
        )
    }
}