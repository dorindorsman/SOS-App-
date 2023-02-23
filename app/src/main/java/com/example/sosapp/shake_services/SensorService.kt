package com.example.sosapp.shake_services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.os.*
import android.telephony.SmsManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.sosapp.DbHelper
import com.example.sosapp.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.tasks.*


class SensorService : Service() {

    companion object {
        const val START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE"
        const val STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE"
        var MAIN_ACTION = "com.example.sosapp.shake_services.sensorservice.action.main"
    }
    private var db: DbHelper = DbHelper(this)
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mShakeDetector: ShakeDetector? = null

    private val smsManager: SmsManager = SmsManager.getDefault()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var location: Location? = null
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult.lastLocation != null) {
                Log.d("dorin", location.toString())
                location = locationResult.lastLocation
                onSuccess(location)
            } else {
                onFailure()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        // start the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(101, Notification())

        // ShakeDetector initialization
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        mAccelerometer = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mShakeDetector = ShakeDetector()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        mShakeDetector!!.setOnShakeListener(object : ShakeDetector.OnShakeListener {
            @SuppressLint("MissingPermission")
            override fun onShake(count: Int) {
                // check if the user has shacked
                // the phone for 3 time in a row
                if (count == 3) {
                    // vibrate the phone
                    vibrate()
                    startLocationUpdates()
                    Log.d("dorin 108", location.toString())
                }
            }
        })

        // register the listener
        mSensorManager?.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI)
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        Log.d("dorin", "line 143")
        locationCallback?.let {
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )

        }
    }

    private fun onFailure() {
        Log.d("Check: ", "OnFailure")
        val message = """
                            I am in DANGER, i need help. Please urgently reach me out.
                            GPS was turned off.Couldn't find location. Call your nearest Police Station.
                            """.trimIndent()
        val db = DbHelper(this@SensorService)
        val list = db.allContacts
        for (c in list) {
            smsManager.sendTextMessage(c.phone, null, message, null, null)
        }
    }

    private fun onSuccess(location: Location?) {
        if (location != null) {
            // get the list of all the contacts in Database
            Log.d("dorin success", location.altitude.toString() + "" + location.longitude + toString())
            // send SMS to each contact
            for (c in db.allContacts) {
                val message =
                    """SOS ${c.name}, I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates. http://maps.google.com/?q=${location.latitude},${location.longitude}"""
                smsManager.sendTextMessage(c.phone, null, message, null, null)
            }
        } else {
            val message =
                """I am in DANGER, i need help. Please urgently reach me out. GPS was turned off.Couldn't find location. Call your nearest Police Station. """.trimIndent()
            val list = db.allContacts
            for (c in list) {
                smsManager.sendTextMessage(c.phone, null, message, null, null)
            }
        }

        fusedLocationClient.removeLocationUpdates(locationCallback )
    }

    @SuppressLint("ServiceCast")
    fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Android Q and above have some predefined vibrating patterns
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.cancel()
        }
        vibrator.vibrate(500)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "com.example.sosapp.shake_services.CHANNEL_ID_FOREGROUND"
        val channelName = "Background Service"
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)


        // On notification click
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = MAIN_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            101,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentIntent(pendingIntent)
            .setContentTitle("You are protected.")
            .setContentText("We are there for you") // this is important, otherwise the notification will show the way
            // you want i.e. it will show some default notification
            //.setSmallIcon(R.drawable.)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }

    override fun onDestroy() {
        // create an Intent to call the Broadcast receiver
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, ReactivateService::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }
}

