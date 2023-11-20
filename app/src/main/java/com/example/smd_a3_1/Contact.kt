package com.example.smd_a3_1

import android.net.Uri

data class Contact(
    var id: Long = 0,
    var name: String,
    var phoneNumber: String,
    var imageUri: Uri? = null
)

