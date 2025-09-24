package com.develop.traiscore.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.utils.PaymentsUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.Wallet.WalletOptions
import com.google.android.gms.wallet.WalletConstants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.android.gms.wallet.PaymentData
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resume


class CheckoutViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val TAG = "CheckoutViewModel"
    private val dispatcher : CoroutineDispatcher = Dispatchers.Default
    private val payClient : PaymentsClient = Wallet.getPaymentsClient(
        application,
        WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build()
    )

    private val _paymentUiState : MutableStateFlow<PaymentUiState> = MutableStateFlow(PaymentUiState.NotStarted)
    val paymentUiState : StateFlow<PaymentUiState> = _paymentUiState.asStateFlow()

    init {
        Log.d(TAG, "ViewModel initialized, verifying Google Pay readiness...")
        viewModelScope.launch(dispatcher){
            verifyGooglePayReadiness()
        }
    }
    private suspend fun verifyGooglePayReadiness() {
        Log.d(TAG, "verifyGooglePayReadiness() called")
        val requestJson = PaymentsUtil.isReadyToPayRequest()
        Log.d(TAG, "isReadyToPayRequest JSON: $requestJson")

        val iRtpRequest = IsReadyToPayRequest.fromJson(requestJson.toString())
        val newState = try {
            val result = payClient.isReadyToPay(iRtpRequest).await()
            Log.d(TAG, "isReadyToPay result: $result")
            if (result) PaymentUiState.Available else {
                Log.w(TAG, "Google Pay not available on this device")
                PaymentUiState.Error(CommonStatusCodes.ERROR)
            }
        } catch (ae: ApiException) {
            Log.i( "Viewmodel", ae.status.statusMessage!!)
            PaymentUiState.Error(ae.statusCode)
        }
        _paymentUiState.update { newState }
        Log.d(TAG, "paymentUiState updated to: $newState")
    }


    fun startPaymentProcess(priceLabel: String): Task<PaymentData> {
        Log.d(TAG, "startPaymentProcess() called with priceLabel: $priceLabel")
        val pdRequestJson = PaymentsUtil.getPaymentDataRequest(priceLabel)
        Log.d(TAG, "PaymentDataRequest JSON: $pdRequestJson")
        val pdRequest = PaymentDataRequest.fromJson(pdRequestJson.toString())
        Log.d(TAG, "Returning loadPaymentData Task")
        return payClient.loadPaymentData(pdRequest)
    }

    abstract class PaymentUiState internal constructor() {
        object NotStarted : PaymentUiState() {
            override fun toString() = "NotStarted"
        }
        object Available : PaymentUiState() {
            override fun toString() = "Available"
        }
        class PaymentCompleted(val payerName: String) : PaymentUiState() {
            override fun toString() = "PaymentCompleted(payerName=$payerName)"
        }
        class Error(val code: Int, val message: String? = null) : PaymentUiState() {
            override fun toString() = "Error(code=$code, message=$message)"
        }
    }
    suspend fun <T> Task<T>.awaitTask(cancellationTokenSource: CancellationTokenSource? = null): Task<T> {
        Log.d(TAG, "awaitTask() called, isComplete=$isComplete")
        return if (isComplete) {
            this.also { Log.d(TAG, "Task already complete, returning immediately") }
        } else suspendCancellableCoroutine { cont ->
            addOnCompleteListener(DirectExecutor) { task ->
                Log.d(TAG, "Task completed with result: ${task.result}")
                cont.resume(task)
            }
            cancellationTokenSource?.let { cancellationSource ->
                cont.invokeOnCancellation {
                    Log.w(TAG, "Task was cancelled, cancelling token")
                    cancellationSource.cancel()
                }
            }
        }
    }
    private object DirectExecutor : Executor {
        override fun execute(r: Runnable){
            r.run()
        }
    }

}
