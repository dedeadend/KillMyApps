# 1. Keep Shizuku Library Classes
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }

# 2. Keep UserService & its constructor (Prevent Reflection issues)
-keep class dedeadend.killmyapps.service.UserService {
    public <init>();
    public *;
}

# 3. Keep AIDL Interface & Stub classes (Prevent Binder IPC Mismatch)
-keep interface dedeadend.killmyapps.IKillAppService { *; }
-keep class dedeadend.killmyapps.IKillAppService$* { *; }