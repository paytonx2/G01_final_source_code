package com.example.smartapartment.utils

object Constants {
    // For Android Emulator to access localhost (XAMPP)
    const val BASE_URL = "http://10.0.2.2:3000/"
    
    // To construct full image URLs 
    // Example: Constants.IMAGE_URL + invoice.slip_image
    const val IMAGE_URL = "${BASE_URL}uploads/"
}
