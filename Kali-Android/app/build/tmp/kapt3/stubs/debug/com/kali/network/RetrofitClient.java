package com.kali.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010J\u0016\u0010\u0011\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\t2\u0006\u0010\u0013\u001a\u00020\tR\u0011\u0010\u0003\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/kali/network/RetrofitClient;", "", "()V", "api", "Lcom/kali/network/KaliApiService;", "getApi", "()Lcom/kali/network/KaliApiService;", "apiInstance", "cachedIp", "", "cachedPort", "okHttpClient", "Lokhttp3/OkHttpClient;", "initialize", "", "context", "Landroid/content/Context;", "updateBaseUrl", "ip", "port", "app_debug"})
public final class RetrofitClient {
    @org.jetbrains.annotations.NotNull()
    private static java.lang.String cachedIp = "192.168.0.151";
    @org.jetbrains.annotations.NotNull()
    private static java.lang.String cachedPort = "3000";
    @org.jetbrains.annotations.Nullable()
    private static com.kali.network.KaliApiService apiInstance;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.OkHttpClient okHttpClient = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.kali.network.RetrofitClient INSTANCE = null;
    
    private RetrofitClient() {
        super();
    }
    
    public final void initialize(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void updateBaseUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String ip, @org.jetbrains.annotations.NotNull()
    java.lang.String port) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kali.network.KaliApiService getApi() {
        return null;
    }
}