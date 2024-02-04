package com.vbevia.montanejosqr

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bevia.mfypro.utils.ScreenUtils
import com.google.android.material.snackbar.Snackbar
import com.vbevia.montanejosqr.qrevent.QREventActivityView
import com.vbevia.montanejosqr.registerevent.RegisterEventActivityView
import com.vbevia.montanejosqr.settings.SettingsActivity
import com.vbevia.montanejosqr.utils.SharePreferenceSingleton
import com.vbevia.montanejosqr.utils.TestingParamsSettings
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val PERMISSION_ID = 44
const val LOCATION_SERVICE_INTENT = "location service intent"

class MainActivity : AppCompatActivity() {

    //private var locationService: LocationService? = null
    //private var isServiceConnected: Boolean = false
    private lateinit var constraintLayout: LinearLayoutCompat
    private lateinit var upperCardEventsActivity: LinearLayoutCompat
    private lateinit var upperCardData: LinearLayoutCompat

    private lateinit var cardView1: CardView
    private lateinit var cardView2: CardView
    private lateinit var currentTime: TextView
    private lateinit var currentEvent: TextView
    private lateinit var currentDevice: TextView
    private lateinit var currentTerminalActivity: TextView
    private lateinit var version: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ScreenUtils.initialize(this)
        val screenLengthDp = ScreenUtils.getScreenLengthDp()

        //Toast.makeText(this, "screenLengthDp: $screenLengthDp", Toast.LENGTH_LONG).show()

        if(screenLengthDp < 790){
            setContentView(R.layout.activity_main_small)
        }else{
            setContentView(R.layout.activity_main)
        }

        SharePreferenceSingleton.setRegisterStatus(this@MainActivity, true)

        version = findViewById(R.id.version)
        upperCardEventsActivity = findViewById(R.id.upperCardEvents)
        upperCardData = findViewById(R.id.upperCard)
        cardView1 = findViewById(R.id.cardView1)
        cardView2 = findViewById(R.id.cardView2)

        currentTime = findViewById(R.id.current_time)
        currentEvent = findViewById(R.id.current_event)  //fuente de los baños, feria del queso, etc....
        currentDevice = findViewById(R.id.terminal_activity_name)
        currentTerminalActivity = findViewById(R.id.terminal_activity_name) //Barra, bebidas, entrada, hotel, etc....

        version.text = "versión: " + BuildConfig.VERSION_NAME

        currentEvent.text = SharePreferenceSingleton.getEventName(this)
        currentDevice.text = SharePreferenceSingleton.getEventTerminalName(this)
        //currentDevice.text = SharePreferenceSingleton.getDeviceName(this)

        // aforoTotal = findViewById(R.id.aforo_total)

        constraintLayout = findViewById(R.id.constraint_layout)
        upperCardEventsActivity.setOnClickListener {

        }

        upperCardData.setOnClickListener {
            val intent = Intent(this, QREventActivityView::class.java)
            startActivity(intent)
        }

        // Customize the card properties
        cardView1.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cardView1.setOnClickListener {

            val intent = Intent(this, QRReaderActivity::class.java)
            intent.putExtra(getString(R.string.qr_action), getString(R.string.enter))
            someActivityResultLauncher.launch(intent)
        }

        cardView2.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cardView2.setOnClickListener {

            val intent = Intent(this, QRReaderActivity::class.java)
            intent.putExtra(getString(R.string.qr_action), getString(R.string.exit))
            someActivityResultLauncher.launch(intent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentTime.text = getCurrentTime()
        } else {
            currentTime.text = getCurrentTimeOldVersions()
        }

        val mainTopImage: ImageView = findViewById(R.id.centeredImageView)
        mainTopImage.setOnLongClickListener {
            showAlertDialog(this)
            return@setOnLongClickListener true
        }

        val settingsTopImage: ImageView = findViewById(R.id.settingsImage)
        settingsTopImage.setOnLongClickListener {
            settingsResultLauncher.launch(Intent(this, SettingsActivity::class.java))
            return@setOnLongClickListener true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTime(): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        val currentDateTime = LocalDateTime.now()
        return currentDateTime.format(formatter)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTimeOldVersions(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm")
        val currentDate = Date()
        return sdf.format(currentDate)
    }

    private val settingsResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val mainIntent = Intent(this, RegisterEventActivityView::class.java)
                startActivity(mainIntent)
                finish()
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.d("DEBUG_CODEBAR", "MainActivity ---> resultData : RESULT_CANCELED")
            }
        }

    private val someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result from the called activity
                val data: Intent? = result.data
                val resultData = data?.getStringExtra("result_key")
                // Do something with the result
                Log.d("DEBUG_CODEBAR", "MainActivity ---> resultData result_key : $resultData")

                currentEvent.text = SharePreferenceSingleton.getEventName(this)
                customAlertDialog(resultData.toString())

                // Access the returned data using the "data" Intent object
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // Handle if the called activity was canceled
                Log.d("DEBUG_CODEBAR", "MainActivity ---> resultData : RESULT_CANCELED")
                customAlertDialog("QR - ERROR ")
            }
        }

    private fun showAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Introducir nueva API Url.")

        // Create an EditText view and set it as the content of the dialog
        val inputEditText = EditText(context)
        builder.setView(inputEditText)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog, _ ->

            val enteredText = inputEditText.text.toString()

            if (inputEditText.text.toString().length > 1) {
               SharePreferenceSingleton.setApiURL(this@MainActivity, enteredText)

                if(com.vbevia.montanejosqr.utils.URLValidator.isValidURL(enteredText)){
                    //changeBaseUrl(enteredText)
                    SharePreferenceSingleton.setApiURL(this@MainActivity, enteredText)
                    resetUuiSharedPreference()
                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()

                }else{
                    Toast.makeText(this@MainActivity,
                        "Url no valida."    , Toast.LENGTH_LONG)
                        .show()
                }

            } else {
                Toast.makeText(this@MainActivity,
                    "No se ha efectuado ningun cambio."    , Toast.LENGTH_LONG)
                    .show()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Handle the Cancel button click here
            dialog.dismiss()
        }

        // Create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun resetUuiSharedPreference() {
        GlobalScope.launch(Dispatchers.IO) {
            SharePreferenceSingleton.setTestingMode(this@MainActivity, false)
            TestingParamsSettings.resetSettings(this@MainActivity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
    }

    private fun customAlertDialog(message: String) {
        Snackbar.make( // be sure to pass in the layout that you use.
            constraintLayout,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

}