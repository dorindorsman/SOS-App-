package com.example.sosapp

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosapp.contacts.ContactModel
import com.example.sosapp.contacts.ItemView
import com.example.sosapp.shake_services.ReactivateService
import com.example.sosapp.shake_services.SensorService
import com.example.sosapp.ui.theme.SOSAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class MainActivity : ComponentActivity() {

    companion object {
        private val IGNORE_BATTERY_OPTIMIZATION_REQUEST = 1002
        private val PICK_CONTACT = 1
    }

    private val mainViewModel: MainViewModel by viewModels()

    // create instances of various classes to be used
    var db: DbHelper? = null
    var list: List<ContactModel>? = emptyList()
    var customAdapter: CustomAdapter? = null

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val smsManager: SmsManager = SmsManager.getDefault()
        db = DbHelper(this)

        // check for BatteryOptimization,
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            askIgnoreOptimization()
        }

        // start the service
        val sensorService = SensorService()
        val intent = Intent(this, sensorService.javaClass)
        if (!isMyServiceRunning(sensorService.javaClass)) {
            startService(intent)
        }

        Log.d("dorin", db!!.allContacts.toString())
        list = db?.let { it.allContacts }
        for (c in list!!) {
            val message = ("""Hey, ${c.name}I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.
 http://maps.google.com/?q=$""" + ",")
            smsManager.sendTextMessage(
                c.phone, null,
                message, null, null
            )
        }


        setContent {
            SOSAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {

                    val context = LocalContext.current

                    val multiplePermissionsState = rememberMultiplePermissionsState(
                        listOf(
                            SEND_SMS,
                            ACCESS_COARSE_LOCATION,
                            ACCESS_FINE_LOCATION,
                            READ_CONTACTS,
                        )
                    )

                    if (multiplePermissionsState.allPermissionsGranted) {
                        MainScreen(context)
                    } else {
                        AskForPermissions(
                            mainViewModel = mainViewModel,
                            multiplePermissionsState = multiplePermissionsState
                        )
                    }
                }
            }
        }
    }

    // method to check if the service is running
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }

    override fun onDestroy() {
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, ReactivateService::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permissions Denied!\n Can't use the App!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_CONTACT -> if (resultCode == RESULT_OK) {
                val contactData: Uri? = data?.let { it.data }
                val c: Cursor = managedQuery(contactData, null, null, null, null)
                if (c.moveToFirst()) {
                    val id: String = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val hasPhone: String = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    var phone: String? = null
                    try {
                        if (hasPhone.equals("1", ignoreCase = true)) {
                            val phones: Cursor? = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                null,
                                null
                            )
                            phones?.let {
                                phones.moveToFirst()
                                phone = phones.getString(phones.getColumnIndex("data1"))
                            }
                        }
                        val name: String = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        db!!.addContact(ContactModel(0, name, phone!!))
                        list = db!!.allContacts
                        customAdapter!!.refresh(list)
                    } catch (ex: Exception) {
                    }
                }
            }
        }
    }

    // this method prompts the user to remove any
    // battery optimisation constraints from the App
    private fun askIgnoreOptimization() {
        @SuppressLint("BatteryLife") val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST)
    }

    @Composable
    fun MainScreen(context: Context) {
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {

            ExtendedFloatingActionButton(
                modifier = Modifier
                    .wrapContentSize()
                    .height(100.dp)
                    .clip(CircleShape)
                    .padding(20.dp),
                onClick = {
                    db?.let { dbHelper ->
                        if (dbHelper.count() !== 5) {
                            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                            startActivityForResult(intent, PICK_CONTACT)
                        } else {
                            Toast.makeText(context, "Can't Add more than 5 Contacts", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.add),
                        modifier = Modifier.wrapContentSize(),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace, fontSize = 20.sp, color = Color.White
                        )
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                shape = MaterialTheme.shapes.small,
            )

            Spacer(modifier = Modifier.width(25.dp))


            list?.let { contacts ->
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.wrapContentWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(contacts) { contact ->
                            ItemView(name = contact.name, phone = contact.phone)
                        }
                    }
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        SOSAppTheme {
            MainScreen(LocalContext.current)
        }
    }

}

