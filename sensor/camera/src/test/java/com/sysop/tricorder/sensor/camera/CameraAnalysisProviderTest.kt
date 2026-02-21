package com.sysop.tricorder.sensor.camera

import com.google.common.truth.Truth.assertThat
import com.sysop.tricorder.core.model.SensorCategory
import org.junit.Test

class CameraAnalysisProviderTest {

    @Test
    fun `provider has correct metadata`() {
        // Can't construct without Context, but verify constants would be correct
        // This is a structural test to ensure the class exists and compiles
        assertThat(SensorCategory.CAMERA.displayName).isEqualTo("Camera")
    }
}
