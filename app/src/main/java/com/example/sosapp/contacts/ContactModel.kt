package com.example.sosapp.contacts

class ContactModel(val id: Int, name: String, phone: String) {
    val phone: String
    var name: String

    // constructor
    init {
        this.phone = validate(phone)
        this.name = name
    }

    // validate the phone number, and reformat is necessary
    private fun validate(phone: String): String {

        // creating StringBuilder for both the cases
        val case1 = StringBuilder("+972")
        val case2 = StringBuilder("")

        // check if the string already has a "+"
        return if (phone[0] != '+') {
            for (i in 0 until phone.length) {
                // remove any spaces or "-"
                if (phone[i] != '-' && phone[i] != ' ') {
                    case1.append(phone[i])
                }
            }
            case1.toString()
        } else {
            for (i in 0 until phone.length) {
                // remove any spaces or "-"
                if (phone[i] != '-' || phone[i] != ' ') {
                    case2.append(phone[i])
                }
            }
            case2.toString()
        }
    }
}