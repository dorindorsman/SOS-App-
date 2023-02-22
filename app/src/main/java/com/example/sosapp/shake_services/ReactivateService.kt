package com.example.sosapp.shake_services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class ReactivateService : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Check: ", "Receiver Started");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("ReactivateService: ", " if Receiver Started");
            context?.let { it.startForegroundService(Intent(context, SensorService::class.java)) }
        } else {
            Log.d("ReactivateService: ", "else Receiver Started");
            context?.let { it.startService(Intent(context, SensorService::class.java)) }
        }
    }
}