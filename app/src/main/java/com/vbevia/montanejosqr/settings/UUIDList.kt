package com.vbevia.montanejosqr.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bevia.mfypro.utils.ScreenUtils
import com.vbevia.montanejosqr.R
import com.vbevia.montanejosqr.dbuuidevents.EventsDatabase
import com.vbevia.montanejosqr.dbuuidevents.RoomEventsUuidDataDao
import com.vbevia.montanejosqr.registerevent.adapters.RegisterEventAdapter
import com.vbevia.montanejosqr.registerevent.RegisterEventActivityView
import com.vbevia.montanejosqr.utils.DividerItemDecoration
import com.vbevia.montanejosqr.utils.SharePreferenceSingleton
import com.vbevia.montanejosqr.utils.TestingParamsSettings
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UUIDList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RegisterEventAdapter
    private lateinit var exitButton: Button
    private lateinit var deletingTextview: TextView
    private var alertDialog: AlertDialog? = null
    private var screenLengthDp = ScreenUtils.getScreenLengthDp()

    //Events Database stuff:
    private lateinit var eventDatabase: EventsDatabase
    private lateinit var eventRoomDataDao: RoomEventsUuidDataDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ScreenUtils.initialize(this)
        var screenLengthDp = ScreenUtils.getScreenLengthDp()

        if (screenLengthDp < 790) {
            setContentView(R.layout.activity_uuidlist_small)
        } else {
            setContentView(R.layout.activity_uuidlist)
        }

        initializeDataBase()
        initializeAdapter()
        checkEventsDataBase()
        deletingTextview = findViewById(R.id.deleting_textview)
        deletingTextview.visibility = View.GONE
        exitButton = findViewById(R.id.btnExit)
        exitButton.setOnClickListener {
            finish()
        }
    }

    private fun initializeAdapter() {

        val dividerDrawableResId = R.drawable.divider
        val itemDecoration = DividerItemDecoration(this, dividerDrawableResId)

        recyclerView = findViewById(R.id.eventsRecycler)
        recyclerView.addItemDecoration(itemDecoration)

        adapter = RegisterEventAdapter(emptyList()) { selectedItem, _ ->

            showAlertDialog(
                "Borrar uuid",
                "¿seguro que quieres borrar el evento:\n${selectedItem.event} \nperteneciente al uuid:\n" +
                        "${selectedItem.uuid}",
                selectedItem.uuid
            )

        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun deleteEvent(uUid: String) {
        GlobalScope.launch(Dispatchers.IO) {
            eventRoomDataDao.deleteItemById(uUid)
        }
    }

    private fun initializeDataBase() {
        eventDatabase =
            Room.databaseBuilder(applicationContext, EventsDatabase::class.java, "events_database")
                .build()
        eventRoomDataDao = eventDatabase.roomUuidDataDao()
    }

    private fun checkEventsDataBase() {
        eventRoomDataDao.getAllRoomDataDescending().observe(this) { roomDataList ->
            adapter.dataList = roomDataList
            recyclerView.visibility = View.VISIBLE
            deletingTextview.visibility = View.GONE
            adapter.notifyDataSetChanged()

            if (roomDataList.isEmpty()) {
                recyclerView.visibility = View.GONE
                deletingTextview.visibility = View.VISIBLE
                resetUuiSharedPreference()
                startActivity(Intent(this@UUIDList, RegisterEventActivityView::class.java))
                finish()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun resetUuiSharedPreference() {
        GlobalScope.launch(Dispatchers.IO) {
            SharePreferenceSingleton.setTestingMode(this@UUIDList, false)
            SharePreferenceSingleton.setRegisterStatus(this@UUIDList, false)
            TestingParamsSettings.resetSettings(this@UUIDList)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showAlertDialog(title: String, message: String, uuid: String) {

        val inflater = LayoutInflater.from(this@UUIDList)
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

        val alertDialogBuilder = AlertDialog.Builder(this@UUIDList)
        alertDialogBuilder.setView(dialogView)
            .setCancelable(false) // Prevent dialog dismissal on outside touch or back button

        alertDialog = alertDialogBuilder.create()

        // Find UI elements and set their behavior
        val closeButton = dialogView.findViewById<Button>(R.id.okButton)
        closeButton.setOnClickListener {

            deleteEvent(uuid)
            checkEventsDataBase()
            alertDialog?.dismiss()
        }


        val cancerButton = dialogView.findViewById<Button>(R.id.cancelButton)
        cancerButton.setOnClickListener {
            alertDialog?.dismiss()
        }

        alertDialog?.show()
    }
}