package com.evora.saglik

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.evora.saglik.data.PrefsManager
import com.evora.saglik.databinding.ActivityMainBinding
import com.evora.saglik.receiver.DailyResetReceiver
import com.evora.saglik.service.StepCounterService
import com.evora.saglik.ui.HomeFragment
import com.evora.saglik.ui.ProfileFragment
import com.evora.saglik.ui.StepsFragment
import com.evora.saglik.ui.WaterFragment
import com.evora.saglik.worker.WaterReminderWorker

/**
 * Uygulamanın tek Activity'si. Alt sekmeler (Ana Sayfa / Adım / Su / Profil)
 * arasında geçişi yönetir; sensör izinleri, arka plan servisi ve periyodik
 * görevlerin kurulumu burada yapılır.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsManager

    private val permissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // İzin sonucu ne olursa olsun servisi başlatmayı dene (izin yoksa sensör 0 kalır)
        startStepService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)
        prefs.resetIfNewDay(currentStepSensorValue = null)

        requestPermissionsAndStart()
        WaterReminderWorker.schedule(this)
        DailyResetReceiver.scheduleNext(this)

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_steps -> StepsFragment()
                R.id.nav_water -> WaterFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            showFragment(fragment)
            true
        }

        if (savedInstanceState == null) {
            if (!prefs.isProfileSet()) {
                // İlk açılış: kullanıcıyı doğrudan profil sekmesine yönlendir
                binding.bottomNav.selectedItemId = R.id.nav_profile
            } else {
                binding.bottomNav.selectedItemId = R.id.nav_home
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // ---------- İzinler ----------

    private fun requestPermissionsAndStart() {
        val permissionsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsNeeded.isNotEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            startStepService()
        }
    }

    private fun startStepService() {
        val intent = Intent(this, StepCounterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
    }
}
