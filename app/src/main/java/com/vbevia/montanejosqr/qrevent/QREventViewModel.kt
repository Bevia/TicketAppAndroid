package com.vbevia.montanejosqr.qrevent

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vbevia.montanejosqr.network.RetrofitBuilder

class QREventViewModel  : ViewModel() {

    private val serviceSetterGetter = MutableLiveData<YourResponse>()
    val networkErrorLiveData: MutableLiveData<String> = MutableLiveData()

    suspend fun callPOSTMethod(uuid: String, eventUuid: String): MutableLiveData<YourResponse> {

        Log.v("DEBUGEVENTS : ", "callPOSTMethod uuid: $uuid eventUuid: $eventUuid")

        // Use coroutine scope to make the API call

        try {
            val response = RetrofitBuilder.apiLoginService.eventStatus(uuid, eventUuid)
            if (response.isSuccessful) {


                /*
                {
                    "people": {
                        "in_global": 2,
                        "in_today": 2,
                        "out_global": 1,
                        "out_today": 1
                    },
                    "event_capacity": {
                        "current": 49,
                        "limit": 50,
                        "temperature": "green"
                    }
                  }
                 */

                val yourResponse = response.body()

                val peopleInGlobal = yourResponse?.people?.inGlobal
                val peopleInToday = yourResponse?.people?.inToday
                val peopleOutGlobal = yourResponse?.people?.outGlobal
                val peopleOutToday = yourResponse?.people?.outToday

                val eventCurrentCapacity = yourResponse?.eventCapacity?.current
                val eventLimitCapacity = yourResponse?.eventCapacity?.limit
                val temperature = yourResponse?.eventCapacity?.temperature

                serviceSetterGetter.value = YourResponse(
                    QREventModel(
                        peopleInGlobal ?: 0,
                        peopleInToday ?: 0,
                        peopleOutGlobal ?: 0,
                        peopleOutToday ?: 0
                    ),
                    EventCapacity(
                        eventCurrentCapacity ?: 0,
                        eventLimitCapacity ?: 0,
                        temperature ?: "green"
                    )
                )

            } else {
                val errorBody = response.errorBody()?.string()
                Log.v("DEBUGEVENTS : ", "networkErrorLiveData errorBody: $errorBody")
                networkErrorLiveData.postValue(errorBody ?: "Unknown error")
            }
        } catch (e: Exception) {
            Log.v("DEBUGEVENTS : ", "networkErrorLiveData exception: " + e.message)
            networkErrorLiveData.postValue("Network error occurred " + e.message)
        }

        return serviceSetterGetter
    }
}