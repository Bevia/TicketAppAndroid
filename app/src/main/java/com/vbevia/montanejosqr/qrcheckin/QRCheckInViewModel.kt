package com.vbevia.montanejosqr.qrcheckin

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.vbevia.montanejosqr.network.RetrofitBuilder

class QRCheckInViewModel : ViewModel() {

    private val serviceSetterGetter = MutableLiveData<QRCheckInModel>()
    val networkErrorLiveData: MutableLiveData<String> = MutableLiveData()

    suspend fun callPOSTMethod(uuid: String, qr: String): MutableLiveData<QRCheckInModel> {

        Log.v("DEBUG_CODEBAR : ", "RegisterDeviceViewModel checking uuid: $uuid")
        Log.v("DEBUG_CODEBAR : ", "RegisterDeviceViewModel checking qr: $qr")

        // Use coroutine scope to make the API call

        try {
            val response = RetrofitBuilder.apiLoginService.qrCheckIn(uuid, qr)
            if (response.isSuccessful) {
                val responseData = response.body()

                /**

                {
                "uuid": "302325d2-b8ba-3973-8763-a4b6596d6632",
                "title": "Visita Fuente Baños",
                "event_uuid": "6b6d2c29-c54e-3186-8c0e-ad57ef275012",
                "concept": "Entrada 5 eur",
                "current_usages": 15,
                "available_usages": 85,
                "max_usages": 100,
                "ignore_quota": false,
                "allow_admission": true,
                "state": "MOVEMENT_IN"
                }

                 */

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

                serviceSetterGetter.value = QRCheckInModel(
                    uuid,
                    title,
                    eventUuid,
                    concept,
                    currentUsages,
                    availableUsages,
                    maxUsages,
                    ignoreQuota,
                    allowAdmission,
                    state
                )

            } else {
                val errorBody = response.errorBody()?.string()
                Log.v("DEBUG_CODEBAR : ", "networkErrorLiveData errorBody: $errorBody")

                val gson = Gson()
                val response = gson.fromJson(errorBody, ErrorResponse::class.java)

                response.message?.let {
                    Log.d(
                        "DEBUG_CODEBAR : message ------> ",
                        response.message
                    )
                    networkErrorLiveData.postValue(response.message)
                }
            }
        } catch (e: Exception) {
            networkErrorLiveData.postValue("Network exception occurred " + e.message)
            Log.v("DEBUG_CODEBAR : ", "networkErrorLiveData exception: " + e.message)
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
               errorBody:


               {
                    "status": 403,
                    "string_status": "error",
                    "message": "The device [Entrada principal] does not exist or not belong to event [e245eb12-c90b-3193-a60f-88dd139dac14]",
                    "data": []
                }



               {
                      "status":403,
                      "string_status":"error",
                      "message":"Este valor no es un UUID v\u00e1lido.",
                      "data":{
                         "secret":[
                            "Este valor no es un
                                               UUID v\u00e1lido.",
                            "The string \u0022qr_code\u0022 is not a valid Uuid for a \u0022Ticket\u0022 entity."
                         ]
                      }
                   }
                */

}