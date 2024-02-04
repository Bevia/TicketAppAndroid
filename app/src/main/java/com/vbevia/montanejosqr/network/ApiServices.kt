package com.vbevia.montanejosqr.network


import com.vbevia.montanejosqr.registerevent.models.TerminalResponse
import com.vbevia.montanejosqr.qrcheckin.QRCheckInModel
import com.vbevia.montanejosqr.qrcheckout.QRCheckOutModel
import com.vbevia.montanejosqr.qrevent.YourResponse
import com.vbevia.montanejosqr.registerevent.models.RegisterDeviceUuidModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ApiServices {

    /**
    When you define a function as "suspend" in Kotlin, it means that the function can be paused
    and resumed later without blocking the thread. This is particularly useful when performing
    asynchronous or long-running operations, such as making network requests with Retrofit.

    When you use Retrofit with Kotlin coroutines, you can define API methods as suspend functions,
    allowing you to make network calls asynchronously without blocking the main thread. By using
    the suspend modifier, Retrofit automatically integrates with Kotlin coroutines, enabling you
    to write clean and concise asynchronous code.

    functions here are marked with the "suspend" modifier. This indicates that the function
    can be suspended without blocking the thread while waiting for the network response.

     you cannot use suspend if you are using Call instead of Response!!!
     suspend is the coroutines keyword for asynchronous calls
     */

   /* @GET("api/device")
    suspend fun registerDevice(
        @Header("x-auth-token") uuid: String
    ) : Response<RegisterDeviceModel>*/

/*    @GET("api/device")
    suspend fun registerDevice(
        @Header("x-auth-token") uuid: String
    ) : Response<RegisterDeviceUuidModel>*/

    @GET("api/event/{event_uuid}/devices")
    suspend fun getTerminalActions(
        @Path("event_uuid") eventUuid: String
    ): Response<TerminalResponse>

    @GET("api/checkin/{checkinId}")
    suspend fun qrCheckIn(
        @Header("x-auth-token") uuid: String,
        @Path("checkinId") checkinId: String
    ): Response<QRCheckInModel>

    @GET("api/checkout/{checkoutId}")
    suspend fun qrCheckOut(
        @Header("x-auth-token") uuid: String,
        @Path("checkoutId") checkinId: String
    ): Response<QRCheckOutModel>

    @GET("api/event/{event_uuid}/status")
    suspend fun eventStatus(
        @Header("x-auth-token") uuid: String,
        @Path("event_uuid") eventUuid: String
    ): Response<YourResponse>
}