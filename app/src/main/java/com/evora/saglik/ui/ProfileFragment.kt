package com.evora.saglik.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.evora.saglik.databinding.FragmentProfileBinding
import com.evora.saglik.util.HealthCalculator
import com.evora.saglik.widget.EvoraWidgetProvider

class ProfileFragment : BaseFragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSaveProfile.setOnClickListener { saveProfile() }
    }

    private fun saveProfile() {
        val age = binding.etAge.text.toString().toIntOrNull()
        val weight = binding.etWeight.text.toString().toDoubleOrNull()

        if (age == null || age <= 0 || age > 120) {
            binding.etAge.error = "Geçerli bir yaş gir"
            return
        }
        if (weight == null || weight <= 0 || weight > 300) {
            binding.etWeight.error = "Geçerli bir kilo gir"
            return
        }

        prefs.saveProfile(age, weight)
        EvoraWidgetProvider.requestUpdate(requireContext())
        Toast.makeText(requireContext(), "Bilgilerin kaydedildi", Toast.LENGTH_SHORT).show()
        refreshUi()
    }

    override fun refreshUi() {
        if (_binding == null) return

        // Kullanıcı yazarken alanların üzerine yazmamak için sadece boşsa doldur
        if (binding.etAge.text.isNullOrEmpty()) {
            binding.etAge.setText(prefs.age.toString())
        }
        if (binding.etWeight.text.isNullOrEmpty()) {
            binding.etWeight.setText(formatWeight(prefs.weightKg))
        }

        val stepGoal = HealthCalculator.idealDailySteps(prefs.age, prefs.weightKg)
        val waterGoal = HealthCalculator.dailyWaterTargetMl(prefs.age, prefs.weightKg)

        binding.tvCalculatedGoals.text =
            "🚶 Günlük adım hedefi: $stepGoal adım\n💧 Günlük su hedefi: $waterGoal ml"
    }

    private fun formatWeight(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
