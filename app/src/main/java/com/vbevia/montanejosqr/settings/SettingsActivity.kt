package com.vbevia.montanejosqr.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.room.Room
import com.bevia.mfypro.utils.ScreenUtils
import com.vbevia.montanejosqr.R
import com.vbevia.montanejosqr.constants.Constants
import com.vbevia.montanejosqr.dbqrentries.AppDatabase
import com.vbevia.montanejosqr.dbqrentries.MainDBActivityView
import com.vbevia.montanejosqr.dbqrentries.RoomDataDao
import com.vbevia.montanejosqr.dbuuidevents.EventsDatabase
import com.vbevia.montanejosqr.dbuuidevents.RoomEventsUuidDataDao
import com.vbevia.montanejosqr.utils.SharePreferenceSingleton
import com.vbevia.montanejosqr.utils.TestingParamsSettings
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private var alertDialog: AlertDialog? = null
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var currentUuid: TextView
    private lateinit var resetingUuidText: TextView
    private lateinit var borrarHistorialText: TextView
    private lateinit var listadoUuiTextview: TextView
    private lateinit var btnHistorial: Button
    private lateinit var btnResetUuid: Button
    private lateinit var btnReturn: Button
    private lateinit var btnDeleteUuid: Button
    private lateinit var btnCleanDB: Button
    private lateinit var resetTextButtonView: LinearLayoutCompat
    private lateinit var deleteUuidView: LinearLayoutCompat

    private var screenLengthDp = ScreenUtils.getScreenLengthDp()

    //Events Database stuff:
    private lateinit var eventDatabase: EventsDatabase
    private lateinit var eventRoomDataDao: RoomEventsUuidDataDao


    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ScreenUtils.initialize(this)

        if (screenLengthDp < 790) {
            setContentView(R.layout.settings_activity_small)
        } else {
            setContentView(R.layout.settings_activity)
        }

        //initialize DB
        val dao = initDB()
        initializeDataBase()
        eventsDBUISetUp()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(Constants.UUI, Context.MODE_PRIVATE)
        resetingUuidText = findViewById(R.id.resetingUuidText)
        listadoUuiTextview = findViewById(R.id.listado_uui_textview)
        currentUuid = findViewById(R.id.currentUuid)
        currentUuid.text = SharePreferenceSingleton.getUui(this)
        deleteUuidView = findViewById(R.id.delete_uuid_view)
        resetTextButtonView = findViewById(R.id.reset_text_button_view)
        btnHistorial = findViewById(R.id.btnHistorial)
        btnHistorial.setOnClickListener {
            val intent = Intent(this, MainDBActivityView::class.java)
            startActivity(intent)
        }

        btnDeleteUuid = findViewById(R.id.btnBorrarUuid)
        btnDeleteUuid.setOnClickListener {
            val intent = Intent(this, UUIDList::class.java)
            startActivity(intent)
        }

        borrarHistorialText = findViewById(R.id.borrarHistorialText)
        btnCleanDB = findViewById(R.id.btnBorrarHistorial)
        var rowCount: Int

        GlobalScope.launch(Dispatchers.IO) {
            rowCount = dao.getRowCount()
            runOnUiThread {
                //Toast.makeText(this@SettingsActivity, "rowCount: $rowCount", Toast.LENGTH_SHORT).show()
                btnCleanDB.isEnabled = true
                btnCleanDB.isClickable = true
                btnCleanDB.isFocusable = true
                btnCleanDB.alpha = 1.0f
                borrarHistorialText.alpha = 1.0f

                if (rowCount < 1) {
                    btnCleanDB.isEnabled = false
                    btnCleanDB.isClickable = false
                    btnCleanDB.isFocusable = false
                    btnCleanDB.alpha = 0.5f
                    borrarHistorialText.alpha = 0.5f
                }
            }
        }

        btnCleanDB.setOnClickListener {
            showAlertDialog(
                getString(R.string.borrar_base_de_datos_title),
                getString(R.string.borrar_base_de_datos_message),
                "db"
            )
        }

        btnResetUuid = findViewById(R.id.btnResetUuid)
        btnResetUuid.setOnClickListener {
            showAlertDialog(
                getString(R.string.reset_uuid_tltle),
                getString(R.string.resetear_uuid_message),
                "uuid"
            )
        }

        btnReturn = findViewById(R.id.btnReturn)
        btnReturn.setOnClickListener {
            finish()
        }
    }

    private fun eventsDBUISetUp() {
        eventRoomDataDao.getAllRoomDataDescending().observe(this) { roomDataList ->
            if (roomDataList.isEmpty()) {

                btnDeleteUuid.isEnabled = false
                btnDeleteUuid.isClickable = false
                btnDeleteUuid.isFocusable = false
                btnDeleteUuid.alpha = 0.5f
                listadoUuiTextview.alpha = 0.5f

            } else {
                btnDeleteUuid.isEnabled = true
                btnDeleteUuid.isClickable = true
                btnDeleteUuid.isFocusable = true
                btnDeleteUuid.alpha = 1.0f
                listadoUuiTextview.alpha = 1.0f
            }
        }
    }

    private fun initializeDataBase() {
        eventDatabase =
            Room.databaseBuilder(applicationContext, EventsDatabase::class.java, "events_database")
                .build()
        eventRoomDataDao = eventDatabase.roomUuidDataDao()
    }

    private fun initDB(): RoomDataDao {
        val database = AppDatabase.getInstance(applicationContext)
        return database.roomDataDao()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showAlertDialog(title: String, message: String, from: String) {

        val inflater = LayoutInflater.from(this@SettingsActivity)
        val dialogView: View?

        if (screenLengthDp < 790) {
            dialogView = inflater.inflate(R.layout.custom_alert_dialog_layout_small, null)
        } else {
            dialogView = inflater.inflate(R.layout.custom_alert_dialog_layout, null)
        }

        // Set the title text
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialogTitle)
        titleTextView.text = title

        val messageTextView = dialogView.findViewById<TextView>(R.id.dialogMessage)
        messageTextView.text = message

        val alertDialogBuilder = AlertDialog.Builder(this@SettingsActivity)
        alertDialogBuilder.setView(dialogView)
            .setCancelable(false) // Prevent dialog dismissal on outside touch or back button

        alertDialog = alertDialogBuilder.create()

        // Find UI elements and set their behavior
        val closeButton = dialogView.findViewById<Button>(R.id.okButton)
        closeButton.setOnClickListener {

            when (from) {
                getString(R.string.uuid) -> {
                    resetTextButtonView.visibility = View.GONE
                    deleteUuidView.visibility = View.GONE
                    resetUuiSharedPreference()
                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    alertDialog!!.dismiss() // Close the dialog
                    resetingUuidText.visibility = View.VISIBLE
                    btnReturn.isEnabled = false
                    btnReturn.isClickable = false
                    btnReturn.isFocusable = false
                    btnReturn.alpha = 0.5f

                    finish()
                }

                getString(R.string.db) -> {
                    GlobalScope.launch(Dispatchers.IO) {
                        val dao = initDB()
                        dao.deleteAllData()
                        alertDialog!!.dismiss() // Close the dialog
                        finish()
                    }
                }

                else -> {
                    Toast.makeText(this@SettingsActivity, "Error", Toast.LENGTH_SHORT).show()
                    alertDialog!!.dismiss() // Close the dialog
                    finish()
                }
            }
        }

        val cancerButton = dialogView.findViewById<Button>(R.id.cancelButton)
        cancerButton.setOnClickListener {
            alertDialog?.dismiss()
        }

        alertDialog?.show()

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun resetUuiSharedPreference() {
        GlobalScope.launch(Dispatchers.IO) {
            SharePreferenceSingleton.setTestingMode(this@SettingsActivity, false)
            SharePreferenceSingleton.setRegisterStatus(this@SettingsActivity, false)
            TestingParamsSettings.resetSettings(this@SettingsActivity)
        }
    }
}