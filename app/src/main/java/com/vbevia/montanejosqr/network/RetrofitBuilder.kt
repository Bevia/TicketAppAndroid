package com.vbevia.montanejosqr.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {

    /* private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val registerDeviceRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Endpoint.serverMainUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiLoginService : ApiServices by lazy{
        registerDeviceRetrofit.create(ApiServices::class.java)
    }*/

    private var _registerDeviceRetrofit: Retrofit? = null

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val registerDeviceRetrofit: Retrofit
        get() {
            if (_registerDeviceRetrofit == null) {
                _registerDeviceRetrofit = createRetrofit(Endpoint.serverMainUrl)
            }
            return _registerDeviceRetrofit!!
        }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Function to change the base URL
    fun changeBaseUrl(newBaseUrl: String) {
        _registerDeviceRetrofit = createRetrofit(newBaseUrl)
    }

    val apiLoginService: ApiServices by lazy {
        registerDeviceRetrofit.create(ApiServices::class.java)
    }
}