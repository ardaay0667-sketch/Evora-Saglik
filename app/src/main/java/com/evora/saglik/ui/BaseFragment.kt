package com.evora.saglik.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.fragment.app.Fragment
import com.evora.saglik.data.PrefsManager
import com.evora.saglik.service.StepCounterService

/**
 * Tüm sekmelerin ortak atası. Adım verisi her değiştiğinde (servisten gelen
 * broadcast ile) refreshUi() otomatik çağrılır, böylece hangi sekmede olursan ol
 * veriler güncel kalır.
 */
abstract class BaseFragment : Fragment() {

    protected lateinit var prefs: PrefsManager

    private val stepsUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshUi()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefs = PrefsManager(context)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(StepCounterService.ACTION_STEPS_UPDATED)
        val ctx = requireContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.registerReceiver(stepsUpdatedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ctx.registerReceiver(stepsUpdatedReceiver, filter)
        }
        prefs.resetIfNewDay(currentStepSensorValue = null)
        refreshUi()
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(stepsUpdatedReceiver)
    }

    /** Her sekme kendi arayüzünü bu fonksiyonda günceller */
    abstract fun refreshUi()
}
