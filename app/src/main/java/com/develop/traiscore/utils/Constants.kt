package com.develop.traiscore.utils

object Constants {

    const val PAYMENTS_ENVIRONMENT = ""


    val SUPPORTED_NETWORKS = listOf(
        "AMEX",
        "DISCOVER",
        "JCB",
        "MASTERCARD",
        "VISA"
    )
    const val CURRENCY_CODE = "USD"
    const val COUNTRY_CODE = "US"

    val SUPPORTED_AUTH_METHODS = listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS"
    )
}