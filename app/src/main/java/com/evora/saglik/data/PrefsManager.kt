package com.evora.saglik.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tüm kullanıcı verilerini cihazda (internetsiz) SharedPreferences ile saklar.
 * Adım/su gibi günlük veriler, tarih değiştiğinde otomatik sıfırlanır.
 */
class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ---------- Kullanıcı Bilgileri ----------

    var age: Int
        get() = prefs.getInt(KEY_AGE, DEFAULT_AGE)
        set(value) = prefs.edit().putInt(KEY_AGE, value).apply()

    var weightKg: Double
        get() = prefs.getFloat(KEY_WEIGHT, DEFAULT_WEIGHT).toDouble()
        set(value) = prefs.edit().putFloat(KEY_WEIGHT, value.toFloat()).apply()

    fun isProfileSet(): Boolean = prefs.contains(KEY_AGE) && prefs.contains(KEY_WEIGHT)

    fun saveProfile(age: Int, weightKg: Double) {
        prefs.edit()
            .putInt(KEY_AGE, age)
            .putFloat(KEY_WEIGHT, weightKg.toFloat())
            .apply()
    }

    // ---------- Adım Sayar ----------

    /** Sensörün cihaz açıldığından beri verdiği ham (kümülatif) değer, günlük sayımın referans noktası */
    var stepBaseline: Float
        get() = prefs.getFloat(KEY_STEP_BASELINE, -1f)
        set(value) = prefs.edit().putFloat(KEY_STEP_BASELINE, value).apply()

    var dailySteps: Int
        get() = prefs.getInt(KEY_DAILY_STEPS, 0)
        set(value) = prefs.edit().putInt(KEY_DAILY_STEPS, value).apply()

    // ---------- Su Takibi ----------

    var dailyWaterMl: Int
        get() = prefs.getInt(KEY_DAILY_WATER, 0)
        set(value) = prefs.edit().putInt(KEY_DAILY_WATER, value).apply()

    fun addWater(amountMl: Int) {
        dailyWaterMl = (dailyWaterMl + amountMl).coerceAtLeast(0)
    }

    fun resetWaterManually() {
        dailyWaterMl = 0
    }

    // ---------- Günlük Sıfırlama ----------

    var lastResetDate: String
        get() = prefs.getString(KEY_LAST_RESET_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_RESET_DATE, value).apply()

    fun todayString(): String = dateFormat.format(Date())

    /**
     * Tarih değiştiyse günlük sayaçları (adım, su) sıfırlar.
     * Adım sensör baseline'ı da sıfırlanır ki yeni gün 0'dan başlasın.
     * Her ekran açılışında ve servis her adım aldığında çağrılmalıdır.
     */
    fun resetIfNewDay(currentStepSensorValue: Float?) {
        val today = todayString()
        if (lastResetDate != today) {
            dailySteps = 0
            dailyWaterMl = 0
            lastResetDate = today
            if (currentStepSensorValue != null) {
                stepBaseline = currentStepSensorValue
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "evora_saglik_prefs"
        private const val KEY_AGE = "key_age"
        private const val KEY_WEIGHT = "key_weight"
        private const val KEY_STEP_BASELINE = "key_step_baseline"
        private const val KEY_DAILY_STEPS = "key_daily_steps"
        private const val KEY_DAILY_WATER = "key_daily_water"
        private const val KEY_LAST_RESET_DATE = "key_last_reset_date"

        private const val DEFAULT_AGE = 30
        private const val DEFAULT_WEIGHT = 70f
    }
}
