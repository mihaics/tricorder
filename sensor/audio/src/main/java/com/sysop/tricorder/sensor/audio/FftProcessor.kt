package com.sysop.tricorder.sensor.audio

import kotlin.math.*

object FftProcessor {

    fun fft(input: FloatArray): FloatArray {
        val n = input.size
        require(n > 0 && n and (n - 1) == 0) { "Input size must be a power of 2" }

        // Apply Hanning window
        val windowed = FloatArray(n) { i ->
            input[i] * (0.5f * (1 - cos(2.0 * PI * i / (n - 1)))).toFloat()
        }

        // Cooley-Tukey FFT
        val real = windowed.copyOf()
        val imag = FloatArray(n)
        cooleyTukey(real, imag, n)

        // Return magnitude spectrum (first half only — symmetric)
        val halfN = n / 2
        return FloatArray(halfN) { i ->
            sqrt(real[i] * real[i] + imag[i] * imag[i])
        }
    }

    private fun cooleyTukey(real: FloatArray, imag: FloatArray, n: Int) {
        // Bit-reversal permutation
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                var temp = real[i]; real[i] = real[j]; real[j] = temp
                temp = imag[i]; imag[i] = imag[j]; imag[j] = temp
            }
            var k = n / 2
            while (k <= j) {
                j -= k
                k /= 2
            }
            j += k
        }

        // FFT butterfly
        var step = 2
        while (step <= n) {
            val halfStep = step / 2
            val angle = -2.0 * PI / step
            for (i in 0 until n step step) {
                for (k in 0 until halfStep) {
                    val w = angle * k
                    val cosW = cos(w).toFloat()
                    val sinW = sin(w).toFloat()
                    val tReal = cosW * real[i + k + halfStep] - sinW * imag[i + k + halfStep]
                    val tImag = sinW * real[i + k + halfStep] + cosW * imag[i + k + halfStep]
                    real[i + k + halfStep] = real[i + k] - tReal
                    imag[i + k + halfStep] = imag[i + k] - tImag
                    real[i + k] += tReal
                    imag[i + k] += tImag
                }
            }
            step *= 2
        }
    }

    fun computeDbSpl(samples: FloatArray): Double {
        if (samples.isEmpty()) return -96.0
        val rms = sqrt(samples.map { it * it }.average())
        return if (rms > 0) 20.0 * log10(rms) else -96.0
    }

    fun peakFrequency(magnitudes: FloatArray, sampleRate: Int): Double {
        if (magnitudes.isEmpty()) return 0.0
        val peakIndex = magnitudes.indices.maxByOrNull { magnitudes[it] } ?: 0
        return peakIndex.toDouble() * sampleRate / (magnitudes.size * 2)
    }
}
