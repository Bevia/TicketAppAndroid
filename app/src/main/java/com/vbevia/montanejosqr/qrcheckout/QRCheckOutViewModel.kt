package com.vbevia.montanejosqr.qrcheckout

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.vbevia.montanejosqr.network.RetrofitBuilder
import com.vbevia.montanejosqr.qrcheckin.QRCheckInModel
import com.vbevia.montanejosqr.qrcheckin.QRCheckInViewModel

class QRCheckOutViewModel : ViewModel() {

    private val serviceSetterGetter = MutableLiveData<QRCheckOutModel>()
    val networkErrorLiveData: MutableLiveData<String> = MutableLiveData()

    suspend fun callPOSTMethod(uuid: String, qr: String): MutableLiveData<QRCheckOutModel> {

        Log.v("DEBUG_CODEBAR : ", "RegisterDeviceViewModel checkout uuid: $uuid")
        Log.v("DEBUG_CODEBAR : ", "RegisterDeviceViewModel checkout qr: $qr")

        // Use coroutine scope to make the API call

        try {
            val response = RetrofitBuilder.apiLoginService.qrCheckOut(uuid, qr)
            if (response.isSuccessful) {
                val responseData = response.body()

                val uuid = responseData!!.uuid
                val title = responseData.title
                val eventUuid = responseData.eventUuid
                val concept = responseData.concept
                val currentUsages = responseData.currentUsages
                val availableUsages = responseData.availableUsages
                val maxUsages = responseData.maxUsages
                val ignoreQuota = responseData.ignoreQuota
                val allowAdmission = responseData.allowAdmission
                val state = responseData.state


                serviceSetterGetter.value = QRCheckOutModel(uuid,
                    title,
                    eventUuid,
                    concept,
                    currentUsages,
                    availableUsages,
                    maxUsages,
                    ignoreQuota,
                    allowAdmission,
                    state)

            } else {
                val errorBody = response.errorBody()?.string()
                Log.v("DEBUG_CODEBAR : ", "networkErrorLiveData errorBody: $errorBody")

                val gson = Gson()
                val response = gson.fromJson(errorBody, ErrorResponse::class.java)

                response.message?.let { Log.d("DEBUG_CODEBAR : message ------> ",
                    response.message
                )
                    networkErrorLiveData.postValue(response.message)
                }

            }
        } catch (e: Exception) {
            Log.v("DEBUG_CODEBAR : ", "networkErrorLiveData exception: " + e.message)
            networkErrorLiveData.postValue("Network error occurred " + e.message)
        }

        return serviceSetterGetter
    }

    data class ErrorResponse(
        val status: Int,
        val string_status: String,
        val message: String,
        val data: List<Any>
    )

    /*
    {"status":403,
    "string_status":"error",
    "message":"Max usages exceeded ticket exception for uuid
                            [4996e7c3-8e3e-3e13-89df-ea2879224c1e]",
                            "data":[]}
     */
}