package com.example.run_tracker_native_app.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.activity.AchievementsActivity
import com.example.run_tracker_native_app.activity.MainActivity
import com.example.run_tracker_native_app.activity.ReminderActivity
import com.example.run_tracker_native_app.activity.SubscriptionActivity
import com.example.run_tracker_native_app.adapter.LanguagesADP
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import com.example.run_tracker_native_app.databinding.FragmentSettingBinding
import com.example.run_tracker_native_app.dataclass.AchievementData
import com.example.run_tracker_native_app.utils.Constant
import com.example.run_tracker_native_app.utils.Util
import com.example.run_tracker_native_app.viewmodels.AchievementViewModel
import com.example.run_tracker_native_app.viewmodels.SettingsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.intuit.sdp.R.dimen as sdp


class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    private var distanceUnit: String = ""
    private var dailyGoal: Int = 1
    private var language: String = ""
    private val settingsViewModel by lazy {
        ViewModelProvider(this)[SettingsViewModel::class.java]
    }
    private var mSignInClient: GoogleSignInClient? = null

    var name: String? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    var firebaseUser: FirebaseUser? = null
    val fireStore = FirebaseFirestore.getInstance()
    var myPrefLocal: MyPref? = null
    private var arrayListDayNumber: ArrayList<Int>? = null
    private val achievementDataList: ArrayList<AchievementData> = arrayListOf()
    private val achievementViewModel by lazy {
        ViewModelProvider(this)[AchievementViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
//        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    private var someActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data!!
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
            Log.e("TAG", "result===>>> : $result")
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            requireActivity().runOnUiThread {
                showProgress()
            }
            firebaseAuthWithGoogle(credential)
            Log.e("TAG", "handleSignInResult:::Data==>>  " + Gson().toJson(account))
        } catch (e: ApiException) {
            Log.e("TAG", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(credential: AuthCredential) {
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val map = HashMap<String, String>()
                    map[Constant.USER_NAME] = firebaseUser!!.displayName.toString()
                    map[Constant.USER_EMAIL] = firebaseUser!!.email.toString()
                    map[Constant.USER_UID] = firebaseUser!!.uid.toString()
                    val users = fireStore.collection(Constant.TABLE_USERS)
                    users.document(firebaseUser!!.uid)
                        .set(map)
                        .addOnSuccessListener {
                            Util.setPref(requireContext(), Constant.IS_LOGIN, true)
                            onSync()
                        }.addOnFailureListener {
                            Util.setPref(requireContext(), Constant.IS_LOGIN, false)
                            cancelProgress()
                        }
                } else {
                    task.exception!!.printStackTrace()
                    Util.showToast(requireContext(), task.exception!!.message.toString())
                    cancelProgress()
                }
            }
    }

    private fun onSync(isShowDialog: Boolean = false) {
        if (Util.isNetworkAvailable(requireContext())) {
            if (Util.isLogin(requireContext())) {
                if (isShowDialog) {
                    requireActivity().runOnUiThread {
                        showProgress()
                    }
                    getPreferenceLocalAndSetServer()
                    getMyRunningTableDataAndSetServer()
                } else {
                    getPreferenceServerAndSetLocal()
                    getHistoryServerAndSetLocal()
                }
//                    getMyRunningTableDataAndSetServer()
            } else {
                Util.showToast(requireContext(), getString(R.string.please_login_first))
            }
        } else {
            Util.showToast(requireContext(), getString(R.string.no_internet))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llAchievement.setOnClickListener {
            val intent = Intent(requireContext(), AchievementsActivity::class.java)
            startActivity(intent)
        }

        binding.llSubscription.setOnClickListener {
            val intent = Intent(requireContext(), SubscriptionActivity::class.java)
            startActivity(intent)
        }

        binding.llReminder.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager =
                    ContextCompat.getSystemService(requireContext(), AlarmManager::class.java)
                if (manager?.canScheduleExactAlarms() == true) {
                    val intent = Intent(requireContext(), ReminderActivity::class.java)
                    startActivity(intent)
                } else {
                    Util.requestRequiredAlarmPermission(requireContext())
                }
            }

        }

        binding.llDailyGoal.setOnClickListener {
            openBottomSheet(getString(R.string.set_daily_goal))
        }

        binding.llUnit.setOnClickListener {
            openBottomSheet(resources.getString(R.string.metric_imperial_unit))
        }

        binding.llLanguage.setOnClickListener {
            openBottomSheet(getString(R.string.language))
        }
        getMyPrefFromDatabase()

        getFirebaseUser()
        firebaseAuth!!.addAuthStateListener(authStateListener!!)
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mSignInClient = GoogleSignIn.getClient(requireContext(), options)
        binding.llBackupRestore.visibility =
            if (Util.isPurchased(requireContext())) View.VISIBLE else View.GONE

        binding.llSubscription.visibility =
            if (Util.isPurchased(requireContext())) View.GONE else View.VISIBLE

//        binding.viSub.visibility =
//            if (Util.isPurchased(requireContext())) View.GONE else View.VISIBLE

        binding.llBackupAndRestore.setOnClickListener {
            if (!Util.isLogin(requireContext())) {
                onLoginProcess()
            }
        }
        binding.imgSyncData.setOnClickListener {
            if (!Util.isLogin(requireContext())) {
                onLoginProcess()
            } else {
                onSync(isShowDialog = true)
            }
        }

        binding.llRateUs.setOnClickListener {
            val appPackageName = requireActivity().packageName
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                            "market://details?id=$appPackageName"
                        )
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                            "https://play.google.com/store/apps/details?id=$appPackageName"
                        )
                    )
                )
            }
        }
        binding.llContactUs.setOnClickListener {
            contactUs()
        }
        binding.llPrivacyPolicy.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(Constant.PRIVACY_POLICY)
                )
            )
        }
        setListData()
        achievementViewModel.getMyTotalDistanceAllTime()
        achievementViewModel.myTotalDistanceAllTime.observe(viewLifecycleOwner) { totalDistance ->
//            if (totalDistance == null) return@observe
            val distance =
                if (totalDistance == null) 0.0 else if (distanceUnit == "km") totalDistance / 1000.0 else totalDistance / 1609.34
            achievementDataList.forEach { it.isCompleted = distance > it.distance }
            val achievementSize =
                achievementDataList.filter { achievementData -> achievementData.isCompleted }.size
            binding.txtAchievementKm.text =
                if (achievementSize < 1) "0" else achievementSize.toString()

        }
//        setLocalData()  shreyuinfotech2019@gmail.com
    }

    private fun contactUs() {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Constant.CONTACT_US))
            intent.putExtra(
                Intent.EXTRA_SUBJECT, resources.getString(
                    R.string.app_name
                )
            )
            intent.setType("message/rfc822")
            intent.setPackage("com.google.android.gm")
            startActivity(intent)
        } catch (e: Exception) {
            val sendIntentIfGmailFail = Intent(Intent.ACTION_SEND)
            sendIntentIfGmailFail.type = "*/*"
            sendIntentIfGmailFail.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf(Constant.CONTACT_US)
            )
            sendIntentIfGmailFail.putExtra(
                Intent.EXTRA_SUBJECT,
                resources.getString(R.string.app_name)
            )
            if (sendIntentIfGmailFail.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(sendIntentIfGmailFail)
            }
        }
    }

    private fun onLoginProcess() {
        if (Util.isNetworkAvailable(requireContext())) {
            if (firebaseUser == null) {
                val signInIntent: Intent = mSignInClient!!.signInIntent
                someActivityResultLauncher.launch(signInIntent)
            } else {
                openSignOtDialog()
            }
        } else {
            Util.showToast(requireContext(), getString(R.string.no_internet))
        }
    }

    private fun openSignOtDialog() {
        val builder1: AlertDialog.Builder = AlertDialog.Builder(context)
        builder1.setMessage(getString(R.string.are_you_sure_sign_out))
        builder1.setCancelable(true)

        builder1.setPositiveButton(
            getString(R.string._yes)
        ) { dialog, _ ->
            FirebaseAuth.getInstance().signOut()
            mSignInClient!!.signOut()
            getFirebaseUser()
            Util.setPref(requireContext(), Constant.IS_LOGIN, false)
            dialog.dismiss()
        }

        builder1.setNegativeButton(
            getString(R.string._no)
        ) { dialog, _ -> dialog.cancel() }

        val alert11: AlertDialog = builder1.create()
        alert11.show()
    }

    private fun getFirebaseUser() {
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {

                Log.e("TAG", "onAuthStateChanged:signed_in:" + firebaseUser!!.uid)
            } else {
                Log.e("TAG", "onAuthStateChanged:::signed_out")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getMyPrefFromDatabase() {
        settingsViewModel.myPrefLiveData.observe(viewLifecycleOwner) { myPref ->
            myPrefLocal = myPref
            distanceUnit = myPref.distanceUnit
            binding.txtDailyGoalKmMile.text = myPref.dailyGoal.toString()
            dailyGoal = myPref.dailyGoal
            binding.txtUnitType.text = myPref.distanceUnit
            binding.txtKmMile.text = myPref.distanceUnit
            language = myPref.language
            binding.tvLanguage.text = language
            if (myPref.reminderTimeHour != null && myPref.reminderTimeMinute != null && myPref.reminderDays.isNotEmpty()) {
                var prefHour = myPref.reminderTimeHour.toString()
                var prefMinute = myPref.reminderTimeMinute.toString()
                val strAmPm = if (myPref.reminderTimeHour < 12) "AM" else "PM"
                if (myPref.reminderTimeHour < 10) {
                    prefHour = "0$prefHour"
                }
                if (myPref.reminderTimeMinute < 10) {
                    prefMinute = "0$prefMinute"
                }
                binding.tvReminderTime.text = "$prefHour:$prefMinute $strAmPm"
                arrayListDayNumber = ArrayList()
                arrayListDayNumber!!.addAll(myPref.reminderDays)
                var txtDaysName = ""
                for (i in arrayListDayNumber!!.indices) {
                    if (txtDaysName.isEmpty()) {
                        txtDaysName = Util.getShortDayName(arrayListDayNumber!![i])
                        binding.txtSelectedDays.text = txtDaysName
                    } else {
                        txtDaysName = (", ").plus(Util.getShortDayName(arrayListDayNumber!![i]))
                        binding.txtSelectedDays.append(txtDaysName)
                    }
                }
            } else {
                binding.tvReminderTime.text = ""
                binding.txtSelectedDays.text = getString(R.string.select_days)
            }
        }
    }

    private fun openBottomSheet(strType: String) {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet)
        val txtTitle = bottomSheetDialog.findViewById<TextView>(R.id.txtTitle)
        val llBottomKmMile = bottomSheetDialog.findViewById<LinearLayout>(R.id.llBottomKmMile)
        val llBottomMetric = bottomSheetDialog.findViewById<LinearLayout>(R.id.llBottomMetric)
        val llBottomLanguage = bottomSheetDialog.findViewById<LinearLayout>(R.id.llBottomLanguage)

        txtTitle!!.text = strType

        when (strType) {
            getString(R.string.set_daily_goal) -> {
                llBottomKmMile!!.visibility = View.VISIBLE
                val pickerNumberKmMile =
                    bottomSheetDialog.findViewById<NumberPicker>(R.id.pickerNumberKmMile)
                val txtKmType = bottomSheetDialog.findViewById<TextView>(R.id.txtKmType)
                val txtMileType = bottomSheetDialog.findViewById<TextView>(R.id.txtMileType)
                val txtGoalSave = bottomSheetDialog.findViewById<TextView>(R.id.txtGoalSave)
                val txtGoalType = bottomSheetDialog.findViewById<TextView>(R.id.txtGoalType)
                var heightType = distanceUnit
                var selectedGoalKm = dailyGoal
                txtGoalType!!.text = heightType
                if (heightType == getString(R.string.km)) {
                    txtKmType!!.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab_view)
                    txtMileType!!.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.selected_tab_view_trans
                    )

                    txtMileType.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.txtGray
                        )
                    )
                    txtKmType.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                    heightType = getString(R.string.km)
                    pickerNumberKmMile!!.minValue = 1
                    pickerNumberKmMile.maxValue = 50

                } else {
                    txtMileType!!.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab_view)
                    txtKmType!!.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.selected_tab_view_trans
                    )

                    txtMileType.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    txtKmType.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.txtGray
                        )
                    )


                    heightType = getString(R.string._miles)
                    pickerNumberKmMile!!.minValue = 1
                    pickerNumberKmMile.maxValue = 50

                }


                txtKmType.setOnClickListener {
                    txtKmType.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab_view)
                    txtMileType.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.selected_tab_view_trans
                    )

                    txtKmType.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    txtMileType.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.txtGray
                        )
                    )

                    heightType = getString(R.string.km)

                    pickerNumberKmMile.minValue = 1
                    pickerNumberKmMile.maxValue = 50
                }

                txtMileType.setOnClickListener {
                    txtMileType.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab_view)
                    txtKmType.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.selected_tab_view_trans
                    )

                    txtKmType.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.txtGray
                        )
                    )
                    txtMileType.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )

                    heightType = getString(R.string._miles)
                    pickerNumberKmMile.minValue = 1
                    pickerNumberKmMile.maxValue = 50

                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    pickerNumberKmMile.textColor =
                        ContextCompat.getColor(requireContext(), R.color.darkTheme)
                    pickerNumberKmMile.textSize = resources.getDimension(sdp._30sdp)
                }

                pickerNumberKmMile.value = selectedGoalKm
                pickerNumberKmMile.setOnValueChangedListener { _, _, i2 ->
                    selectedGoalKm = i2
                }

                txtGoalSave!!.setOnClickListener {
                    settingsViewModel.updateDistanceUnitMyPref(heightType)
                    settingsViewModel.updateDailyGoalUnitMyPref(selectedGoalKm)
                    bottomSheetDialog.dismiss()
                }
            }

            getString(R.string.metric_imperial_unit) -> {
                llBottomMetric!!.visibility = View.VISIBLE
                val llUnitKm = bottomSheetDialog.findViewById<LinearLayout>(R.id.llUnitKm)
                val llUnitMile = bottomSheetDialog.findViewById<LinearLayout>(R.id.llUnitMile)
                val imgDoneKm = bottomSheetDialog.findViewById<ImageView>(R.id.imgDoneKm)
                val imgDoneMile = bottomSheetDialog.findViewById<ImageView>(R.id.imgDoneMile)
                val txtGoalMile = bottomSheetDialog.findViewById<TextView>(R.id.txtGoalMile)
                val txtGoalKm = bottomSheetDialog.findViewById<TextView>(R.id.txtGoalKm)

                var isSelectKm = distanceUnit == "km"
                if (isSelectKm) {
                    llUnitKm!!.setBackgroundResource(R.drawable.selected_card_kg_lb)
                    llUnitMile!!.setBackgroundResource(R.drawable.unselected_card_kg_lb)
                    imgDoneKm!!.visibility = View.VISIBLE
                    imgDoneMile!!.visibility = View.GONE
                    txtGoalMile!!.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.txtGrayDark
                        )
                    )
                    txtGoalKm!!.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.theme
                        )
                    )
                } else {
                    llUnitMile!!.setBackgroundResource(R.drawable.selected_card_kg_lb)
                    llUnitKm!!.setBackgroundResource(R.drawable.unselected_card_kg_lb)
                    imgDoneMile!!.visibility = View.VISIBLE
                    imgDoneKm!!.visibility = View.GONE
                    txtGoalMile!!.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.theme
                        )
                    )
                    txtGoalKm!!.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.txtGrayDark
                        )
                    )
                }
                llUnitKm.setOnClickListener {
                    if (!isSelectKm) {
                        isSelectKm = true
                        llUnitKm.setBackgroundResource(R.drawable.selected_card_kg_lb)
                        llUnitMile.setBackgroundResource(R.drawable.unselected_card_kg_lb)
                        imgDoneKm.visibility = View.VISIBLE
                        imgDoneMile.visibility = View.GONE
                        txtGoalMile.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.txtGrayDark
                            )
                        )
                        txtGoalKm.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.theme
                            )
                        )
                        settingsViewModel.updateDistanceUnitMyPref(getString(R.string.km))
                        bottomSheetDialog.dismiss()
                    }
                }

                llUnitMile.setOnClickListener {
                    if (isSelectKm) {
                        isSelectKm = false
                        llUnitMile.setBackgroundResource(R.drawable.selected_card_kg_lb)
                        llUnitKm.setBackgroundResource(R.drawable.unselected_card_kg_lb)
                        imgDoneMile.visibility = View.VISIBLE
                        imgDoneKm.visibility = View.GONE
                        txtGoalMile.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.theme
                            )
                        )
                        txtGoalKm.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.txtGrayDark
                            )
                        )
                        settingsViewModel.updateDistanceUnitMyPref(getString(R.string.mile))
                        bottomSheetDialog.dismiss()
                    }
                }

            }

            getString(R.string.language) -> {
                llBottomLanguage!!.visibility = View.VISIBLE
                val edtFindLang = bottomSheetDialog.findViewById<EditText>(R.id.edtFindLang)
                val adp = LanguagesADP(requireContext(), object : LanguagesADP.OnSelectedLanguage {
                    override fun selectedLanguage(pos: Int, strLanguage: String) {
                        Log.e("TAG", "selectedLanguage:::==>>> $pos  $strLanguage")
                        settingsViewModel.updateLanguageUnitMyPref(strLanguage)
                        Util.setPref(
                            requireContext(),
                            Constant.PREFERENCE_SELECTED_LANGUAGE,
                            strLanguage
                        )
                        bottomSheetDialog.dismiss()
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        (requireActivity()).finishAffinity()
                    }
                })
                adp.setFilterable(true)
                adp.addAll(Util.getAllLanguageDataOptionArray(requireContext()))
                if (language != ""
                ) {
                    val selectedLanguage = language
                    Log.e("selectedLanguage:  ", selectedLanguage)
                    val index = findIndex(
                        Util.getAllLanguageDataOptionArray(requireContext()),
                        selectedLanguage
                    )
                    adp.changeSelection(index!!, false)
                }
                val rvLanguages = bottomSheetDialog.findViewById<RecyclerView>(R.id.rvLanguage)
                rvLanguages!!.setHasFixedSize(true)
                rvLanguages.layoutManager = LinearLayoutManager(requireContext())
                rvLanguages.adapter = adp


                edtFindLang!!.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        if (editable.toString().trim { it <= ' ' }.isNotEmpty()) {
                            Log.e(
                                "TAG",
                                "afterTextChanged:::==>>  " + editable.toString()
                                    .trim { it <= ' ' })
                            adp.filter!!.filter(editable.toString().trim { it <= ' ' })
                        } else {
                            adp.addAll(Util.getAllLanguageDataOptionArray(requireContext()))
                        }
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                })
            }
        }


        bottomSheetDialog.setOnDismissListener {
//            setLocalData()
        }
        bottomSheetDialog.setOnShowListener { dialogInterface ->
            bgTrans(dialogInterface)
        }

        bottomSheetDialog.show()
    }

    private fun bgTrans(dialogInterface: DialogInterface) {

        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
            ?: return
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun findIndex(arr: ArrayList<LanguagesData>, item: String): Int? {
        return (arr.indices)
            .firstOrNull { i: Int -> item == arr[i].langName }
    }


    class LanguagesData(var langName: String, var isSelected: Boolean)

    private var mProgressDialog: ProgressDialog? = null
    private fun showProgress() {
        requireActivity().runOnUiThread {
            mProgressDialog = ProgressDialog(requireContext())
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.setTitle(getString(R.string.sync))
            mProgressDialog!!.setMessage(getString(R.string.please_wait))
            Handler(Looper.getMainLooper()).postDelayed({
                mProgressDialog!!.show()
            }, 100)
        }
    }

    private fun cancelProgress() {
        requireActivity().runOnUiThread {
            Handler(Looper.getMainLooper()).postDelayed({
                mProgressDialog!!.dismiss()
            }, 100)
        }
    }

    private fun getPreferenceLocalAndSetServer() {
        val map = HashMap<String, Any>()

        val autoId: DocumentReference =
            fireStore.collection(Constant.TABLE_USERS)
                .document(firebaseUser!!.uid)
                .collection(Constant.TABLE_PREFERENCE)
                .document(Constant.TABLE_PREFERENCE)

        map[Constant.TABLE_PREFERENCE_GENDER] = myPrefLocal!!.gender
        map[Constant.TABLE_PREFERENCE_DAILY_GOAL] = myPrefLocal!!.dailyGoal
        map[Constant.TABLE_PREFERENCE_DISTANCE_UNIT] = myPrefLocal!!.distanceUnit
        map[Constant.TABLE_PREFERENCE_REMINDER_DAYS] = myPrefLocal!!.reminderDays
        map[Constant.TABLE_PREFERENCE_HOUR_REMINDER] =
            if (myPrefLocal!!.reminderTimeHour != null) myPrefLocal!!.reminderTimeHour!! else 0
        map[Constant.TABLE_PREFERENCE_MINUTE_REMINDER] =
            if (myPrefLocal!!.reminderTimeMinute != null) myPrefLocal!!.reminderTimeMinute!! else 0
        map[Constant.TABLE_PREFERENCE_LANGUAGE] = myPrefLocal!!.language



        autoId.set(map).addOnSuccessListener {
            settingsViewModel.updateReminderIsSyncMyPref(true)

//            Util.setPref(requireContext(), Constant.IS_STORE_PREFERENCE_SYNC, true)

        }
    }

    private fun getMyRunningTableDataAndSetServer() {

        val allHistory: List<MyRunningEntity> = settingsViewModel.getAllRunningHistoryOnce()

        val idList = mutableListOf<Int>()
        val deletedIdList = mutableListOf<Int>()
        fireStore.runBatch { batch ->
            for (myRunning in allHistory) {
                val historyRef =
                    fireStore.collection(Constant.TABLE_USERS).document(firebaseUser!!.uid)
                        .collection(Constant.TABLE_RUNNING_HISTORY)
                        .document(myRunning.id.toString())
                if (myRunning.image != null && myRunning.imageString == null) {
                    myRunning.imageString = myRunning.image!!.toBase64String()
                }
                myRunning.image = null
                if (myRunning.isDeleted) {
                    batch.delete(historyRef)
                    deletedIdList.add(myRunning.id!!)
                } else {
                    batch.set(historyRef, myRunning)
                    idList.add(myRunning.id!!)
                }
            }
        }.addOnCompleteListener {
            if (idList.isNotEmpty()) settingsViewModel.updateIsSyncMyHistory(true, idList)
            if (deletedIdList.isNotEmpty()) settingsViewModel.hardDeleteHistoryByIds(deletedIdList)
            cancelProgress()
        }
    }

    private fun ByteArray.toBase64String(): String = Base64.encodeToString(this, Base64.DEFAULT)

    private fun String.toByteArray(): ByteArray = Base64.decode(this, Base64.DEFAULT)
    private fun getPreferenceServerAndSetLocal() {
        fireStore.collection(Constant.TABLE_USERS).document(firebaseUser!!.uid)
            .collection(Constant.TABLE_PREFERENCE).get()
            .addOnSuccessListener { documentSnapshots ->
                if (documentSnapshots.isEmpty) {
                    Log.d("TAG", "getServerPreferenceAndSetLocal: LIST EMPTY")
                    getPreferenceLocalAndSetServer()
                } else {
                    val preferenceData: List<MyPref> =
                        documentSnapshots.toObjects(MyPref::class.java)
                    val myPref = preferenceData[0]
                    myPref.id = 1
                    settingsViewModel.insertMyPref(myPref)
                }
                cancelProgress()
            }.addOnFailureListener {
                Log.d("TAG", "getServerPreferenceAndSetLocal: ${it.message}")
                cancelProgress()
            }

    }

    private fun getHistoryServerAndSetLocal() {
        fireStore.collection(Constant.TABLE_USERS).document(firebaseUser!!.uid)
            .collection(Constant.TABLE_RUNNING_HISTORY).get()
            .addOnSuccessListener { documentSnapshots ->
                if (documentSnapshots.isEmpty) {
                    Log.d("TAG", "getServerPreferenceAndSetLocal: LIST EMPTY")
                    getMyRunningTableDataAndSetServer()
                } else {
                    val myRunningEntityData: List<MyRunningEntity> =
                        documentSnapshots.toObjects(MyRunningEntity::class.java)
                    for (myRunningHistory in myRunningEntityData) {
                        myRunningHistory.image = myRunningHistory.imageString!!.toByteArray()
                        myRunningHistory.isSynchronized = true
                    }
                    settingsViewModel.insertAll(myRunningEntityData)
                }
                cancelProgress()
            }.addOnFailureListener {
                Log.d("TAG", "getServerPreferenceAndSetLocal: ${it.message}")
                cancelProgress()
            }

    }

    private fun setListData() {
        achievementDataList.clear()
        achievementDataList.addAll(
            listOf(
                AchievementData(distanceUnit, 50, R.drawable.ic_50_km, false),
                AchievementData(distanceUnit, 100, R.drawable.ic_100_km, false),
                AchievementData(distanceUnit, 500, R.drawable.ic_500_km, false),
                AchievementData(
                    distanceUnit,
                    1000,
                    R.drawable.ic_1000_km,
                    false,
                    resources.getDimension(sdp._10sdp)
                ),
                AchievementData(
                    distanceUnit,
                    5000,
                    R.drawable.ic_5000_km,
                    false,
                    resources.getDimension(sdp._10sdp)
                ),
                AchievementData(
                    distanceUnit,
                    10000,
                    R.drawable.ic_10000_km,
                    false,
                    resources.getDimension(sdp._10sdp)
                ),
                AchievementData(distanceUnit, 20000, R.drawable.ic_20000_km, false),
                AchievementData(distanceUnit, 30000, R.drawable.ic_30000_km, false),
                AchievementData(distanceUnit, 50000, R.drawable.ic_50000_km, false),
                AchievementData(distanceUnit, 60000, R.drawable.ic_60000_km, false),
                AchievementData(distanceUnit, 75000, R.drawable.ic_75000_km, false),
                AchievementData(distanceUnit, 100000, R.drawable.ic_100000_km, false),
            )
        )
    }
}