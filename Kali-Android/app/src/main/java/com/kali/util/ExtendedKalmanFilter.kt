package com.kali.util

import kotlin.math.*

class ExtendedKalmanFilter {

    var x = DoubleArray(4) // State vector: [lat, lng, vLat, vLng]
    var P = Array(4) { DoubleArray(4) } // Covariance matrix
    
    private val processNoisePosVar = 0.00001
    private val processNoiseVelVar = 0.0001
    private val measurementNoiseVar = 0.0003
    
    var isInitialized = false
        private set
        
    private var lastTime: Double = 0.0

    init {
        reset()
    }

    fun reset() {
        x = DoubleArray(4)
        P = Array(4) { DoubleArray(4) }
        for (i in 0..3) {
            P[i][i] = 1.0
        }
        isInitialized = false
        lastTime = 0.0
    }

    fun predict(dt: Double) {
        if (!isInitialized || dt <= 0.0) return

        // State transition matrix F
        val F = arrayOf(
            doubleArrayOf(1.0, 0.0, dt, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, dt),
            doubleArrayOf(0.0, 0.0, 1.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 1.0)
        )

        // Process noise matrix Q
        val dt2 = dt * dt
        val dt3 = dt2 * dt / 2.0
        val dt4 = dt2 * dt2 / 4.0
        val qp = processNoisePosVar
        val qv = processNoiseVelVar
        val Q = arrayOf(
            doubleArrayOf(qp + dt4 * qv, 0.0, dt3 * qv, 0.0),
            doubleArrayOf(0.0, qp + dt4 * qv, 0.0, dt3 * qv),
            doubleArrayOf(dt3 * qv, 0.0, dt2 * qv, 0.0),
            doubleArrayOf(0.0, dt3 * qv, 0.0, dt2 * qv)
        )

        // Predict state: x = F * x
        x = mat4MulVec(F, x)

        // Predict covariance: P = F * P * F^T + Q
        val FP = mat4Mul(F, P)
        val FT = mat4Transpose(F)
        val FPFT = mat4Mul(FP, FT)
        P = mat4Add(FPFT, Q)
    }

    fun update(measuredLat: Double, measuredLng: Double) {
        val now = System.currentTimeMillis() / 1000.0

        if (!isInitialized) {
            x = doubleArrayOf(measuredLat, measuredLng, 0.0, 0.0)
            P = Array(4) { DoubleArray(4) }
            P[0][0] = measurementNoiseVar
            P[1][1] = measurementNoiseVar
            P[2][2] = 0.001
            P[3][3] = 0.001
            isInitialized = true
            lastTime = now
            return
        }

        val dt = now - lastTime
        lastTime = now

        // 1. Prediction Step
        predict(dt)

        // Measurement matrix H (observe position only)
        val H = arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0)
        )

        // Measurement noise R
        val R = arrayOf(
            doubleArrayOf(measurementNoiseVar, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, measurementNoiseVar, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 1e10, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 1e10)
        )

        // Innovation: y = z - H * x
        val Hx = mat4MulVec(H, x)
        val z = doubleArrayOf(measuredLat, measuredLng, 0.0, 0.0)
        val y = doubleArrayOf(z[0] - Hx[0], z[1] - Hx[1], 0.0, 0.0)

        // Innovation covariance: S = H * P * H^T + R
        val HT = mat4Transpose(H)
        val HP = mat4Mul(H, P)
        val HPHT = mat4Mul(HP, HT)
        val S = mat4Add(HPHT, R)

        // Kalman gain: K = P * H^T * S^-1
        val PHT = mat4Mul(P, HT)
        val Sinv = mat4Inverse(S)
        val K = mat4Mul(PHT, Sinv)

        // Update state: x = x + K * y
        val Ky = mat4MulVec(K, y)
        x = doubleArrayOf(
            x[0] + Ky[0],
            x[1] + Ky[1],
            x[2] + Ky[2],
            x[3] + Ky[3]
        )

        // Update covariance: P = (I - K*H) * P
        val KH = mat4Mul(K, H)
        val IKH = mat4Sub(mat4Identity(), KH)
        P = mat4Mul(IKH, P)
    }

    fun getPosition(): DoubleArray {
        return doubleArrayOf(x[0], x[1])
    }

    fun getVelocity(): DoubleArray {
        return doubleArrayOf(x[2], x[3])
    }

    fun getSpeedKmh(): Double {
        val vLat = x[2]
        val vLng = x[3]
        val latKmPerDeg = 111.32
        val lngKmPerDeg = 111.32 * cos(x[0] * Math.PI / 180.0)
        val vLatKm = vLat * latKmPerDeg // km/s
        val vLngKm = vLng * lngKmPerDeg // km/s
        val speedKmS = sqrt(vLatKm * vLatKm + vLngKm * vLngKm)
        return speedKmS * 3600.0 // km/h
    }

    fun getBearing(): Double {
        val vLat = x[2]
        val vLng = x[3]
        if (abs(vLat) < 1e-12 && abs(vLng) < 1e-12) return 0.0
        val bearing = atan2(vLng, vLat) * 180.0 / Math.PI
        return (bearing + 360.0) % 360.0
    }

    fun getEstimationError(): Double {
        return sqrt(P[0][0] + P[1][1])
    }

    /* ═══════════════════════════════════════════════════════
       4×4 MATRIX MATH HELPERS
    ═══════════════════════════════════════════════════════ */
    private fun mat4Create(): Array<DoubleArray> {
        return Array(4) { DoubleArray(4) }
    }

    private fun mat4Identity(): Array<DoubleArray> {
        val m = mat4Create()
        for (i in 0..3) {
            m[i][i] = 1.0
        }
        return m
    }

    private fun mat4Add(A: Array<DoubleArray>, B: Array<DoubleArray>): Array<DoubleArray> {
        val R = mat4Create()
        for (i in 0..3) {
            for (j in 0..3) {
                R[i][j] = A[i][j] + B[i][j]
            }
        }
        return R
    }

    private fun mat4Sub(A: Array<DoubleArray>, B: Array<DoubleArray>): Array<DoubleArray> {
        val R = mat4Create()
        for (i in 0..3) {
            for (j in 0..3) {
                R[i][j] = A[i][j] - B[i][j]
            }
        }
        return R
    }

    private fun mat4Mul(A: Array<DoubleArray>, B: Array<DoubleArray>): Array<DoubleArray> {
        val R = mat4Create()
        for (i in 0..3) {
            for (j in 0..3) {
                for (k in 0..3) {
                    R[i][j] += A[i][k] * B[k][j]
                }
            }
        }
        return R
    }

    private fun mat4Transpose(A: Array<DoubleArray>): Array<DoubleArray> {
        val R = mat4Create()
        for (i in 0..3) {
            for (j in 0..3) {
                R[i][j] = A[j][i]
            }
        }
        return R
    }

    private fun mat4MulVec(A: Array<DoubleArray>, v: DoubleArray): DoubleArray {
        val r = DoubleArray(4)
        for (i in 0..3) {
            for (j in 0..3) {
                r[i] += A[i][j] * v[j]
            }
        }
        return r
    }

    private fun mat4Inverse(m: Array<DoubleArray>): Array<DoubleArray> {
        val inv = mat4Create()
        val a = m[0]
        val b = m[1]
        val c = m[2]
        val d = m[3]

        val s0 = a[0] * b[1] - b[0] * a[1]
        val s1 = a[0] * b[2] - b[0] * a[2]
        val s2 = a[0] * b[3] - b[0] * a[3]
        val s3 = a[1] * b[2] - b[1] * a[2]
        val s4 = a[1] * b[3] - b[1] * a[3]
        val s5 = a[2] * b[3] - b[2] * a[3]

        val c5 = c[2] * d[3] - d[2] * c[3]
        val c4 = c[1] * d[3] - d[1] * c[3]
        val c3 = c[1] * d[2] - d[1] * c[2]
        val c2 = c[0] * d[3] - d[0] * c[3]
        val c1 = c[0] * d[2] - d[0] * c[2]
        val c0 = c[0] * d[1] - d[0] * c[1]

        var det = s0 * c5 - s1 * c4 + s2 * c3 + s3 * c2 - s4 * c1 + s5 * c0
        if (abs(det) < 1e-20) {
            return mat4Identity()
        }
        det = 1.0 / det

        inv[0][0] = (b[1] * c5 - b[2] * c4 + b[3] * c3) * det
        inv[0][1] = (-a[1] * c5 + a[2] * c4 - a[3] * c3) * det
        inv[0][2] = (d[1] * s5 - d[2] * s4 + d[3] * s3) * det
        inv[0][3] = (-c[1] * s5 + c[2] * s4 - c[3] * s3) * det

        inv[1][0] = (-b[0] * c5 + b[2] * c2 - b[3] * c1) * det
        inv[1][1] = (a[0] * c5 - a[2] * c2 + a[3] * c1) * det
        inv[1][2] = (-d[0] * s5 + d[2] * s2 - d[3] * s1) * det
        inv[1][3] = (c[0] * s5 - c[2] * s2 + c[3] * s1) * det

        inv[2][0] = (b[0] * c4 - b[1] * c2 + b[3] * c0) * det
        inv[2][1] = (-a[0] * c4 + a[1] * c2 - a[3] * c0) * det
        inv[2][2] = (d[0] * s4 - d[1] * s2 + d[3] * s0) * det
        inv[2][3] = (-c[0] * s4 + c[1] * s2 - c[3] * s0) * det

        inv[3][0] = (-b[0] * c3 + b[1] * c1 - b[2] * c0) * det
        inv[3][1] = (a[0] * c3 - a[1] * c1 + a[2] * c0) * det
        inv[3][2] = (-d[0] * s3 + d[1] * s1 - d[2] * s0) * det
        inv[3][3] = (c[0] * s3 - c[1] * s1 + c[2] * s0) * det

        return inv
    }
}
