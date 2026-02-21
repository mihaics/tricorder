package com.sysop.tricorder.sensor.audio

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class FftProcessorTest {

    @Test
    fun `fft detects peak at correct frequency`() {
        val sampleRate = 1024
        val frequency = 100.0
        val samples = FloatArray(1024) { i ->
            sin(2.0 * PI * frequency * i / sampleRate).toFloat()
        }
        val magnitudes = FftProcessor.fft(samples)
        val peakFreq = FftProcessor.peakFrequency(magnitudes, sampleRate)
        // Should be close to 100 Hz (within 1 bin = 1 Hz for 1024 samples at 1024 Hz)
        assertThat(peakFreq).isWithin(2.0).of(frequency)
    }

    @Test
    fun `computeDbSpl returns negative for silence`() {
        val silence = FloatArray(1024) { 0f }
        assertThat(FftProcessor.computeDbSpl(silence)).isLessThan(-90.0)
    }

    @Test
    fun `fft requires power of 2`() {
        try {
            FftProcessor.fft(FloatArray(100))
            assertThat(false).isTrue() // should not reach here
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("power of 2")
        }
    }
}
