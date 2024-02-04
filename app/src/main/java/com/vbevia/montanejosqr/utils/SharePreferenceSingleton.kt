package com.vbevia.montanejosqr.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.vbevia.montanejosqr.constants.Constants
import com.vbevia.montanejosqr.network.Endpoint

object SharePreferenceSingleton {
    init {
        println("Singleton class invoked.")
    }

    fun getUui(context: Context): String? {
        val prefs = context.getSharedPreferences(Constants.UUI, Context.MODE_PRIVATE)
        return prefs.getString(Constants.uUIPreference, "")
    }

    fun setUui(context: Context, uui: String) {
        val editor = context.getSharedPreferences(Constants.UUI, AppCompatActivity.MODE_PRIVATE).edit()
        editor.putString(Constants.uUIPreference, uui)
        editor.apply()
    }

    fun getApiURL(context: Context): String? {
        val prefs = context.getSharedPreferences(Constants.APIURL, Context.MODE_PRIVATE)
        return prefs.getString(Constants.apiUrlPreference, Endpoint.serverMainUrl)
    }

    fun setApiURL(context: Context, uui: String) {
        val editor = context.getSharedPreferences(Constants.APIURL, AppCompatActivity.MODE_PRIVATE).edit()
        editor.putString(Constants.apiUrlPreference, uui)
        editor.apply()
    }

    fun getEventName(context: Context): String? {
        val prefs = context.getSharedPreferences(Constants.EVENT, Context.MODE_PRIVATE)
        return prefs.getString(Constants.eventPreference, "")
    }

    fun setEventName(context: Context, event: String) {
        val editor = context.getSharedPreferences(Constants.EVENT, AppCompatActivity.MODE_PRIVATE).edit()
        editor.putString(Constants.eventPreference, event)
        editor.apply()
    }

    fun getEventTerminalName(context: Context): String? {
        val prefs = context.getSharedPreferences(Constants.TERMINAL_NAME, Context.MODE_PRIVATE)
        return prefs.getString(Constants.terminalNamePreference, "")
    }

    fun setEventTerminalName(context: Context, terminalName: String) {
        val editor = context.getSharedPreferences(Constants.TERMINAL_NAME, AppCompatActivity.MODE_PRIVATE).edit()
        editor.putString(Constants.terminalNamePreference, terminalName)
        editor.apply()
    }

    fun getEventUuid(context: Context): String? {
        val prefs = context.getSharedPreferences(Constants.EVENT_UUID, Context.MODE_PRIVATE)
        return prefs.getString(Constants.eventUuidPreference, "")
    }

    fun setEventUuid(context: Context, event: String) {
        val editor = context.getSharedPreferences(Constants.EVENT_UUID, AppCompatActivity.MODE_PRIVATE).edit()
        editor.putString(Constants.eventUuidPreference, event)
        editor.apply()
    }

    fun getTestingMode(context: Context): Boolean? {
        val prefs = context.getSharedPreferences(Constants.TESTING_MODE, Context.MODE_PRIVATE)
        return prefs.getBoolean(Constants.testingModePreference, false )
    }

    fun setTestingMode(context: Context, mode: Boolean) {
        val editor = context.getSharedPreferences(Constants.TESTING_MODE, AppCompatActivity.MODE_PRIVATE).edit()
        editor.putBoolean(Constants.testingModePreference, mode)
        editor.apply()
    }

    fun getRegisterStatus(context: Context): Boolean? {
        val prefs = context.getSharedPreferences(Constants.REGISTER_STATUS, Context.MODE_PRIVATE)
        return prefs.getBoolean(Constants.registerStatusPreference, false )
    }

    fun setRegisterStatus(context: Context, mode: Boolean) {
        val editor = context.getSharedPreferences(Constants.REGISTER_STATUS, AppCompatActivity.MODE_PRIVATE).edit()
        editor.putBoolean(Constants.registerStatusPreference, mode)
        editor.apply()
    }

}