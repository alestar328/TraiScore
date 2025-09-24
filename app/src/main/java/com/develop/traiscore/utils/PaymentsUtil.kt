package com.develop.traiscore.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import com.develop.traiscore.utils.Constants

object PaymentsUtil {

    private val baseRequest = JSONObject()
        .put("apiVersion", 2)
        .put("apiVersionMinor", 0)

    private val allowedCardAuthMethods = JSONArray(Constants.SUPPORTED_AUTH_METHODS)
    private val allowedCardNetworks = JSONArray(Constants.SUPPORTED_NETWORKS)

    private fun getTransactionInfo(price : String): JSONObject =
        JSONObject()
            .put("totalPrice", price)
            .put("totalPriceStatus", "FINAL")
            .put("countryCode", Constants.COUNTRY_CODE)
            .put("currencyCode", Constants.CURRENCY_CODE)


    private val merchantInfo : JSONObject =
        JSONObject().put("merchantName", "Example Merchant")

    fun getPaymentDataRequest(pricingLabel : String): JSONObject =
        baseRequest
            .put("allowedPaymentMethods", allowedPaymentMethods)
            .put("transactionInfo", getTransactionInfo(pricingLabel))
            .put("merchantInfo", merchantInfo)
            .put("shippingAddressRequired", false)

    private fun gatewayTokenizationSpecification(): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject(mapOf(
                "gateway" to "stripe",
                "stripe:version" to "2018-10-31",
                "stripe:publishableKey" to "pk_test_51SAqiuEL0nu0cngafwZYSQqpp4GxJNtYKYcxlIoAEWfFE3vqrnlOThdfNwK4e3ukUic1EXzIMjZZRs0BXmxz6RTT00Tc4mubXL"
                /*"gateway" to "example",
                "gatewayMerchantId" to "exampleGatewayMerchantId"*/
            )))
        }
    }

    private val cardPaymentMethod: JSONObject = baseCardPaymentMethod()
        .put("tokenizationSpecification", gatewayTokenizationSpecification())

    val allowedPaymentMethods: JSONArray = JSONArray().put(cardPaymentMethod)




    fun baseCardPaymentMethod() : JSONObject =
        JSONObject()
            .put("type", "CARD")
            .put("parameters", JSONObject()
                .put("allowedAuthMethods", allowedCardAuthMethods)
                .put("allowedCardNetworks", allowedCardNetworks)
            )




    fun isReadyToPayRequest() : JSONObject? =
        try {
            baseRequest.put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
        }catch (e: JSONException){
            null
        }
}