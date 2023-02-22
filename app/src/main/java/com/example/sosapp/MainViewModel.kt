package com.example.sosapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sosapp.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    companion object {
        const val TAG = "MainViewModel"
    }

    private val _permissionState = MutableStateFlow<RequestState<Boolean>>(RequestState.Idle)
    val permissionState: StateFlow<RequestState<Boolean>> = _permissionState

    init {
        readPermissionState()
    }

    private fun readPermissionState() {
        _permissionState.value = RequestState.Loading
        try {
            viewModelScope.launch {
                dataStoreRepository.readPermissionState
                    .map { state -> state }
                    .collect { state ->
                        _permissionState.value = RequestState.Success(state)
                    }
            }
        } catch (e: Exception) {
            _permissionState.value = RequestState.Error(e)
        }
    }

    fun persistPermissionState(state: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.persistPermissionState(state)
        }
    }


//
//    private fun getNumberOfContacts(context: Context) : Cursor? {
//        val contentResolver: ContentResolver = context.contentResolver
//        return contentResolver.query(
//            ContactsContract.Contacts.CONTENT_URI,
//            null,
//            null,
//            null,
//            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
//        )
//    }
//
//    private fun getLocation(context: Context): DoubleArray {
//        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        val providers = lm.getProviders(true)
//
//        var l: Location? = null
//        for (i in providers.indices.reversed()) {
//            if (
//                ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED ||
//                ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                l = lm.getLastKnownLocation(providers[i])
//            }
//
//            if (l != null) break
//        }
//        val gps = DoubleArray(2)
//        if (l != null) {
//            gps[0] = l.latitude
//            gps[1] = l.longitude
//        }
//        return gps
//    }
}