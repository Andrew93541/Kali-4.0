package com.kali.ui.sos;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\u0010\u0010\u0017\u001a\u00020\u00142\u0006\u0010\u0018\u001a\u00020\u0016H\u0002J\u0012\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0015J\b\u0010\u001d\u001a\u00020\u001aH\u0014J\b\u0010\u001e\u001a\u00020\u001aH\u0014J\b\u0010\u001f\u001a\u00020\u001aH\u0014J\b\u0010 \u001a\u00020\u001aH\u0002J\b\u0010!\u001a\u00020\u001aH\u0002J\b\u0010\"\u001a\u00020\u001aH\u0002J\u001e\u0010#\u001a\u00020\u001a2\f\u0010$\u001a\b\u0012\u0004\u0012\u00020&0%2\u0006\u0010\'\u001a\u00020(H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u000b\u001a\u00020\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000f\u0010\u0010\u001a\u0004\b\r\u0010\u000eR\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lcom/kali/ui/sos/TrackingActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "activeAlert", "Lcom/kali/network/Alert;", "binding", "Lcom/kali/databinding/ActivityTrackingBinding;", "isFirstLoad", "", "lastUserInteractedTime", "", "session", "Lcom/kali/util/SessionManager;", "getSession", "()Lcom/kali/util/SessionManager;", "session$delegate", "Lkotlin/Lazy;", "trackingJob", "Lkotlinx/coroutines/Job;", "calculateEta", "", "distanceKm", "", "getCompassDirection", "bearing", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onPause", "onResume", "resetTelemetryUI", "startTracking", "stopTracking", "updateMapAndTelemetry", "locations", "", "Lcom/kali/network/HistoricalLocation;", "stats", "Lcom/kali/network/TelemetryStats;", "app_debug"})
public final class TrackingActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.kali.databinding.ActivityTrackingBinding binding;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy session$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job trackingJob;
    @org.jetbrains.annotations.Nullable()
    private com.kali.network.Alert activeAlert;
    private long lastUserInteractedTime = 0L;
    private boolean isFirstLoad = true;
    
    public TrackingActivity() {
        super();
    }
    
    private final com.kali.util.SessionManager getSession() {
        return null;
    }
    
    @java.lang.Override()
    @android.annotation.SuppressLint(value = {"ClickableViewAccessibility"})
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void startTracking() {
    }
    
    private final void updateMapAndTelemetry(java.util.List<com.kali.network.HistoricalLocation> locations, com.kali.network.TelemetryStats stats) {
    }
    
    private final void resetTelemetryUI() {
    }
    
    private final java.lang.String getCompassDirection(double bearing) {
        return null;
    }
    
    private final java.lang.String calculateEta(double distanceKm) {
        return null;
    }
    
    private final void stopTracking() {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    @java.lang.Override()
    protected void onPause() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
}