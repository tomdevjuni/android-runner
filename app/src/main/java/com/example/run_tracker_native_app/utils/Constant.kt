package com.example.run_tracker_native_app.utils


object Constant {

    const val RequestCodePermission = 333
    const val RequestCodeBackgroundPermission = 334
    const val SKU_ID = "pro_version" // Set your Subscription ID at here
    const val PRIVACY_POLICY = "https://sites.google.com/benzatine.com/run-tracker/home" // Set your Privacy policy URL at here
    const val CONTACT_US = "shreyuinfotech2019@gmail.com" // Set your Contact Email at here

    const val IS_LOGIN = "IS_LOGIN"
    var IS_SERVICE_RUNNING_BOOL = false

    /*Preference Table*/
    const val TABLE_RUNNING_HISTORY = "RunningHistory"
    const val TABLE_PREFERENCE = "Preference"
    const val TABLE_PREFERENCE_GENDER = "gender"
    const val TABLE_PREFERENCE_DAILY_GOAL = "dailyGoal"
    const val TABLE_PREFERENCE_DISTANCE_UNIT = "distanceUnit"
    const val TABLE_PREFERENCE_REMINDER_DAYS = "reminderDays"
    const val TABLE_PREFERENCE_HOUR_REMINDER = "reminderTimeHour"
    const val TABLE_PREFERENCE_MINUTE_REMINDER = "reminderTimeMinute"
    const val TABLE_PREFERENCE_LANGUAGE = "language"

    /*User Table*/
    const val TABLE_USERS = "RunTrackerUsers"
    const val USER_NAME = "user_name"
    const val USER_EMAIL = "user_email"
    const val USER_UID = "user_uid"

    const val KEY_SET_GOAL = "key_set_goal"
    const val IS_FIRST_TIME_INTRO = "is_first_time_intro"
    const val formatMMMYYYY = "MMM yyyy"
    const val formatM = "M"
    const val formatYYYY = "yyyy"
    const val formatDDMMYYYY = "dd/MM/yyyy"
    const val formatMMMDDAHHMM = "MMM dd, HH:mm a"
    const val formatYYYYMMDD = "yyyy-MM-dd"
    const val formatFull = "yyyy-MM-dd HH:mm:ss"
    const val SHARE_WP = "share_wp"
    const val SHARE_INSTA = "share_insta"
    const val SHARE_FB = "share_fb"
    const val SELECTED_UNIT_KM_MILE = "SELECTED_UNIT_KM_MILE"
    const val PREFERENCE_CURRANT_RUNNING_SESSION_ID = "preference_currant_running_session_time"
    const val EXTRA_REMINDER_ID = "Reminder_ID"
    const val PREF_KEY_PURCHASE_STATUS = "pref_key_purchase_status"
    const val PREFERENCE_SELECTED_LANGUAGE = "preference_selected_language"
    const val IS_PROFILE_INTRO_DONE = "IS_PROFILE_INTRO_DONE"
    const val LAST_SYNC_DATE = "LAST_SYNC_DATE"

    const val GOOGLE_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"//Change your App Id for AdMob here
    const val GOOGLE_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"//Change your Banner Id for AdMob here
    const val GOOGLE_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"//Change your Interstitial Id for AdMob here

    const val FB_BANNER_ID = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID" //Change your Banner Id for Facebook here
    const val FB_INTERSTITIAL_ID = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"//Change your Interstitial Id for Facebook here

    const val AD_FACEBOOK = "facebook"
    const val AD_GOOGLE = "google"
    const val AD_TYPE_FACEBOOK_GOOGLE = AD_GOOGLE

    const val ENABLE = "Enable"
    const val DISABLE = "Disable"
    const val ENABLE_DISABLE = ENABLE

    const val AD_TYPE_FB_GOOGLE = "AD_TYPE_FB_GOOGLE"
    const val GOOGLE_BANNER = "GOOGLE_BANNER"
    const val GOOGLE_INTERSTITIAL = "GOOGLE_INTERSTITIAL"
    const val FB_BANNER = "FB_BANNER"
    const val FB_INTERSTITIAL = "FB_INTERSTITIAL"
    const val SPLASH_SCREEN_COUNT = "splash_screen_count"
    const val STATUS_ENABLE_DISABLE = "STATUS_ENABLE_DISABLE"

}