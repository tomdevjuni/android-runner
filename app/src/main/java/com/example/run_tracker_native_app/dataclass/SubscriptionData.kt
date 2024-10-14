package com.example.run_tracker_native_app.dataclass

import com.android.billingclient.api.ProductDetails

data class SubscriptionData(
    val productDetails: ProductDetails,
    val subsName: String,
    val formattedPrice: String,
    val planIndex: Int,
    var isSelected: Boolean,
)

