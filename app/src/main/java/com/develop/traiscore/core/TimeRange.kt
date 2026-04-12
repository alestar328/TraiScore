package com.develop.traiscore.core

enum class TimeRange(val displayName: String, val days: Int) {
    TWO_WEEKS("2 sem", 14),
    ONE_MONTH("1 mes", 30),
    TWO_MONTHS("2 meses", 60),
    SIX_MONTHS("6 meses", 180),
    ONE_YEAR("1 año", 365)
}