package com.example.run_tracker_native_app.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.run_tracker_native_app.adapter.SubscriptionADP
import com.example.run_tracker_native_app.databinding.ActivitySubscriptionBinding
import com.example.run_tracker_native_app.dataclass.SubscriptionData
import com.example.run_tracker_native_app.utils.Constant
import com.example.run_tracker_native_app.utils.Util
import java.util.concurrent.Executors

class SubscriptionActivity : BaseActivity() {

    private lateinit var binding: ActivitySubscriptionBinding

    private var recyclerView: RecyclerView? = null
    private var subscriptionADP: SubscriptionADP? = null
    private val subscriptionDataList: ArrayList<SubscriptionData> = arrayListOf()

    private var billingClient: BillingClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cvClose.setOnClickListener {
            finish()
        }
        binding.progressCircular.visibility = View.VISIBLE
        binding.btnSubscribe.setOnClickListener {
            for(sub in subscriptionDataList){
                if(sub.isSelected) onPurchaseClick(sub.productDetails,sub.planIndex)
            }
        }
        billingClient = BillingClient.newBuilder(this@SubscriptionActivity)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()

        billingClient!!.startConnection(object : BillingClientStateListener {


            override fun onBillingServiceDisconnected() {
                Log.e("TAG", "onBillingServiceDisconnected::::: ")
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                Log.e("TAG", "onBillingSetupFinished:::: " + p0.debugMessage)
                checkSubscriptionList()
            }
        })
    }

    private val purchaseUpdateListener: PurchasesUpdatedListener =
        PurchasesUpdatedListener { result, _ ->
            try {
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                        Util.setPref(this, Constant.PREF_KEY_PURCHASE_STATUS, true)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
//                else {
//                    Util.setPref(this, Constant.PREF_KEY_PURCHASE_STATUS, true)
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
//                }
                checkSubscriptionList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun setData() {
        if (billingClient != null) {
            val executorService = Executors.newSingleThreadExecutor()
            executorService.execute {
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder().setProductId(Constant.SKU_ID)
                        .setProductType(BillingClient.ProductType.SUBS).build()
                )
                val params =
                    QueryProductDetailsParams.newBuilder().setProductList(productList)
                        .build()

                billingClient!!.queryProductDetailsAsync(params) { billingResult,
                                                                   productDetailsList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        subscriptionDataList.clear()
                        for (productDetails in productDetailsList) {
                            if (productDetails.subscriptionOfferDetails != null) {
                                for (i in 0 until productDetails.subscriptionOfferDetails!!.size) {
                                    var subsName: String = productDetails.name
                                    val index: Int = i
                                    var phases: String
                                    val formattedPrice: String =
                                        productDetails.subscriptionOfferDetails?.get(i)?.pricingPhases?.pricingPhaseList?.get(
                                            0
                                        )?.formattedPrice.toString()

                                    var billingPeriod: String =
                                        productDetails.subscriptionOfferDetails?.get(i)?.pricingPhases?.pricingPhaseList?.get(
                                            0
                                        )?.billingPeriod.toString()

                                    val recurrenceMode: String =
                                        productDetails.subscriptionOfferDetails?.get(i)?.pricingPhases?.pricingPhaseList?.get(
                                            0
                                        )?.recurrenceMode.toString()

                                    if (recurrenceMode == "2") {
                                        when (billingPeriod) {
                                            "P1M" -> billingPeriod = " For 1 Month"
                                            "P6M" -> billingPeriod = " For 6 Month"
                                            "P1Y" -> billingPeriod = " For 1 Year"
                                            "P1W" -> billingPeriod = " For 1 Week"
                                            "P3W" -> billingPeriod = " For 3 Week"
                                        }
                                    } else {
                                        when (billingPeriod) {
                                            "P1M" -> billingPeriod = "/Every 1 Month"
                                            "P6M" -> billingPeriod = "/Every 6 Month"
                                            "P1Y" -> billingPeriod = "/Every 1 Year"
                                            "P1W" -> billingPeriod = "/Week"
                                            "P3W" -> billingPeriod = "/Every 3 Week"
                                        }
                                    }
                                    phases = "$formattedPrice$billingPeriod"
                                    for (j in 0 until (productDetails.subscriptionOfferDetails!![i]?.pricingPhases?.pricingPhaseList?.size!!)) {
                                        if (j > 0) {
                                            val price: String =
                                                productDetails.subscriptionOfferDetails?.get(
                                                    i
                                                )?.pricingPhases?.pricingPhaseList?.get(
                                                    j
                                                )?.formattedPrice.toString()

                                            var period: String =
                                                productDetails.subscriptionOfferDetails?.get(
                                                    i
                                                )?.pricingPhases?.pricingPhaseList?.get(
                                                    j
                                                )?.billingPeriod.toString()

                                            when (period) {
                                                "P1M" -> period = "/Every 1 Month"
                                                "P6M" -> period = "/Every 6 Month"
                                                "P1Y" -> period = "/Every 1 Year"
                                                "P1W" -> period = "/Week"
                                                "P3W" -> period = "/Every 3 Week"
                                            }
                                            subsName += "\n" + productDetails.subscriptionOfferDetails?.get(
                                                i
                                            )?.offerId.toString()
                                            phases += "\n$price$period"

                                        }
                                    }
                                    val tmpItem =
                                        SubscriptionData(productDetails,subsName, phases, index, false)
                                    subscriptionDataList.add(tmpItem)
                                }
                            }
                        }
                    }
                }
                runOnUiThread {
                    Thread.sleep(500)
                    binding.progressCircular.visibility = View.GONE
                    binding.rvSubscription.stopNestedScroll()
                    recyclerView = binding.rvSubscription
                    recyclerView!!.layoutManager =
                        LinearLayoutManager(this@SubscriptionActivity)
                    subscriptionADP = SubscriptionADP(this@SubscriptionActivity)
                    recyclerView!!.adapter = subscriptionADP
                    subscriptionADP!!.addAllData(subscriptionDataList)
                    subscriptionADP!!.setOnClickListener(
                        object : SubscriptionADP.OnItemClickListener {
                            override fun onItemClick(index: Int) {

                                subscriptionADP!!.onChangeItemSelection(index)
                            }

                        }
                    )
                }
            }

        }


//        subscriptionDataList.addAll(
//            listOf(
//                SubscriptionData("999.99", "1", true, "sub_1_month"),
//                SubscriptionData("4999.99", "6", false, "sub_16_month"),
//                SubscriptionData("9999.99", "12", false, "sub_12_month"),
//            )
//        )


    }

    private fun checkSubscriptionList() {
        if (billingClient != null) {
            var isPurchasedSku = false
            try {
                billingClient!!.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                ) { purchasesResult, purchaseList ->
                    if (purchasesResult.responseCode == 0) {
                        Log.e("", "purchaseDataList::$purchaseList")
                        if (purchaseList.isNotEmpty()) {
                            for (i in 0 until purchaseList.size) {
                                val purchaseData = purchaseList[i]
                                if (purchaseData.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                    isPurchasedSku = true
                                    if (!purchaseData.isAcknowledged) {
                                        val acknowledgePurchaseParams =
                                            AcknowledgePurchaseParams.newBuilder()
                                                .setPurchaseToken(purchaseData.purchaseToken)
                                        billingClient!!.acknowledgePurchase(
                                            acknowledgePurchaseParams.build()
                                        ) { p0 ->
                                            if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                                                    Util.setPref(this, Constant.PREF_KEY_PURCHASE_STATUS, isPurchasedSku)
                                                    startActivity(Intent(this, MainActivity::class.java))
                                                    finish()
                                            }
                                            Log.e("BillingResult ======>", p0.debugMessage)
                                        }
                                    }else{
                                        Util.setPref(this, Constant.PREF_KEY_PURCHASE_STATUS, isPurchasedSku)
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                                }
                            }
                        }
                        Util.setPref(this, Constant.PREF_KEY_PURCHASE_STATUS, isPurchasedSku)
                        setData()
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        }
    }


    private fun onPurchaseClick(productDetails: ProductDetails, planIndex: Int) {
        runOnUiThread {
            Log.e("subsName : ", productDetails.title)
            val productDetailsParamsList = listOf(
                productDetails.subscriptionOfferDetails?.get(planIndex)?.let {
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(it.offerToken)
                        .build()
                }
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

// Launch the billing flow
            billingClient!!.launchBillingFlow(this@SubscriptionActivity, billingFlowParams)

        }
//        val productList = ArrayList<QueryProductDetailsParams.Product>()
//        productList.add(
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(sku)
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build()
//        )
//        val params = QueryProductDetailsParams.newBuilder()
//        params.setProductList(productList)
////        val productDetailsResult = withContext(Dispatchers.IO) {
////            billingClient!!.queryProductDetails(params.build())
////        }
//        val paramsNewMonth = QueryProductDetailsParams.newBuilder().setProductList(productList)
//        billingClient!!.queryProductDetailsAsync(paramsNewMonth.build()) { _, productDetailsList ->
//            if(productDetailsList.isNotEmpty()){
//
//            }
//        }
    }
}