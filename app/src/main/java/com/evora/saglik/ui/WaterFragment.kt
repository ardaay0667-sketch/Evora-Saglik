package com.evora.saglik.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.evora.saglik.databinding.FragmentWaterBinding
import com.evora.saglik.util.HealthCalculator
import com.evora.saglik.widget.EvoraWidgetProvider

class WaterFragment : BaseFragment() {

    private var _binding: FragmentWaterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddWater.setOnClickListener {
            prefs.addWater(100)
            refreshUi()
            EvoraWidgetProvider.requestUpdate(requireContext())
        }
        binding.btnResetWater.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Suyu sıfırla")
                .setMessage("Bugünkü su takibini 0 ml'ye sıfırlamak istiyor musun?")
                .setPositiveButton("Evet") { _, _ ->
                    prefs.resetWaterManually()
                    refreshUi()
                    EvoraWidgetProvider.requestUpdate(requireContext())
                }
                .setNegativeButton("Vazgeç", null)
                .show()
        }
    }

    override fun refreshUi() {
        if (_binding == null) return
        val age = prefs.age
        val weight = prefs.weightKg
        val water = prefs.dailyWaterMl

        val waterGoal = HealthCalculator.dailyWaterTargetMl(age, weight)
        val remaining = (waterGoal - water).coerceAtLeast(0)

        binding.tvWaterBig.text = "$water ml"
        binding.tvWaterGoalLabel.text = "Hedef: $waterGoal ml"
        binding.progressWater.max = waterGoal
        binding.progressWater.progress = water.coerceAtMost(waterGoal)

        binding.tvWaterRemaining.text = if (remaining > 0) {
            "Hedefe kalan: $remaining ml"
        } else {
            "Günlük su hedefine ulaştın! 💧"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
