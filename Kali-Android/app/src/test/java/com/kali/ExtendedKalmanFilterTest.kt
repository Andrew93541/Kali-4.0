package com.kali

import com.kali.util.ExtendedKalmanFilter
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class ExtendedKalmanFilterTest {

    @Test
    fun testInitialization() {
        val ekf = ExtendedKalmanFilter()
        assertFalse(ekf.isInitialized)

        // First update must initialize the filter
        ekf.update(12.9716, 77.5946)
        assertTrue(ekf.isInitialized)

        val pos = ekf.getPosition()
        assertEquals(12.9716, pos[0], 1e-6)
        assertEquals(77.5946, pos[1], 1e-6)
        assertEquals(0.0, ekf.getSpeedKmh(), 1e-6)
    }

    @Test
    fun testReset() {
        val ekf = ExtendedKalmanFilter()
        ekf.update(12.9716, 77.5946)
        assertTrue(ekf.isInitialized)

        ekf.reset()
        assertFalse(ekf.isInitialized)
        assertEquals(0.0, ekf.x[0], 1e-6)
        assertEquals(1.0, ekf.P[0][0], 1e-6)
    }

    @Test
    fun testMovementAndSpeedEstimation() {
        val ekf = ExtendedKalmanFilter()
        
        // Initial coordinate
        ekf.update(12.9716, 77.5946)
        
        // Simulate a moving linear trajectory over 5 seconds (constant speed traveling north-east)
        // Travel 0.0001 deg North and 0.0001 deg East per second
        var curLat = 12.9716
        var curLng = 77.5946
        
        for (i in 1..5) {
            // Wait 1.0 second between updates
            Thread.sleep(10) // Simulate timing
            curLat += 0.0001
            curLng += 0.0001
            ekf.update(curLat, curLng)
        }

        // Verify speed estimation is calculated and is reasonable (> 0)
        val speed = ekf.getSpeedKmh()
        assertTrue("Speed estimation ($speed km/h) should be positive", speed > 0.0)
        
        // Verify bearing estimation is positive and around 45 degrees (North-East)
        val bearing = ekf.getBearing()
        assertTrue("Bearing ($bearing) should be around 45 degrees", abs(bearing - 45.0) < 15.0)
    }

    @Test
    fun testNoiseSuppressionAndConvergence() {
        val ekf = ExtendedKalmanFilter()
        ekf.update(12.9716, 77.5946)
        
        val initialError = ekf.getEstimationError()
        
        // Update repeatedly with small variations
        for (i in 1..10) {
            ekf.update(12.9716 + 0.00001 * (i % 2), 77.5946 - 0.00001 * (i % 2))
        }

        val finalError = ekf.getEstimationError()
        assertTrue("Estimation error should decrease (converge) after updates. Initial: $initialError, Final: $finalError", finalError < initialError)
    }
}
