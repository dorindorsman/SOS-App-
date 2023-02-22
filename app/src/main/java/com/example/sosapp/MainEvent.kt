package com.example.sosapp

import com.example.sosapp.contacts.ContactModel

sealed class MainEvent {
    class SetShowAlertDialogState( val state: Boolean) : MainEvent()
    class SetCurrentContact(val contact: ContactModel) : MainEvent() {

    }

}
