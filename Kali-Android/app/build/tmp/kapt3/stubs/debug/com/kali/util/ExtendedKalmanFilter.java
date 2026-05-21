package com.kali.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u0013\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\u001d\n\u0002\u0010\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0019\u001a\u00020\u0010J\u0006\u0010\u001a\u001a\u00020\u0010J\u0006\u0010\u001b\u001a\u00020\u0005J\u0006\u0010\u001c\u001a\u00020\u0010J\u0006\u0010\u001d\u001a\u00020\u0005J/\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u0002\u00a2\u0006\u0002\u0010!J\u0013\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u0002\u00a2\u0006\u0002\u0010\u0007J\u0013\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u0002\u00a2\u0006\u0002\u0010\u0007J!\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u0002\u00a2\u0006\u0002\u0010&J/\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u0002\u00a2\u0006\u0002\u0010!J#\u0010(\u001a\u00020\u00052\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010)\u001a\u00020\u0005H\u0002\u00a2\u0006\u0002\u0010*J/\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u0002\u00a2\u0006\u0002\u0010!J!\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u0002\u00a2\u0006\u0002\u0010&J\u000e\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\u0010J\u0006\u00100\u001a\u00020.J\u0016\u00101\u001a\u00020.2\u0006\u00102\u001a\u00020\u00102\u0006\u00103\u001a\u00020\u0010R\"\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0086\u000e\u00a2\u0006\u0010\n\u0002\u0010\n\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001e\u0010\r\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\f@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0010X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0010X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0010X\u0082D\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018\u00a8\u00064"}, d2 = {"Lcom/kali/util/ExtendedKalmanFilter;", "", "()V", "P", "", "", "getP", "()[[D", "setP", "([[D)V", "[[D", "<set-?>", "", "isInitialized", "()Z", "lastTime", "", "measurementNoiseVar", "processNoisePosVar", "processNoiseVelVar", "x", "getX", "()[D", "setX", "([D)V", "getBearing", "getEstimationError", "getPosition", "getSpeedKmh", "getVelocity", "mat4Add", "A", "B", "([[D[[D)[[D", "mat4Create", "mat4Identity", "mat4Inverse", "m", "([[D)[[D", "mat4Mul", "mat4MulVec", "v", "([[D[D)[D", "mat4Sub", "mat4Transpose", "predict", "", "dt", "reset", "update", "measuredLat", "measuredLng", "app_debug"})
public final class ExtendedKalmanFilter {
    @org.jetbrains.annotations.NotNull()
    private double[] x;
    @org.jetbrains.annotations.NotNull()
    private double[][] P;
    private final double processNoisePosVar = 1.0E-5;
    private final double processNoiseVelVar = 1.0E-4;
    private final double measurementNoiseVar = 3.0E-4;
    private boolean isInitialized = false;
    private double lastTime = 0.0;
    
    public ExtendedKalmanFilter() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final double[] getX() {
        return null;
    }
    
    public final void setX(@org.jetbrains.annotations.NotNull()
    double[] p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final double[][] getP() {
        return null;
    }
    
    public final void setP(@org.jetbrains.annotations.NotNull()
    double[][] p0) {
    }
    
    public final boolean isInitialized() {
        return false;
    }
    
    public final void reset() {
    }
    
    public final void predict(double dt) {
    }
    
    public final void update(double measuredLat, double measuredLng) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final double[] getPosition() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final double[] getVelocity() {
        return null;
    }
    
    public final double getSpeedKmh() {
        return 0.0;
    }
    
    public final double getBearing() {
        return 0.0;
    }
    
    public final double getEstimationError() {
        return 0.0;
    }
    
    private final double[][] mat4Create() {
        return null;
    }
    
    private final double[][] mat4Identity() {
        return null;
    }
    
    private final double[][] mat4Add(double[][] A, double[][] B) {
        return null;
    }
    
    private final double[][] mat4Sub(double[][] A, double[][] B) {
        return null;
    }
    
    private final double[][] mat4Mul(double[][] A, double[][] B) {
        return null;
    }
    
    private final double[][] mat4Transpose(double[][] A) {
        return null;
    }
    
    private final double[] mat4MulVec(double[][] A, double[] v) {
        return null;
    }
    
    private final double[][] mat4Inverse(double[][] m) {
        return null;
    }
}