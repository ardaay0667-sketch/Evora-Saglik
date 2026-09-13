package com.evora.saglik.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.evora.saglik.databinding.FragmentHomeBinding
import com.evora.saglik.util.HealthCalculator
import java.util.Locale

class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun refreshUi() {
        if (_binding == null) return
        val age = prefs.age
        val weight = prefs.weightKg
        val steps = prefs.dailySteps
        val water = prefs.dailyWaterMl

        val stepGoal = HealthCalculator.idealDailySteps(age, weight)
        val waterGoal = HealthCalculator.dailyWaterTargetMl(age, weight)
        val distanceKm = HealthCalculator.distanceKm(steps)
        val calories = HealthCalculator.caloriesBurned(steps, weight)

        binding.tvGreeting.text = greetingMessage(steps, stepGoal)

        binding.tvHomeSteps.text = "$steps / $stepGoal adım"
        binding.progressHomeSteps.max = stepGoal
        binding.progressHomeSteps.progress = steps.coerceAtMost(stepGoal)

        binding.tvHomeWater.text = "$water / $waterGoal ml"
        binding.progressHomeWater.max = waterGoal
        binding.progressHomeWater.progress = water.coerceAtMost(waterGoal)

        binding.tvHomeDistance.text = String.format(Locale.getDefault(), "%.2f km", distanceKm)
        binding.tvHomeCalories.text = String.format(Locale.getDefault(), "%.0f kcal", calories)
    }

    private fun greetingMessage(steps: Int, goal: Int): String {
        val ratio = if (goal > 0) steps.toFloat() / goal else 0f
        return when {
            ratio >= 1f -> "Harika! Günlük adım hedefine ulaştın 🎉"
            ratio >= 0.5f -> "Yolun yarısındasın, devam et!"
            steps == 0 -> "Bugüne henüz başlamadın, hadi yürüyüşe çık!"
            else -> "İyi gidiyorsun, biraz daha hareket et."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
