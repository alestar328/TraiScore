package com.develop.traiscore.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.develop.traiscore.presentation.screens.PricingScreenUI
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.viewmodels.CheckoutViewModel
import com.develop.traiscore.presentation.viewmodels.ThemeViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.contract.TaskResultContracts.GetPaymentDataResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CheckoutActivity : ComponentActivity() {

    private val model : CheckoutViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val paymentLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val paymentData = result.data?.let { PaymentData.getFromIntent(it) }
                    Log.i("CheckoutActivity", "SUCCESS: ${paymentData?.toJson()}")
                    finish()
                }
                Activity.RESULT_CANCELED -> {
                    Log.i("CheckoutActivity", "User canceled payment sheet")
                }
                else -> {
                    Log.e("CheckoutActivity", "Unexpected resultCode=${result.resultCode}")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            TraiScoreTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                val payState: CheckoutViewModel.PaymentUiState by model.paymentUiState.collectAsState()
                PricingScreenUI(
                    payUiState = payState,
                    onGooglePayClick = ::requestPayment,
                    onSkipClick = { finish() },      // 👈 cierra la Activity
                    onNotNowClick = { finish() }
                )
            }
        }
    }
    private fun requestPayment() {
        val task = model.startPaymentProcess("2.99")

        task.addOnCompleteListener { completed ->
            try {
                // Éxito directo (ocasional)
                val paymentData = completed.getResult(ApiException::class.java)
                Log.i("CheckoutActivity", "Immediate success: ${paymentData.toJson()}")
                finish()
            } catch (ex: ApiException) {
                when (ex.statusCode) {
                    CommonStatusCodes.RESOLUTION_REQUIRED -> {
                        val sender = ex.status?.resolution?.intentSender
                        if (sender != null) {
                            val req = IntentSenderRequest.Builder(sender).build()
                            paymentLauncher.launch(req) // 👈 mismo nombre
                        } else {
                            Log.e("CheckoutActivity", "No resolution available", ex)
                        }
                    }
                    CommonStatusCodes.CANCELED -> {
                        Log.i("CheckoutActivity", "User canceled (immediate)")
                    }
                    else -> {
                        Log.e("CheckoutActivity",
                            "ApiException code=${ex.statusCode} msg=${ex.status?.statusMessage}", ex)
                    }
                }
            } catch (t: Throwable) {
                Log.e("CheckoutActivity", "Unexpected error completing task", t)
            }
        }
    }

}

