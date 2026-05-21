package com.kali.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J(\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ(\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00032\b\b\u0001\u0010\u0007\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u0011J\u001e\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00032\b\b\u0001\u0010\u0007\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u001e\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0007\u001a\u00020\u0016H\u00a7@\u00a2\u0006\u0002\u0010\u0017J<\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0019\u001a\u00020\u001a2\b\b\u0001\u0010\u001b\u001a\u00020\u001a2\b\b\u0001\u0010\u001c\u001a\u00020\u001dH\u00a7@\u00a2\u0006\u0002\u0010\u001e\u00a8\u0006\u001f"}, d2 = {"Lcom/kali/network/KaliApiService;", "", "addLocation", "Lretrofit2/Response;", "", "token", "", "body", "Lcom/kali/network/LocationRequest;", "(Ljava/lang/String;Lcom/kali/network/LocationRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createAlert", "Lcom/kali/network/AlertResponse;", "Lcom/kali/network/AlertRequest;", "(Ljava/lang/String;Lcom/kali/network/AlertRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "googleLogin", "Lcom/kali/network/LoginResponse;", "Lcom/kali/network/GoogleLoginRequest;", "(Lcom/kali/network/GoogleLoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "login", "Lcom/kali/network/LoginRequest;", "(Lcom/kali/network/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "register", "Lcom/kali/network/RegisterRequest;", "(Lcom/kali/network/RegisterRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadMedia", "alertId", "Lokhttp3/RequestBody;", "mediaType", "file", "Lokhttp3/MultipartBody$Part;", "(Ljava/lang/String;Lokhttp3/RequestBody;Lokhttp3/RequestBody;Lokhttp3/MultipartBody$Part;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public abstract interface KaliApiService {
    
    @retrofit2.http.POST(value = "auth/login")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object login(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.LoginRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.kali.network.LoginResponse>> $completion);
    
    @retrofit2.http.POST(value = "auth/register")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object register(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.RegisterRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.POST(value = "auth/google")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object googleLogin(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.GoogleLoginRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.kali.network.LoginResponse>> $completion);
    
    @retrofit2.http.POST(value = "alerts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createAlert(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.AlertRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.kali.network.AlertResponse>> $completion);
    
    @retrofit2.http.POST(value = "locations")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addLocation(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.LocationRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "media")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadMedia(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Part(value = "alert_id")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody alertId, @retrofit2.http.Part(value = "media_type")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody mediaType, @retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part file, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
}