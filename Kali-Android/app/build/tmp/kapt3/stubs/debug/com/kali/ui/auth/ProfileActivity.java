package com.kali.ui.auth;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\t\u001a\u00020\nH\u0002J\u0010\u0010\u000b\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\rH\u0002J\b\u0010\u000e\u001a\u00020\nH\u0002J\u0016\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0012\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\u0011J\b\u0010\u0013\u001a\u00020\nH\u0002J\b\u0010\u0014\u001a\u00020\nH\u0002J\u0012\u0010\u0015\u001a\u00020\n2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0014J\u0016\u0010\u0018\u001a\u00020\n2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001aH\u0002J\b\u0010\u001c\u001a\u00020\nH\u0002J\b\u0010\u001d\u001a\u00020\nH\u0002J\b\u0010\u001e\u001a\u00020\nH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/kali/ui/auth/ProfileActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/kali/databinding/ActivityProfileBinding;", "session", "Lcom/kali/util/SessionManager;", "userRole", "", "addEmergencyContact", "", "deleteEmergencyContact", "contactId", "", "linkToUser", "loadEmergencyContacts", "authHeader", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadGuardianUserRelation", "loadProfileData", "logoutUser", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "populateContactsUI", "contacts", "", "Lcom/kali/network/EmergencyContact;", "setupUIForRole", "unlinkFromUser", "updateProfileDetails", "app_debug"})
public final class ProfileActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.kali.databinding.ActivityProfileBinding binding;
    private com.kali.util.SessionManager session;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String userRole = "User";
    
    public ProfileActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupUIForRole() {
    }
    
    private final void loadProfileData() {
    }
    
    private final void updateProfileDetails() {
    }
    
    private final java.lang.Object loadGuardianUserRelation(java.lang.String authHeader, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void linkToUser() {
    }
    
    private final void unlinkFromUser() {
    }
    
    private final java.lang.Object loadEmergencyContacts(java.lang.String authHeader, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void populateContactsUI(java.util.List<com.kali.network.EmergencyContact> contacts) {
    }
    
    private final void addEmergencyContact() {
    }
    
    private final void deleteEmergencyContact(int contactId) {
    }
    
    private final void logoutUser() {
    }
}