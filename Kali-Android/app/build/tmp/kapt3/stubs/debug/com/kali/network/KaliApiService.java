package com.kali.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00b8\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J(\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ(\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ(\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u0011J(\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0013\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0015J$\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\u00170\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0019J$\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00170\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0019J,\u0010\u001b\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\u0006\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u001c0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0019J(\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001e0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u001f\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0015J.\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020!0\u00170\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u001f\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0015J$\u0010\"\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\u00170\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u001e\u0010#\u001a\b\u0012\u0004\u0012\u00020$0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u001e\u0010%\u001a\b\u0012\u0004\u0012\u00020&0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u001e\u0010\'\u001a\b\u0012\u0004\u0012\u00020(0\u00032\b\b\u0001\u0010\u0007\u001a\u00020)H\u00a7@\u00a2\u0006\u0002\u0010*J(\u0010+\u001a\b\u0012\u0004\u0012\u00020,0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020-H\u00a7@\u00a2\u0006\u0002\u0010.J\u001e\u0010/\u001a\b\u0012\u0004\u0012\u00020(0\u00032\b\b\u0001\u0010\u0007\u001a\u000200H\u00a7@\u00a2\u0006\u0002\u00101J\u001e\u00102\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u0007\u001a\u000203H\u00a7@\u00a2\u0006\u0002\u00104J2\u00105\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u001f\u001a\u00020\u00142\b\b\u0003\u0010\u0007\u001a\u000206H\u00a7@\u00a2\u0006\u0002\u00107J\u001e\u00108\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0019J(\u00109\u001a\b\u0012\u0004\u0012\u00020&0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020:H\u00a7@\u00a2\u0006\u0002\u0010;J<\u0010<\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u001f\u001a\u00020=2\b\b\u0001\u0010>\u001a\u00020=2\b\b\u0001\u0010?\u001a\u00020@H\u00a7@\u00a2\u0006\u0002\u0010A\u00a8\u0006B"}, d2 = {"Lcom/kali/network/KaliApiService;", "", "addContact", "Lretrofit2/Response;", "Lcom/kali/network/EmergencyContact;", "token", "", "body", "Lcom/kali/network/EmergencyContactRequest;", "(Ljava/lang/String;Lcom/kali/network/EmergencyContactRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "addLocation", "", "Lcom/kali/network/LocationRequest;", "(Ljava/lang/String;Lcom/kali/network/LocationRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createAlert", "Lcom/kali/network/AlertResponse;", "Lcom/kali/network/AlertRequest;", "(Ljava/lang/String;Lcom/kali/network/AlertRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteContact", "contactId", "", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAlerts", "", "Lcom/kali/network/Alert;", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getContacts", "getGuardianActiveAlert", "", "getLocationStats", "Lcom/kali/network/TelemetryStats;", "alertId", "getLocations", "Lcom/kali/network/HistoricalLocation;", "getMyAlerts", "getMyUser", "Lcom/kali/network/GuardianUserResponse;", "getProfile", "Lcom/kali/network/ProfileResponse;", "googleLogin", "Lcom/kali/network/LoginResponse;", "Lcom/kali/network/GoogleLoginRequest;", "(Lcom/kali/network/GoogleLoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "linkGuardian", "Lcom/kali/network/GuardianLinkResponse;", "Lcom/kali/network/GuardianLinkRequest;", "(Ljava/lang/String;Lcom/kali/network/GuardianLinkRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "login", "Lcom/kali/network/LoginRequest;", "(Lcom/kali/network/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "register", "Lcom/kali/network/RegisterRequest;", "(Lcom/kali/network/RegisterRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resolveAlert", "Lcom/kali/network/ResolveRequest;", "(Ljava/lang/String;ILcom/kali/network/ResolveRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "unlinkGuardian", "updateProfile", "Lcom/kali/network/ProfileUpdateRequest;", "(Ljava/lang/String;Lcom/kali/network/ProfileUpdateRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadMedia", "Lokhttp3/RequestBody;", "mediaType", "file", "Lokhttp3/MultipartBody$Part;", "(Ljava/lang/String;Lokhttp3/RequestBody;Lokhttp3/RequestBody;Lokhttp3/MultipartBody$Part;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
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
    
    @retrofit2.http.GET(value = "auth/profile")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getProfile(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.kali.network.ProfileResponse>> $completion);
    
    @retrofit2.http.PUT(value = "auth/profile")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProfile(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.ProfileUpdateRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.kali.network.ProfileResponse>> $completion);
    
    @retrofit2.http.POST(value = "alerts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createAlert(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.AlertRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.kali.network.AlertResponse>> $completion);
    
    @retrofit2.http.GET(value = "alerts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAlerts(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.kali.network.Alert>>> $completion);
    
    @retrofit2.http.GET(value = "alerts/my")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMyAlerts(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.kali.network.Alert>>> $completion);
    
    @retrofit2.http.POST(value = "alerts/{id}/resolve")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object resolveAlert(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Path(value = "id")
    int alertId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.ResolveRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.POST(value = "locations")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addLocation(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.LocationRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "locations/{alert_id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLocations(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Path(value = "alert_id")
    int alertId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.kali.network.HistoricalLocation>>> $completion);
    
    @retrofit2.http.GET(value = "locations/{alert_id}/stats")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLocationStats(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Path(value = "alert_id")
    int alertId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.kali.network.TelemetryStats>> $completion);
    
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
    
    @retrofit2.http.GET(value = "contacts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getContacts(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.kali.network.EmergencyContact>>> $completion);
    
    @retrofit2.http.POST(value = "contacts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addContact(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.EmergencyContactRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.kali.network.EmergencyContact>> $completion);
    
    @retrofit2.http.DELETE(value = "contacts/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteContact(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Path(value = "id")
    int contactId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "guardian/my-user")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMyUser(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.kali.network.GuardianUserResponse>> $completion);
    
    @retrofit2.http.POST(value = "guardian/link")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object linkGuardian(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.kali.network.GuardianLinkRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.kali.network.GuardianLinkResponse>> $completion);
    
    @retrofit2.http.DELETE(value = "guardian/unlink")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object unlinkGuardian(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "guardian/active-alert")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getGuardianActiveAlert(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.Map<java.lang.String, java.lang.Object>>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}