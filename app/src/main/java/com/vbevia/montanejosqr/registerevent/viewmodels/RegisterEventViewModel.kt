package com.vbevia.montanejosqr.registerevent.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vbevia.montanejosqr.network.RetrofitBuilder
import com.vbevia.montanejosqr.registerevent.models.RegisterEventModel
import com.vbevia.montanejosqr.registerevent.models.TerminalResponse

class RegisterEventViewModel : ViewModel() {

        private val serviceSetterGetter = MutableLiveData<TerminalResponse>()
        val networkErrorLiveData: MutableLiveData<String> = MutableLiveData()

        suspend fun callPOSTMethod(uuid: String): MutableLiveData<TerminalResponse> {

            Log.v("DEBUG_REGISTER_EVENT : ", "RegisterDeviceViewModel uuid: $uuid")

            // Use coroutine scope to make the API call

            try {
                val response = RetrofitBuilder.apiLoginService.getTerminalActions(uuid)
                Log.v("DEBUG_REGISTER_EVENT : ", "endpoint: " + response.raw())
                if (response.isSuccessful) {
                    val responseData = response.body()

                    val items: List<RegisterEventModel>? = responseData?.items
                    //Log.v("DEBUG_REGISTER_EVENT : ", "items: $items")
                    serviceSetterGetter.value = items?.let { TerminalResponse(it) }

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.v("DEBUG_REGISTER_EVENT : ", "networkErrorLiveData errorBody: $errorBody")
                    networkErrorLiveData.postValue(errorBody ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.v("DEBUG_REGISTER_EVENT : ", "networkErrorLiveData exception: " + e.message)
                networkErrorLiveData.postValue("Network error occurred " + e.message)
            }

            return serviceSetterGetter
        }
    }

    /*
    possible errors

    {
     "message": "Invalid credentials."
    }


    success:

    {
    "items": [
        {
            "uuid": "1fae8d0e-1a20-37b0-a095-0b759ad42106",
            "name": "Barra",
            "phone": null,
            "location": null,
            "eventName": "Visita Fuente Baños"
        },
        {
            "uuid": "745c5bee-1a4b-33b9-91b9-a43d26ab2f44",
            "name": "Entrada principal",
            "phone": null,
            "location": null,
            "eventName": "Visita Fuente Baños"
        }
    ]
}

     */
