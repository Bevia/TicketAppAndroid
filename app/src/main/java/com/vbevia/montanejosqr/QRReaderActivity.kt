package com.vbevia.montanejosqr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.bevia.mfypro.utils.ScreenUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.camera.CameraSettings
import com.vbevia.montanejosqr.dbqrentries.AppDatabase
import com.vbevia.montanejosqr.dbqrentries.MainDBModel
import com.vbevia.montanejosqr.dbqrentries.RoomDataDao
import com.vbevia.montanejosqr.qrcheckin.QRCheckInViewModel
import com.vbevia.montanejosqr.qrcheckout.QRCheckOutViewModel
import com.vbevia.montanejosqr.utils.BeepObject.playBeep
import com.vbevia.montanejosqr.utils.NetworkCallbackImpl
import com.vbevia.montanejosqr.utils.SharePreferenceSingleton
import com.vbevia.montanejosqr.utils.ValidateURL
import com.vbevia.montanejosqr.utils.VibrateSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

//Montanejos project

class QRReaderActivity : AppCompatActivity() {

    ///https://github.com/journeyapps/zxing-android-embedded

    companion object {
        private const val TAG = "QRReaderActivity"
        private const val PERMISSION_REQUEST_CODE = 999
    }

    private lateinit var qrStatus: String
    private var permissionRejected: Boolean = false
    private lateinit var btnBack: Button
    private lateinit var qrView: DecoratedBarcodeView

    private lateinit var qRCheckInViewModel: QRCheckInViewModel
    private lateinit var qRCheckOutViewModel: QRCheckOutViewModel
    private val networkCallback = NetworkCallbackImpl()
    private val activityScope: CoroutineScope = lifecycleScope
    private var alertDialog: AlertDialog? = null
    private lateinit var progressBar: ProgressBar
    private var isBackButtonEnabled = true

    //QR Database stuff:
    private lateinit var database: AppDatabase
    private lateinit var roomDataDao: RoomDataDao

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")

        ScreenUtils.initialize(this)
        var screenLengthDp = ScreenUtils.getScreenLengthDp()

        if (screenLengthDp < 790) {
            setContentView(R.layout.activity_qrreader_small)
        } else {
            setContentView(R.layout.activity_qrreader)
        }

        initializeDB()

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        btnBack = findViewById(R.id.btnBack)
        qrView = findViewById(R.id.qr_view)

        // Initialize the ViewModel
        qRCheckInViewModel = ViewModelProvider(this)[QRCheckInViewModel::class.java]
        qRCheckOutViewModel = ViewModelProvider(this)[QRCheckOutViewModel::class.java]

        val extraKey = getString(R.string.qr_action)
        qrStatus = intent.getStringExtra(extraKey).toString()

        Log.d("DEBUG_CODEBAR", "qrReadStart ---> qrStatus: $qrStatus")

        btnBack.setOnClickListener {
            qrView.pause()
            progressBar.visibility = View.GONE
            finish()
        }

        //for testing purposes, this happens in checkin and checkout
        //SharePreferenceSingleton.setEventUuid(this@QRReaderActivity, "6b6d2c29-c54e-3186-8c0e-ad57ef275012")
        //SharePreferenceSingleton.setEventUuid(this@QRReaderActivity, "18709db4-0efb-334c-b862-8572c4e2cb21")

    }

    private fun getCurrentTime(): String {
        val currentTime = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat("hh:mm:ss")
        val date = Date(currentTime)
        val time = simpleDateFormat.format(date)

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Month is 0-based, so add 1
        val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        println("MainDBActivityView roomData Current date: $currentDayOfMonth/$currentMonth/$currentYear")
        return time
    }

    private fun initializeDB() {
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
            .build()
        roomDataDao = database.roomDataDao()
    }

    private fun setCamera() {
        qrView.decoderFactory =
            DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))

        val cameraSettings = qrView.cameraSettings
        cameraSettings.focusMode = CameraSettings.FocusMode.AUTO
        cameraSettings.focusMode = CameraSettings.FocusMode.CONTINUOUS

        qrView.resume()
        qrReadStart()

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: permissionRejected=$permissionRejected")
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onResume: permission.CAMERA is granted.")
            setCamera()
        } else {
            Log.d(TAG, "onResume: permission.CAMERA is required.")
            if (!permissionRejected) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Camera permission is required")
                    .setMessage("Camera permission is required. Restart app and grant camera permission.")
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
                    .show()
            }
        }
    }

    private fun qrReadStart() {
        Log.d(TAG, "qrReadStart: ")

        qrView.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(barcodeResult: BarcodeResult) {
                Log.d(TAG, "qrReadStart: barcodeResult.text=${barcodeResult.text}")
                qrView.pause()

                //TODO: check if QR code is valid and send it to the server

                // Check if the receivedString is not null before using it
                if (qrStatus == "checkin") {
                    Log.d("DEBUG_CODEBAR", "qrReadStart ---> checkin: ${barcodeResult.text}")
                    checkInAPIFunction(barcodeResult.text)
                } else if (qrStatus == "checkout") {
                    Log.d("DEBUG_CODEBAR", "qrReadStart ---> checkout: ${barcodeResult.text}")
                    checkOutAPIFunction(barcodeResult.text)
                }

                progressBar.visibility = View.VISIBLE
                progressBar.progress

                isBackButtonEnabled = false

                btnBack.isEnabled = false
                btnBack.isClickable = false
                btnBack.isFocusable = false
                btnBack.alpha = 0.5f

            }

            override fun possibleResultPoints(list: List<ResultPoint>) {}
        })
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun checkInAPIFunction(qr: String) {
        /**
         * for testing purposes
         */
        if (SharePreferenceSingleton.getTestingMode(this@QRReaderActivity) == true) {
            val roomData = MainDBModel(
                uuid = "uuid",
                concept = "Entrada gratis",
                state = "IN",
                availableUsages = "1",
                eventUuid = "6b6d2c29-c54e-3186-8c0e-ad57ef275012",
                time = getCurrentTime()
            )
            GlobalScope.launch(Dispatchers.IO) {
                roomDataDao.insert(roomData)
            }
        }
        /**
         *
         */

        val uuid = SharePreferenceSingleton.getUui(this@QRReaderActivity)
        // val urlToCheck = Endpoint.serverMainUrl
        val urlToCheck = SharePreferenceSingleton.getApiURL(this)
        val isValid = ValidateURL.validateURL(urlToCheck)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        Log.v("DEBUG_CODEBAR : ", "qr: $qr")
        Log.v("DEBUG_CODEBAR : ", "uuid: " + uuid.toString())
        Log.v(
            "DEBUG_CODEBAR : ",
            "check the network isAvailable: " + networkCallback.isAvailable() + " isValid: " + isValid
        )

        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && isValid) {

            activityScope.launch {

                /*
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

                qRCheckInViewModel.callPOSTMethod(uuid.toString(), qr)!!.observe(
                    this@QRReaderActivity
                ) { serviceSetterGetter ->
                    val uuid = serviceSetterGetter.uuid.toString()
                    val title = serviceSetterGetter.title.toString()
                    val eventUuid = serviceSetterGetter.eventUuid.toString()
                    val concept = serviceSetterGetter.concept.toString()
                    val currentUsages = serviceSetterGetter.currentUsages.toString()
                    val availableUsages = serviceSetterGetter.availableUsages.toString()
                    val maxUsages = serviceSetterGetter.maxUsages.toString()
                    val ignoreQuota = serviceSetterGetter.ignoreQuota.toString()
                    val allowAdmission = serviceSetterGetter.allowAdmission.toString()
                    val state = serviceSetterGetter.state.toString()

                    uuid.let { Log.d("DEBUG_CODEBAR checkout : uuid ------> ", it) }
                    title.let { Log.d("DEBUG_CODEBAR checkout : title ------> ", it) }
                    eventUuid.let { Log.d("DEBUG_CODEBAR checkout : eventUuid ------> ", it) }
                    concept.let { Log.d("DEBUG_CODEBAR checkout : concept ------> ", it) }
                    currentUsages.let {
                        Log.d(
                            "DEBUG_CODEBAR checkout : currentUsages ------> ",
                            it
                        )
                    }
                    availableUsages.let {
                        Log.d(
                            "DEBUG_CODEBAR checkout : availableUsages ------> ",
                            it
                        )
                    }
                    allowAdmission.let {
                        Log.d(
                            "DEBUG_CODEBAR checkout : allowAdmission ------> ",
                            it
                        )
                    }
                    state.let { Log.d("DEBUG_CODEBAR checkout : state ------> ", it) }

                    if (serviceSetterGetter.allowAdmission == true) {

                        SharePreferenceSingleton.setEventUuid(this@QRReaderActivity, eventUuid)

                        val roomData = MainDBModel(
                            uuid = uuid,
                            concept = concept,
                            state = state,
                            availableUsages = availableUsages,
                            eventUuid = eventUuid,
                            time = getCurrentTime()
                        )
                        GlobalScope.launch(Dispatchers.IO) {
                            roomDataDao.insert(roomData)
                            VibrateSingleton.vibrateDevice(this@QRReaderActivity)
                            playBeep()

                        }

                        val resultIntent = Intent()
                        resultIntent.putExtra("result_key", "checking_success")
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()

                    } else {
                        Toast.makeText(
                            this@QRReaderActivity,
                            "Admissión denegada.", Toast.LENGTH_LONG
                        ).show()

                        //VibrateSingleton.vibrateDevice(this@QRReaderActivity)
                        //playBeep()
                    }
                }
            }

                qRCheckInViewModel.networkErrorLiveData.observe(
                    this@QRReaderActivity
                ) {
                    showErrorDialog(it)
                }

            } else {
                Toast.makeText(this@QRReaderActivity, "Network is not available", Toast.LENGTH_LONG)
                    .show()
                finish()
            }
        }

        private fun showErrorDialog(it: String?) {

            progressBar.visibility = View.GONE

            isBackButtonEnabled = true

            btnBack.isEnabled = true
            btnBack.isClickable = true
            btnBack.isFocusable = true
            btnBack.alpha = 1.0f

            if (alertDialog == null) {

                val inflater = LayoutInflater.from(this@QRReaderActivity)
                val dialogView: View = inflater.inflate(R.layout.custom_dialog_layout, null)

                // Set the title text
                val titleTextView = dialogView.findViewById<TextView>(R.id.dialogTitle)
                titleTextView.text = "QR read result"

                val messageTextView = dialogView.findViewById<TextView>(R.id.dialogMessage)
                messageTextView.text = it

                val alertDialogBuilder = AlertDialog.Builder(this@QRReaderActivity)
                alertDialogBuilder.setView(dialogView)
                    .setCancelable(false) // Prevent dialog dismissal on outside touch or back button

                alertDialog = alertDialogBuilder.create()

                // Find UI elements and set their behavior
                val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
                closeButton.setOnClickListener {
                    val resultIntent = Intent()
                    resultIntent.putExtra("result_key", "QR error")
                    setResult(Activity.RESULT_OK, resultIntent)
                    alertDialog!!.dismiss() // Close the dialog
                    finish()
                }
            }

            if (!alertDialog?.isShowing!!) {
                alertDialog?.show()
            }

        }

        private fun checkOutAPIFunction(qr: String) {
            /**
             * for testing purposes
             */
            if (SharePreferenceSingleton.getTestingMode(this@QRReaderActivity) == true) {
                val roomData = MainDBModel(
                    uuid = "uuid",
                    concept = "Entrada gratis",
                    state = "OUT",
                    availableUsages = "0",
                    eventUuid = "6b6d2c29-c54e-3186-8c0e-ad57ef275012",
                    time = getCurrentTime()
                )
                GlobalScope.launch(Dispatchers.IO) {
                    roomDataDao.insert(roomData)
                }
            }

            /**
             *
             */

            val uuid = SharePreferenceSingleton.getUui(this@QRReaderActivity)
            //val urlToCheck = Endpoint.serverMainUrl
            val urlToCheck = SharePreferenceSingleton.getApiURL(this)
            val isValid = ValidateURL.validateURL(urlToCheck)

            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

            if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && isValid) {
                activityScope.launch {

                    qRCheckOutViewModel.callPOSTMethod(uuid.toString(), qr)!!.observe(
                        this@QRReaderActivity
                    ) { serviceSetterGetter ->
                        val uuid = serviceSetterGetter.uuid.toString()
                        val title = serviceSetterGetter.title.toString()
                        val eventUuid = serviceSetterGetter.eventUuid.toString()
                        val concept = serviceSetterGetter.concept.toString()
                        val currentUsages = serviceSetterGetter.currentUsages.toString()
                        val availableUsages = serviceSetterGetter.availableUsages.toString()
                        val allowAdmission = serviceSetterGetter.allowAdmission.toString()
                        val state = serviceSetterGetter.state.toString()

                        uuid?.let { Log.d("DEBUG_CODEBAR checkout : uuid ------> ", it) }
                        title?.let { Log.d("DEBUG_CODEBAR checkout : title ------> ", it) }
                        eventUuid?.let { Log.d("DEBUG_CODEBAR checkout : eventUuid ------> ", it) }
                        concept?.let { Log.d("DEBUG_CODEBAR checkout : concept ------> ", it) }
                        currentUsages?.let {
                            Log.d(
                                "DEBUG_CODEBAR checkout : currentUsages ------> ",
                                it
                            )
                        }
                        availableUsages?.let {
                            Log.d(
                                "DEBUG_CODEBAR checkout : availableUsages ------> ",
                                it
                            )
                        }
                        allowAdmission?.let {
                            Log.d(
                                "DEBUG_CODEBAR checkout : allowAdmission ------> ",
                                it
                            )
                        }
                        state?.let { Log.d("DEBUG_CODEBAR checkout : state ------> ", it) }

                        if (serviceSetterGetter.allowAdmission == true) {

                            SharePreferenceSingleton.setEventUuid(this@QRReaderActivity, eventUuid)

                            val roomData = MainDBModel(
                                uuid = uuid,
                                concept = concept,
                                state = state,
                                availableUsages = availableUsages,
                                eventUuid = eventUuid,
                                time = getCurrentTime()
                            )
                            GlobalScope.launch(Dispatchers.IO) {
                                roomDataDao.insert(roomData)
                                VibrateSingleton.vibrateDevice(this@QRReaderActivity)
                                playBeep()
                            }

                            val resultIntent = Intent()
                            resultIntent.putExtra("result_key", "checkout_success")
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@QRReaderActivity,
                                "Admissión denegada.", Toast.LENGTH_LONG
                            ).show()

                            //VibrateSingleton.vibrateDevice(this@QRReaderActivity)
                            //playBeep()
                        }
                    }
                }

                qRCheckOutViewModel.networkErrorLiveData.observe(
                    this@QRReaderActivity
                ) {
                    showErrorDialog(it)
                }

            } else {
                Toast.makeText(this@QRReaderActivity, "Network is not available", Toast.LENGTH_LONG)
                    .show()
                finish()
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            Log.d(
                TAG,
                "onRequestPermissionsResult: requestCode=$requestCode, permissions=${permissions[0]}, grantResults=${grantResults[0]}"
            )
            if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                permissionRejected = true
            }
        }

        override fun onPause() {
            super.onPause()
            Log.d(TAG, "onPause: ")
            qrView.pause()
        }

        //How To Migrate The Deprecated OnBackPressed Function
        //https://medium.com/tech-takeaways/how-to-migrate-the-deprecated-onbackpressed-function-e66bb29fa2fd
        private val onBackPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isBackButtonEnabled) {
                        isEnabled = false // Disable the callback temporarily
                        finish()
                        isEnabled = true // Re-enable the callback
                    }
                }
            }
    }

