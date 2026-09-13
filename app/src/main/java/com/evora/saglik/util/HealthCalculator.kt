package com.evora.saglik.util

/**
 * Yaş ve kiloya göre günlük hedefleri, mesafe ve kaloriyi hesaplayan yardımcı sınıf.
 * NOT: Bu formüller genel sağlık rehberlerine dayanan basitleştirilmiş tahminlerdir,
 * tıbbi tavsiye yerine geçmez.
 */
object HealthCalculator {

    /** Ortalama adım uzunluğu (metre) - yetişkin ortalaması */
    const val AVERAGE_STEP_LENGTH_M = 0.762

    /**
     * Yaş ve kiloya göre günlük ideal adım hedefi.
     * Genel kılavuz: yetişkinler için ~10.000 adım baz alınır.
     * - Yaş arttıkça hedef hafifçe düşürülür (eklem/dayanıklılık farkı).
     * - Kilo arttıkça (fazla kilo yönetimi için) hedef hafifçe artırılır.
     */
    fun idealDailySteps(age: Int, weightKg: Double): Int {
        var target = 10000.0

        target += when {
            age < 18 -> 2000.0
            age in 18..40 -> 0.0
            age in 41..60 -> -1000.0
            else -> -2500.0 // 60 yaş üstü
        }

        // Kiloya göre küçük bir ayarlama (BMI'yi tam bilmesek de kilo üzerinden kabaca)
        target += when {
            weightKg < 55 -> -500.0
            weightKg in 55.0..85.0 -> 0.0
            else -> 1000.0
        }

        return target.coerceIn(4000.0, 15000.0).toInt()
    }

    /**
     * Yaş ve kiloya göre günlük içilmesi önerilen su miktarı (ml).
     * Temel formül: kilo (kg) * 35 ml, yaşa göre küçük düzeltme.
     */
    fun dailyWaterTargetMl(age: Int, weightKg: Double): Int {
        var target = weightKg * 35.0

        if (age > 60) {
            target *= 0.9 // yaşlılarda susama hissi azaldığından hedef biraz düşürülür ama takip önemlidir
        } else if (age < 18) {
            target *= 0.95
        }

        return target.coerceIn(1200.0, 4000.0).toInt()
    }

    /** Atılan adım sayısına göre yürünen mesafe (km) */
    fun distanceKm(steps: Int, stepLengthM: Double = AVERAGE_STEP_LENGTH_M): Double {
        return (steps * stepLengthM) / 1000.0
    }

    /**
     * Atılan adıma ve kiloya göre yakılan kalori (kcal).
     * Ortalama olarak 70 kg bir yetişkin adım başına ~0.04 kcal yakar;
     * bu değer kiloya göre ölçeklenir.
     */
    fun caloriesBurned(steps: Int, weightKg: Double): Double {
        val kcalPerStepAt70kg = 0.04
        val scaled = kcalPerStepAt70kg * (weightKg / 70.0)
        return steps * scaled
    }
}
