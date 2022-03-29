package me.remi.espie.brosignal

import android.net.Uri
import com.google.gson.Gson

class UserKt {
    var contactID: String? = null
    var contactName: String? = null
    var contactThumbnails: String? = null
    var contactNumber: String? = null

    constructor() : super() {}

    constructor(contactID: String, contactName: String, contactThumbnails: String, contactNumber: String) : super() {
        this.contactID = contactID
        this.contactName = contactName
        this.contactThumbnails = contactThumbnails
        this.contactNumber = contactNumber
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}
