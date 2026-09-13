package com.evora.saglik.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.evora.saglik.databinding.FragmentStepsBinding
import com.evora.saglik.util.HealthCalculator
import java.util.Locale

class StepsFragment : BaseFragment() {

    private var _binding: FragmentStepsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun refreshUi() {
        if (_binding == null) return
        val age = prefs.age
        val weight = prefs.weightKg
        val steps = prefs.dailySteps

        val stepGoal = HealthCalculator.idealDailySteps(age, weight)
        val distanceKm = HealthCalculator.distanceKm(steps)
        val calories = HealthCalculator.caloriesBurned(steps, weight)
        val remaining = (stepGoal - steps).coerceAtLeast(0)

        binding.tvStepsBig.text = steps.toString()
        binding.tvStepsGoalLabel.text = "Hedef: $stepGoal adım"
        binding.progressSteps.max = stepGoal
        binding.progressSteps.progress = steps.coerceAtMost(stepGoal)

        binding.tvStepsDistance.text = String.format(Locale.getDefault(), "%.2f km", distanceKm)
        binding.tvStepsCalories.text = String.format(Locale.getDefault(), "%.0f kcal", calories)

        binding.tvStepsInfo.text = if (remaining > 0) {
            "Hedefe ulaşmak için $remaining adım daha at."
        } else {
            "Bugünkü adım hedefine ulaştın, harikasın!"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
