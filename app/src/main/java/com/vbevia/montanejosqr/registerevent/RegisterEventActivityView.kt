package com.vbevia.montanejosqr.registerevent

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bevia.mfypro.utils.ScreenUtils
import com.vbevia.montanejosqr.MainActivity
import com.vbevia.montanejosqr.R
import com.vbevia.montanejosqr.dbuuidevents.EventsDatabase
import com.vbevia.montanejosqr.dbuuidevents.RoomEventsUuidDataDao
import com.vbevia.montanejosqr.dbuuidevents.EventsUuidDBModel
import com.vbevia.montanejosqr.network.RetrofitBuilder.changeBaseUrl
import com.vbevia.montanejosqr.registerevent.adapters.ItemAdapter
import com.vbevia.montanejosqr.registerevent.adapters.RegisterEventAdapter
import com.vbevia.montanejosqr.registerevent.models.RegisterEventModel
import com.vbevia.montanejosqr.registerevent.viewmodels.RegisterEventViewModel
import com.vbevia.montanejosqr.utils.DividerItemDecoration
import com.vbevia.montanejosqr.utils.NetworkCallbackImpl
import com.vbevia.montanejosqr.utils.SharePreferenceSingleton
import com.vbevia.montanejosqr.utils.SharePreferenceSingleton.setEventName
import com.vbevia.montanejosqr.utils.SharePreferenceSingleton.setEventTerminalName
import com.vbevia.montanejosqr.utils.SharePreferenceSingleton.setUui
import com.vbevia.montanejosqr.utils.TestingParamsSettings
import com.vbevia.montanejosqr.utils.ValidateURL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("NAME_SHADOWING")
class RegisterEventActivityView : AppCompatActivity(), ItemAdapter.OnItemClickListener {

    private var count: Int = 0
    private lateinit var editText: EditText
    private lateinit var customViewContainer: FrameLayout
    private lateinit var popupView: View
    private lateinit var mRegisterEventUuidViewModel: RegisterEventViewModel

    private lateinit var addUuidView: CardView
    private lateinit var loadingView: LinearLayoutCompat

    private val activityScope: CoroutineScope = lifecycleScope

    private val DELAY = 2000
    private var keep = true
    private var customViewContainerLive: Boolean = false

    private lateinit var mainEventsView: LinearLayoutCompat
    private lateinit var deviceRecyclerView: LinearLayoutCompat
    private lateinit var deviceRecycler: RecyclerView
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var deviceAdapter: ItemAdapter
    private lateinit var deviceCancelButton: Button

    private lateinit var progressBar: ProgressBar
    private lateinit var deviceProgressBar: ProgressBar

    private var alertDialog: AlertDialog? = null
    private lateinit var image: ImageView

    private lateinit var adapter: RegisterEventAdapter
    private lateinit var btnEventos: Button

    private lateinit var eventSelection: TextView

    //Events Database stuff:
    private lateinit var eventDatabase: EventsDatabase
    private lateinit var eventRoomDataDao: RoomEventsUuidDataDao

    private val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkCallback = NetworkCallbackImpl()
    private lateinit var registerDevice: Button

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen: SplashScreen = installSplashScreen()

        keep = true
        splashScreen.setKeepOnScreenCondition { keep }
        GlobalScope.launch {
            delay(DELAY.toLong())
            keep = false
        }

        ScreenUtils.initialize(this)
        var screenLengthDp = ScreenUtils.getScreenLengthDp()

        if (screenLengthDp < 790) {
            setContentView(R.layout.activity_register_device_view_small)
        } else {
            setContentView(R.layout.activity_register_device_view)
        }

      /*  Toast.makeText(
            this@RegisterDeviceView,
            "API url: " +  SharePreferenceSingleton.getApiURL(this),
            Toast.LENGTH_SHORT
        ).show()*/

        // Initialize the ViewModel
        mRegisterEventUuidViewModel = ViewModelProvider(this)[RegisterEventViewModel::class.java]

        customViewContainer = findViewById(R.id.custom_view_container)

        mainEventsView = findViewById(R.id.main_eventos_view)
        deviceRecycler = findViewById(R.id.device_recycler)
        deviceRecyclerView = findViewById(R.id.recycler_layout_view)

        addUuidView = findViewById(R.id.add_uuid_view)
        loadingView = findViewById(R.id.loading_view)
        editText = findViewById(R.id.editText)
        eventSelection = findViewById(R.id.seleccion)

        initializeDataBase()
        initializeAdapter()

        //this UUID are events, token and event name, this one goes to the DB as default
        //checkIfEventExist("6b6d2c29-c54e-3186-8c0e-ad57ef275012", "Fuente de los baños")
        //editText.setText("6b6d2c29-c54e-3186-8c0e-ad57ef275012")
        //editText.setText("745c5bee-1a4b-33b9-91b9-a43d26ab2f44")
        //SharePreferenceSingleton.setUui(this@RegisterDeviceView, "745c5bee-1a4b-33b9-91b9-a43d26ab2f44")
        checkEventsDataBase()
        //readAllEvents()

        deviceCancelButton = findViewById(R.id.btnDeviceSelectionCancel)
        deviceCancelButton.setOnClickListener {

            deviceRecyclerView.visibility = View.GONE
            mainEventsView.visibility = View.VISIBLE

        }

        /**
         * for testing purpose, hide once in production
         */
        image = findViewById(R.id.image)
        image.setOnLongClickListener {
            if (SharePreferenceSingleton.getTestingMode(this@RegisterEventActivityView) == true) {
                SharePreferenceSingleton.setTestingMode(this@RegisterEventActivityView, false)
                TestingParamsSettings.resetSettings(this@RegisterEventActivityView)
            } else {
                SharePreferenceSingleton.setTestingMode(this@RegisterEventActivityView, true)
                TestingParamsSettings.settingsOn(this@RegisterEventActivityView)
                deviceRegisteredGoToQRScannerView()
            }
            Toast.makeText(
                this@RegisterEventActivityView,
                "Testing mode: " + SharePreferenceSingleton.getTestingMode(this),
                Toast.LENGTH_SHORT
            ).show()
            true
        }

        if ((SharePreferenceSingleton.getTestingMode(this@RegisterEventActivityView) == true) ||
            (SharePreferenceSingleton.getRegisterStatus(this@RegisterEventActivityView) == true)) {
            deviceRegisteredGoToQRScannerView()
        }
        /**
         *
         */

        progressBar = findViewById(R.id.progressBar)
        deviceProgressBar = findViewById(R.id.device_progressBar)

        setInternetListener()

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // val textToPaste = "Text to paste"
        // val clipData = ClipData.newPlainText("label", textToPaste)
        // clipboardManager.setPrimaryClip(clipData)

        val pasteIntent = clipboardManager.primaryClip?.getItemAt(0)?.text
        if (pasteIntent != null) {
            setVisibilityVisible()
            editText.setText(pasteIntent)
        }

        btnEventos = findViewById(R.id.btnVerEventos)
        btnEventos.setOnClickListener {

            customViewContainerLive = !customViewContainerLive;
            customViewContainer.removeAllViews();

            setVisibilityVisible()

            if (customViewContainerLive) {
                customViewContainer.addView(popupView);

                registerDevice.visibility = View.GONE
                addUuidView.visibility = View.GONE
                loadingView.visibility = View.GONE

            }

        }

        registerDevice = findViewById(R.id.btnRegisterDevice)
        registerDevice.setOnClickListener {
            if (editText.text.toString().length > 1) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress

                checkIfEventExist(editText.text.toString(), "evento uuid")

                setVisibilityVisible()
                initializedTheViewModel()
            } else {
                Toast.makeText(this@RegisterEventActivityView,
                    getString(R.string.debes_introducir_el_uuid)    , Toast.LENGTH_LONG)
                    .show()

                //@TODO for testing
               // editText.setText("1fae8d0e-1a20-37b0-a095-0b759ad42106")
               // SharePreferenceSingleton.setUui(this@RegisterDeviceView, "1fae8d0e-1a20-37b0-a095-0b759ad42106")
               // progressBar.visibility = View.VISIBLE
               // progressBar.progress
                //initializedTheViewModel()
            }
        }

        deviceProgressBar.visibility = View.GONE
        progressBar.visibility = View.GONE
        deviceRecyclerView.visibility = View.GONE
        mainEventsView.visibility = View.VISIBLE
    }

    private fun setVisibilityVisible() {
        registerDevice.visibility = View.VISIBLE
        addUuidView.visibility = View.VISIBLE
        loadingView.visibility = View.VISIBLE
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun checkIfEventExist(uuid: String, event: String) {
        GlobalScope.launch(Dispatchers.IO) {

            val entryCount = eventDatabase.roomUuidDataDao().doesEntryExist(uuid)
            if (entryCount > 0) {
                // The entry with the specified name exists in the database
              /*  runOnUiThread {
                    Toast.makeText(applicationContext, "Este evento ya existe.", Toast.LENGTH_SHORT)
                        .show()
                }*/
            } else {
                // The entry does not exist in the database
                insertNewEvent(uuid, event)
            }
        }
    }

    private fun initializeDeviceAdapter(items: List<RegisterEventModel>) {

        val dividerDrawableResId = R.drawable.divider
        val itemDecoration = DividerItemDecoration(this, dividerDrawableResId)

        deviceRecycler.addItemDecoration(itemDecoration)
        val adapter = ItemAdapter(items, this)
        deviceRecycler.adapter = adapter
        deviceRecycler.layoutManager = LinearLayoutManager(this)

    }

    override fun onItemClick(item: RegisterEventModel) {
        // Handle item click here
        //Toast.makeText(this, "Item clicked: ${item.uuid}", Toast.LENGTH_SHORT).show()
        deviceProgressBar.visibility = View.VISIBLE

        item.uuid?.let { setUui(this, it) }
        item.eventName?.let { setEventName(this, it) }
        item.name?.let { setEventTerminalName(this, it) }

        deviceRegisteredGoToQRScannerView()

    }

    private fun initializeAdapter(){

        val inflater = LayoutInflater.from(this)
        popupView = inflater.inflate(R.layout.eventos_popup_layout, null)

        val dividerDrawableResId = R.drawable.divider
        val itemDecoration = DividerItemDecoration(this, dividerDrawableResId)

        eventRecyclerView = popupView.findViewById(R.id.eventsRecycler)
        eventRecyclerView.addItemDecoration(itemDecoration)
        adapter = RegisterEventAdapter(emptyList()) { selectedItem, position ->
          //  Toast.makeText(this, "Clicked: ${selectedItem.event} Position: $position", Toast.LENGTH_SHORT).show()
            editText.setText(selectedItem.uuid)
            customViewContainerLive = false
            setVisibilityVisible()
            customViewContainer.removeAllViews()
        }
        eventRecyclerView.adapter = adapter
        eventRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun initializeDataBase(){
        eventDatabase = Room.databaseBuilder(applicationContext, EventsDatabase::class.java, "events_database").build()
        eventRoomDataDao = eventDatabase.roomUuidDataDao()
    }

    private fun checkEventsDataBase(){
        eventRoomDataDao.getAllRoomDataDescending().observe(this) { roomDataList ->

            if(roomDataList.isEmpty()){
                eventSelection.text = "No ha eventos guardados."
                btnEventos.isFocusable = false
                btnEventos.isClickable = false
                btnEventos.alpha = 0.3f
            }else{
                eventSelection.text = "Selecciona un evento"
                adapter.dataList = roomDataList
                adapter.notifyDataSetChanged()
                btnEventos.isFocusable = true
                btnEventos.isClickable = true
                btnEventos.alpha = 1.0f
            }
        }
    }

    private fun initializedTheViewModel() {
        val urlToCheck = SharePreferenceSingleton.getApiURL(this)
        changeBaseUrl(urlToCheck.toString())
        val isValid = ValidateURL.validateURL(urlToCheck)

        Log.v(
            "DEBUG_REGISTER_EVENT : ",
            "check the network isAvailable: " + networkCallback.isAvailable() + " isValid: " + isValid
        )

        if (isValid) {
            activityScope.launch {

                mRegisterEventUuidViewModel.callPOSTMethod(editText.text.toString()).observe(
                    this@RegisterEventActivityView
                ) { serviceSetterGetter ->
                    val items = serviceSetterGetter.items

                    items.takeIf { it.isNotEmpty() }?.let { itemList ->
                        for (item in itemList) {
                            val uuid = item.uuid
                            val name = item.name
                            val phone = item.phone
                            val location = item.location
                            val eventName = item.eventName

                            // Now you can use individual item properties as needed
                            println("DEBUG_REGISTER_EVENT UUID: $uuid, Name: $name, Phone: $phone, Location: $location, Event Name: $eventName")

                        }
                    }

                    //save eventNama and uuid to database
                    checkIfEventExist(editText.text.toString(), "Evento uuid")

                    progressBar.visibility = View.GONE
                    deviceRecyclerView.visibility = View.VISIBLE
                    mainEventsView.visibility = View.GONE

                    initializeDeviceAdapter(items)

                }
            }

            mRegisterEventUuidViewModel.networkErrorLiveData.observe(
                this@RegisterEventActivityView
            ) { it ->
                //Toast.makeText(this@RegisterDeviceView, it, Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                showErrorDialog(it)
                mRegisterEventUuidViewModel.networkErrorLiveData.value?.let {
                    Log.d(
                        "DEBUG_REGISTER_EVENT : ",
                        "ERROR ------> $it"
                    )
                }

            }

        } else {
            // Toast.makeText(this@RegisterDeviceView, "Network is not available", Toast.LENGTH_LONG).show()
            progressBar.visibility = View.GONE
            showErrorDialog("Network is not available")

        }
    }

    private fun showErrorDialog(it: String?) {

        progressBar.visibility = View.GONE

        if (alertDialog == null) {

            val inflater = LayoutInflater.from(this@RegisterEventActivityView)
            val dialogView: View = inflater.inflate(R.layout.custom_dialog_layout, null)

            // Set the title text
            val titleTextView = dialogView.findViewById<TextView>(R.id.dialogTitle)
            titleTextView.text = getString(R.string.error_de_conexi_n)

            val messageTextView = dialogView.findViewById<TextView>(R.id.dialogMessage)
            messageTextView.text = it

            val alertDialogBuilder = AlertDialog.Builder(this@RegisterEventActivityView)
            alertDialogBuilder.setView(dialogView)
                .setCancelable(false) // Prevent dialog dismissal on outside touch or back button

            alertDialog = alertDialogBuilder.create()

            // Find UI elements and set their behavior
            val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
            closeButton.setOnClickListener {
                alertDialog?.dismiss()
            }
        }

        if (!alertDialog?.isShowing!!) {
            alertDialog?.show()
        }

    }

    private fun deviceRegisteredGoToQRScannerView() {
        startActivity(Intent(this@RegisterEventActivityView, MainActivity::class.java))
        finish()
    }

    private fun setInternetListener() {
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun isEntryExist(valueToCheck: String): Boolean {
        GlobalScope.launch(Dispatchers.IO) {
            count = eventDatabase.roomUuidDataDao().doesEntryExist(valueToCheck)
        }
        return count > 0
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun insertNewEvent(editTextUuid: String, apiEvent: String){
        val roomData = EventsUuidDBModel(
            uuid = editTextUuid,
            event = apiEvent
        )
        GlobalScope.launch(Dispatchers.IO) {
            eventRoomDataDao.insert(roomData)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun readAllEvents() {
        eventRoomDataDao.getAllRoomDataDescending().observe(this) { roomDataList ->
            for (roomData in roomDataList) {
                Log.d("DEBUG_UUID_EVENTS", "roomData: $roomData")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister the network callback when the app is in the background
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the network callback when the app is no longer using it
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
