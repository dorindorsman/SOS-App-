package com.example.sosapp.shake_services

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.sosapp.DbHelper
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.*


class SensorService : Service() {
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mShakeDetector: ShakeDetector? = null
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(1, Notification())

        val context = this.applicationContext

        // ShakeDetector initialization
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mShakeDetector = ShakeDetector()
        mShakeDetector!!.setOnShakeListener(object : ShakeDetector.OnShakeListener {
            @SuppressLint("MissingPermission")
            override fun onShake(count: Int) {
                // check if the user has shacked
                // the phone for 3 time in a row
                if (count == 3) {

                    // vibrate the phone
                    vibrate()

//                    val location = getLocation(context = context)
//
//                    if (location != null) {
//                        onSuccess(location)
//                    }else{
//                        onFailure()
//                    }


                    // create FusedLocationProviderClient to get the user location
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext())

                    // use the PRIORITY_BALANCED_POWER_ACCURACY
                    // so that the service doesn't use unnecessary power via GPS
                    // it will only use GPS at this very moment
                    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

                        override fun isCancellationRequested(): Boolean {
                            return false
                        }
                    }).addOnSuccessListener { location ->
                        Log.d("dorin2", location.altitude.toString() +"" + location.longitude+toString())
                        onSuccess(location)
                    }
                        .addOnFailureListener {
                            onFailure()
                        }


                }
            }
        })

        // register the listener
        mSensorManager!!.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    private fun onFailure() {
        Log.d("Check: ", "OnFailure")
        val message = """
                            I am in DANGER, i need help. Please urgently reach me out.
                            GPS was turned off.Couldn't find location. Call your nearest Police Station.
                            """.trimIndent()
        val smsManager: SmsManager = SmsManager.getDefault()
        val db = DbHelper(this@SensorService)
        val list = db.allContacts
        for (c in list) {
            smsManager.sendTextMessage(c.phone, null, message, null, null)
        }
    }

    private fun onSuccess(location: Location?) {
        // check if location is null
        // for both the cases we will
        // create different messages
        if (location != null) {
            // get the SMSManager
            val smsManager: SmsManager = SmsManager.getDefault()

            // get the list of all the contacts in Database
            val db = DbHelper(this@SensorService)
            val list = db.allContacts

            Log.d("dorin", location.altitude.toString() +"" + location.longitude+toString())
            // send SMS to each contact
            for (c in list) {
                val message =
                    """SOS ${c.name}, I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates. http://maps.google.com/?q=${location.altitude},${location.longitude}"""
                smsManager.sendTextMessage(c.phone, null, message, null, null)
            }
        } else {
            val message = """I am in DANGER, i need help. Please urgently reach me out. GPS was turned off.Couldn't find location. Call your nearest Police Station. """.trimIndent()
            val smsManager: SmsManager = SmsManager.getDefault()
            val db = DbHelper(this@SensorService)
            val list = db.allContacts
            for (c in list) {
                smsManager.sendTextMessage(c.phone, null, message, null, null)
            }
        }
    }

    fun getLocation(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = lm.getProviders(true)

        var location: Location? = null
        for (i in providers.indices.reversed()) {
            if (
                ActivityCompat.checkSelfPermission(
                    context,
                    ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                location = lm.getLastKnownLocation(providers[i])
            }
            if (location != null) {
                break
            }
        }
        return location
    }

    // method to vibrate the phone
    fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        val vibEff: VibrationEffect

        // Android Q and above have some predefined vibrating patterns
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibEff = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            vibrator!!.cancel()
            vibrator.vibrate(vibEff)
        } else {
            vibrator!!.vibrate(500)
        }
    }

    // For Build versions higher than Android Oreo, we launch
    // a foreground service in a different way. This is due to the newly
    // implemented strict notification rules, which require us to identify
    // our own notification channel in order to view them correctly.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN)
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)!!
        manager.createNotificationChannel(chan)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("You are protected.")
            .setContentText("We are there for you") // this is important, otherwise the notification will show the way
            // you want i.e. it will show some default notification
            //.setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
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

