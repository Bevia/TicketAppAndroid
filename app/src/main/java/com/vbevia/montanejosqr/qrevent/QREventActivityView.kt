package com.vbevia.montanejosqr.qrevent

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bevia.mfypro.utils.ScreenUtils
import com.vbevia.montanejosqr.R
import com.vbevia.montanejosqr.network.Endpoint
import com.vbevia.montanejosqr.utils.SharePreferenceSingleton
import com.vbevia.montanejosqr.utils.ValidateURL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class QREventActivityView : AppCompatActivity() {

    private val activityScope: CoroutineScope = lifecycleScope
    private lateinit var eventViewModel: QREventViewModel

    private lateinit var title: TextView
    private lateinit var inPeopleGlobalValue: TextView
    private lateinit var inPeopleTodayValue: TextView
    private lateinit var currentValue: TextView
    private lateinit var limitValue: TextView
    private lateinit var outPeopleGlobalValue: TextView
    private lateinit var outPeopleTodayValue: TextView
    private lateinit var exitButton: Button
    private lateinit var mainEventsDataView: LinearLayoutCompat
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarLoadingView: LinearLayoutCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ScreenUtils.initialize(this)
        var screenLengthDp = ScreenUtils.getScreenLengthDp()

        if (screenLengthDp < 790) {
            setContentView(R.layout.activity_qrevent_view_small)
        } else {
            setContentView(R.layout.activity_qrevent_view)
        }

        title = findViewById(R.id.title)
        if(SharePreferenceSingleton.getEventName(this@QREventActivityView)?.isNotEmpty() == true){
            title.text = SharePreferenceSingleton.getEventName(this@QREventActivityView)
        }

        mainEventsDataView = findViewById(R.id.main_events_data_view);
        mainEventsDataView.visibility = View.GONE
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        progressBar.progress
        progressBarLoadingView = findViewById(R.id.loading_view)
        progressBarLoadingView.visibility = View.VISIBLE

        exitButton = findViewById(R.id.btnExit)
        exitButton.setOnClickListener {
            finish()
        }

        inPeopleGlobalValue = findViewById(R.id.people_in_global_value)
        inPeopleTodayValue = findViewById(R.id.people_in_today_value)
        outPeopleGlobalValue = findViewById(R.id.people_out_global_value)
        outPeopleTodayValue = findViewById(R.id.people_out_today_value)

        currentValue = findViewById(R.id.current_value)
        limitValue = findViewById(R.id.limit_value)

        // Initialize the ViewModel
        eventViewModel = ViewModelProvider(this)[QREventViewModel::class.java]

        val uuid = SharePreferenceSingleton.getUui(this@QREventActivityView)
        val eventUuid = SharePreferenceSingleton.getEventUuid(this@QREventActivityView)
        //val urlToCheck = Endpoint.serverMainUrl
        val urlToCheck = SharePreferenceSingleton.getApiURL(this)
        val isValid = ValidateURL.validateURL(urlToCheck)

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && isValid) {

            activityScope.launch {

                eventViewModel.callPOSTMethod(uuid.toString(), eventUuid.toString()).observe(
                    this@QREventActivityView
                ) { serviceSetterGetter ->

                    val peopleInGlobal = serviceSetterGetter?.people?.inGlobal
                    inPeopleGlobalValue.text = peopleInGlobal.toString()

                    val peopleInToday = serviceSetterGetter?.people?.inToday
                    inPeopleTodayValue.text = peopleInToday.toString()

                    val peopleOutGlobal = serviceSetterGetter?.people?.outGlobal
                    outPeopleGlobalValue.text = peopleOutGlobal.toString()

                    val peopleOutToday = serviceSetterGetter?.people?.outToday
                    outPeopleTodayValue.text = peopleOutToday.toString()

                    val eventCurrentCapacity = serviceSetterGetter?.eventCapacity?.current
                    currentValue.text = eventCurrentCapacity.toString()

                    val eventLimitCapacity = serviceSetterGetter?.eventCapacity?.limit
                    limitValue.text = eventLimitCapacity.toString()

                    //val temperature = serviceSetterGetter?.eventCapacity?.temperature

                    progressBarLoadingView.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    mainEventsDataView.visibility = View.VISIBLE

                }
            }

            eventViewModel.networkErrorLiveData.observe(
                this@QREventActivityView
            ) {
                eventViewModel.networkErrorLiveData.value?.let {
                    Log.d(
                        "DEBUG_EVENTS checking : ",
                        "ERROR ------> $it"
                    )
                }

                progressBarLoadingView.visibility = View.GONE
                progressBar.visibility = View.GONE
                mainEventsDataView.visibility = View.VISIBLE

            }

        } else {
            Toast.makeText(this@QREventActivityView, "DEBUG_EVENTS: Network is not available", Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }
}