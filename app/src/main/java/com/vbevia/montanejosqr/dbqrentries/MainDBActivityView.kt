package com.vbevia.montanejosqr.dbqrentries

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bevia.mfypro.utils.ScreenUtils
import com.vbevia.montanejosqr.R
import com.vbevia.montanejosqr.utils.DividerItemDecoration

class MainDBActivityView : AppCompatActivity() {

    //Database stuff:
    private lateinit var database: AppDatabase
    private lateinit var roomDataDao: RoomDataDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var noData: LinearLayoutCompat
    private lateinit var btnReturn: Button
    private lateinit var dbRotateImage: ImageView
    private var oldNewFirst: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ScreenUtils.initialize(this)
        var screenLengthDp = ScreenUtils.getScreenLengthDp()

        if (screenLengthDp < 790) {
            setContentView(R.layout.activity_main_dbactivity_small)
        } else {
            setContentView(R.layout.activity_main_dbactivity)
        }

        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database").build()
        roomDataDao = database.roomDataDao()

        recyclerView = findViewById(R.id.notesRecycler)
        noData = findViewById(R.id.noData)

        val dividerDrawableResId = R.drawable.divider
        val itemDecoration = DividerItemDecoration(this, dividerDrawableResId)
        recyclerView.addItemDecoration(itemDecoration)

        adapter = MyAdapter(emptyList(), this) // Initialize with an empty list

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnReturn = findViewById(R.id.btnReturn)
        btnReturn.setOnClickListener {
            finish()
        }

        dbNewFirst()

        dbRotateImage = findViewById(R.id.dbRotateImage)
        dbRotateImage.setOnClickListener {

            if(oldNewFirst) {
                oldNewFirst = false
                dbOldFirst()
            }else{
                oldNewFirst = true
                dbNewFirst()
            }
        }
    }

    private fun dbNewFirst() {
        roomDataDao.getAllRoomDataDescending().observe(this) { roomDataList ->

            if (roomDataList.isEmpty()) {

                noData.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE

            } else {

                noData.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                adapter.dataList = roomDataList
                adapter.notifyDataSetChanged()

                for (roomData in roomDataList) {

                    Log.d("MainDBActivityView", "roomData: $roomData")
                }
            }
        }
    }

    private fun dbOldFirst() {
        roomDataDao.getAllRoomDataAscending().observe(this) { roomDataList ->

            if (roomDataList.isEmpty()) {

                noData.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE

            } else {

                noData.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                adapter.dataList = roomDataList
                adapter.notifyDataSetChanged()

                for (roomData in roomDataList) {

                    Log.d("MainDBActivityView", "roomData: $roomData")
                }
            }
        }
    }
}