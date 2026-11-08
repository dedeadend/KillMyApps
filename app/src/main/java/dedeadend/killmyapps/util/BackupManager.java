package dedeadend.killmyapps.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.provider.OpenableColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.data.excludedPkgDao;
import dedeadend.killmyapps.model.PKGName;

public class BackupManager {

    public static final String MIME_TYPE = "application/json";
    public static final String FORMAT_NAME = "killmyapps-backup";
    public static final int FORMAT_VERSION = 1;
    private static final int MAX_BACKUP_BYTES = 2 * 1024 * 1024;
    private static final int MAX_PACKAGE_COUNT = 10000;

    public static void writeBackup(Context context, Uri destination)
            throws IOException, BackupException {
        requireBackgroundThread();
        if (context == null || destination == null)
            throw new IllegalArgumentException("Context and destination Uri are required.");

        final String json;
        try {
            json = createBackupJson(context).toString(2);
        } catch (JSONException e) {
            throw new BackupException("Could not create the backup JSON.", e);
        }

        if (json.getBytes(StandardCharsets.UTF_8).length > MAX_BACKUP_BYTES)
            throw new BackupException("The backup is too large.");

        OutputStream outputStream = context.getContentResolver().openOutputStream(destination, "w");
        if (outputStream == null)
            throw new IOException("Android could not open the selected destination file.");

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.write(json);
        }
    }

    public static BackupData readBackup(Context context, Uri source)
            throws IOException, BackupException {
        requireBackgroundThread();
        if (context == null || source == null)
            throw new IllegalArgumentException("Context and source Uri are required.");

        final String jsonText;
        try (InputStream stream = context.getContentResolver().openInputStream(source)) {
            if (stream == null)
                throw new IOException("Android could not open the selected backup file.");
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                int total = 0;

                while ((read = stream.read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_BACKUP_BYTES)
                        throw new BackupException("The selected backup is too large.");
                    output.write(buffer, 0, read);
                }
                jsonText = output.toString(StandardCharsets.UTF_8.name());
            }
        }

        final JSONObject root;
        try {
            root = new JSONObject(jsonText);
        } catch (JSONException e) {
            throw new BackupException("The selected file is not valid JSON.", e);
        }

        return parseAndValidate(context, source, root);
    }

    public static void restoreBackup(Context context, BackupData backup) throws BackupException {
        requireBackgroundThread();
        if (backup == null)
            throw new IllegalArgumentException("Backup data is required.");
        if (App.database == null || App.settings == null)
            throw new BackupException("The app database or settings are not initialized.");

        excludedPkgDao dao = App.database.excludedPkgDao();
        List<PKGName> previousPackages = dao.getAll();
        Map<String, ?> previousSettings = App.settings.getAll();
        List<PKGName> newPackages = new ArrayList<>(backup.packages.size());
        for (String packageName : backup.packages) {
            PKGName item = new PKGName();
            item.name = packageName;
            newPackages.add(item);
        }

        try {
            replacePackages(previousPackages, newPackages);
        } catch (RuntimeException e) {
            throw new BackupException("Could not restore the app selection. Existing data was kept.", e);
        }

        List<String> updatedKeys = new ArrayList<>(backup.settings.keySet());
        updatedKeys.add(App.SELECTION_MODE);

        try {
            SharedPreferences.Editor editor = App.settings.edit();
            for (Map.Entry<String, Object> entry : backup.settings.entrySet())
                putPreferenceValue(editor, entry.getKey(), entry.getValue());
            editor.putInt(App.SELECTION_MODE, backup.selectionMode);

            if (!editor.commit())
                throw rollbackRestore(newPackages, previousPackages, previousSettings, updatedKeys, null);
        } catch (BackupException e) {
            throw e;
        } catch (RuntimeException e) {
            throw rollbackRestore(newPackages, previousPackages, previousSettings, updatedKeys, e);
        }

        App.handler.post(() -> {
            App.setAppThemeMode();
            App.initAutoKillServices();
        });
    }

    private static void replacePackages(List<PKGName> currentPackages, List<PKGName> newPackages) {
        App.database.runInTransaction(() -> {
            excludedPkgDao dao = App.database.excludedPkgDao();
            for (PKGName oldPackage : currentPackages)
                dao.delete(oldPackage);
            for (PKGName item : newPackages)
                dao.insert(item);
        });
    }

    private static BackupException rollbackRestore(List<PKGName> appliedPackages,
                                                   List<PKGName> previousPackages,
                                                   Map<String, ?> previousSettings,
                                                   List<String> updatedKeys,
                                                   RuntimeException cause) {
        boolean settingsRestored = restorePreviousSettings(previousSettings, updatedKeys);
        boolean packagesRestored = restorePackages(appliedPackages, previousPackages);

        String message = settingsRestored && packagesRestored
                ? "Could not save backup settings. Previous data was restored."
                : "Could not save backup settings, and rollback was incomplete. Please check your settings and selection.";

        return cause == null
                ? new BackupException(message)
                : new BackupException(message, cause);
    }

    private static boolean restorePreviousSettings(Map<String, ?> previousSettings,
                                                   List<String> keys) {
        try {
            SharedPreferences.Editor editor = App.settings.edit();
            for (String key : keys) {
                Object value = previousSettings.get(key);
                if (value == null)
                    editor.remove(key);
                else
                    putPreferenceValue(editor, key, value);
            }
            return editor.commit();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean restorePackages(List<PKGName> appliedPackages,
                                           List<PKGName> previousPackages) {
        try {
            replacePackages(appliedPackages, previousPackages);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static String createDefaultFileName() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        return "KillMyApps-Backup-" + date + ".json";
    }

    public static String formatBackupDate(String createdAt) {
        Date date = parseUtcTimestamp(createdAt);
        if (date == null)
            return createdAt == null || createdAt.trim().isEmpty() ? "Unknown" : createdAt;
        return new SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.getDefault()).format(date);
    }

    private static Date parseUtcTimestamp(String value) {
        if (value == null || value.isEmpty())
            return null;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        format.setLenient(false);

        ParsePosition position = new ParsePosition(0);
        Date date = format.parse(value, position);
        return date != null && position.getIndex() == value.length() ? date : null;
    }

    private static JSONObject createBackupJson(Context context)
            throws JSONException, BackupException {
        JSONObject root = new JSONObject();
        root.put("format", FORMAT_NAME);
        root.put("version", FORMAT_VERSION);
        root.put("createdAt", createUtcTimestamp());
        root.put("appVersion", getAppVersionName(context));

        JSONObject selection = new JSONObject();
        selection.put("mode", App.settings.getInt(App.SELECTION_MODE, 0));

        JSONArray packages = new JSONArray();
        List<PKGName> selectedPackages = App.database.excludedPkgDao().getAll();
        for (PKGName item : selectedPackages) {
            if (packages.length() >= MAX_PACKAGE_COUNT)
                throw new BackupException("The selection contains too many packages to back up.");
            packages.put(item.name);
        }

        selection.put("packages", packages);
        root.put("selection", selection);

        JSONObject settings = new JSONObject();
        SharedPreferences prefs = App.settings;

        settings.put("themeMode", prefs.getInt(App.THEME_MODE, 0));
        settings.put("killerMode", prefs.getInt(App.KILLER_MODE, 1));
        settings.put("killLevel", prefs.getInt(App.KILLER_LEVEL, 1));
        settings.put("listMode", prefs.getInt(App.LIST_MODE, 1));

        settings.put("hideKillMyApps", prefs.getBoolean(App.HIDE_KILL_MY_APPS, true));
        settings.put("hideDefaultLauncher", prefs.getBoolean(App.HIDE_DEFAULT_LAUNCHER, true));
        settings.put("hideDefaultAlarm", prefs.getBoolean(App.HIDE_DEFAULT_ALARM, true));
        settings.put("hideDefaultKeyboard", prefs.getBoolean(App.HIDE_DEFAULT_KEYBOARD, true));
        settings.put("hideDefaultDialer", prefs.getBoolean(App.HIDE_DEFAULT_DIALER, true));
        settings.put("hideDefaultSMS", prefs.getBoolean(App.HIDE_DEFAULT_SMS, true));
        settings.put("hideCriticalSystemApps", prefs.getBoolean(App.HIDE_CRITICAL_SYSTEM_APPS, true));

        settings.put("showPkgName", prefs.getBoolean(App.SHOW_PKG_NAME, true));
        settings.put("clickToAppInfo", prefs.getBoolean(App.CLICK_TO_APP_INFO, true));
        settings.put("longClickToMenu", prefs.getBoolean(App.LONG_CLICK_TO_MENU, true));
        settings.put("showScrollAnimation", prefs.getBoolean(App.SHOW_SCROLL_ANIMATION, true));

        settings.put("screenOffAutoKill", prefs.getBoolean(App.SCREEN_OFF_AUTO_KILL, false));
        settings.put("screenOffAutoKillDelay", prefs.getInt(App.SCREEN_OFF_AUTO_KILL_DELAY, 0));
        settings.put("scheduledAutoKill", prefs.getBoolean(App.SCHEDULED_AUTO_KILL, false));
        settings.put("scheduledAutoKillHour", prefs.getInt(App.SCHEDULED_AUTO_KILL_HOUR, 3));
        settings.put("scheduledAutoKillMinute", prefs.getInt(App.SCHEDULED_AUTO_KILL_MINUTE, 30));
        root.put("settings", settings);

        return root;
    }

    private static BackupData parseAndValidate(Context context, Uri source, JSONObject root)
            throws BackupException {
        try {
            if (!FORMAT_NAME.equals(root.optString("format")))
                throw new BackupException("This file is not a KillMyApps backup.");

            int version = readInteger(root, "version", 1, Integer.MAX_VALUE);
            if (version != FORMAT_VERSION)
                throw new BackupException("This backup format version is not supported.");

            String createdAt = readRequiredString(root, "createdAt");
            if (parseUtcTimestamp(createdAt) == null)
                throw new BackupException("The backup contains an invalid creation date.");
            String appVersion = root.optString("appVersion", "Unknown");

            JSONObject selection = root.getJSONObject("selection");
            int selectionMode = readInteger(selection, "mode", 0, 1);
            JSONArray packageArray = selection.getJSONArray("packages");
            if (packageArray.length() > MAX_PACKAGE_COUNT)
                throw new BackupException("The backup contains too many selected packages.");

            LinkedHashSet<String> uniquePackages = new LinkedHashSet<>();
            for (int i = 0; i < packageArray.length(); i++) {
                Object value = packageArray.get(i);
                if (!(value instanceof String))
                    throw new BackupException("The backup contains an invalid package entry.");

                String packageName = ((String) value).trim();
                if (!isValidPackageName(packageName))
                    throw new BackupException("The backup contains an invalid package name.");
                uniquePackages.add(packageName);
            }

            JSONObject settingsJson = root.getJSONObject("settings");
            Map<String, Object> settings = new LinkedHashMap<>();

            addIntegerSetting(settings, settingsJson, "themeMode", App.THEME_MODE, 0, 2);
            addIntegerSetting(settings, settingsJson, "killerMode", App.KILLER_MODE, 0, 3);
            addIntegerSetting(settings, settingsJson, "killLevel", App.KILLER_LEVEL, 0, 2);
            addIntegerSetting(settings, settingsJson, "listMode", App.LIST_MODE, 0, 2);

            addBooleanSetting(settings, settingsJson, "hideKillMyApps", App.HIDE_KILL_MY_APPS);
            addBooleanSetting(settings, settingsJson, "hideDefaultLauncher", App.HIDE_DEFAULT_LAUNCHER);
            addBooleanSetting(settings, settingsJson, "hideDefaultAlarm", App.HIDE_DEFAULT_ALARM);
            addBooleanSetting(settings, settingsJson, "hideDefaultKeyboard", App.HIDE_DEFAULT_KEYBOARD);
            addBooleanSetting(settings, settingsJson, "hideDefaultDialer", App.HIDE_DEFAULT_DIALER);
            addBooleanSetting(settings, settingsJson, "hideDefaultSMS", App.HIDE_DEFAULT_SMS);
            addBooleanSetting(settings, settingsJson, "hideCriticalSystemApps", App.HIDE_CRITICAL_SYSTEM_APPS);

            addBooleanSetting(settings, settingsJson, "showPkgName", App.SHOW_PKG_NAME);
            addBooleanSetting(settings, settingsJson, "clickToAppInfo", App.CLICK_TO_APP_INFO);
            addBooleanSetting(settings, settingsJson, "longClickToMenu", App.LONG_CLICK_TO_MENU);
            addBooleanSetting(settings, settingsJson, "showScrollAnimation", App.SHOW_SCROLL_ANIMATION);

            addBooleanSetting(settings, settingsJson, "screenOffAutoKill", App.SCREEN_OFF_AUTO_KILL);
            addIntegerSetting(settings, settingsJson, "screenOffAutoKillDelay", App.SCREEN_OFF_AUTO_KILL_DELAY, 0, 15);
            addBooleanSetting(settings, settingsJson, "scheduledAutoKill", App.SCHEDULED_AUTO_KILL);
            addIntegerSetting(settings, settingsJson, "scheduledAutoKillHour", App.SCHEDULED_AUTO_KILL_HOUR, 0, 23);
            addIntegerSetting(settings, settingsJson, "scheduledAutoKillMinute", App.SCHEDULED_AUTO_KILL_MINUTE, 0, 30);

            int minute = (Integer) settings.get(App.SCHEDULED_AUTO_KILL_MINUTE);
            if (minute != 0 && minute != 30)
                throw new BackupException("The backup contains an invalid scheduled time.");

            return new BackupData(
                    getDisplayName(context, source),
                    createdAt,
                    appVersion,
                    selectionMode,
                    new ArrayList<>(uniquePackages),
                    settings
            );
        } catch (JSONException e) {
            throw new BackupException("The backup is incomplete or has an invalid structure.", e);
        }
    }

    private static void addIntegerSetting(Map<String, Object> result, JSONObject source,
                                          String jsonKey, String preferenceKey, int min, int max)
            throws JSONException, BackupException {
        result.put(preferenceKey, readInteger(source, jsonKey, min, max));
    }

    private static void addBooleanSetting(Map<String, Object> result, JSONObject source,
                                          String jsonKey, String preferenceKey)
            throws JSONException, BackupException {
        Object value = source.get(jsonKey);
        if (!(value instanceof Boolean))
            throw new BackupException("The backup contains an invalid value for " + jsonKey + ".");
        result.put(preferenceKey, value);
    }

    private static int readInteger(JSONObject object, String key, int min, int max)
            throws JSONException, BackupException {
        Object value = object.get(key);
        if (!(value instanceof Number))
            throw new BackupException("The backup contains an invalid value for " + key + ".");

        Number number = (Number) value;
        double asDouble = number.doubleValue();
        long asLong = number.longValue();
        if (asDouble != (double) asLong || asLong < min || asLong > max)
            throw new BackupException("The backup contains an out-of-range value for " + key + ".");
        return (int) asLong;
    }

    private static String readRequiredString(JSONObject object, String key)
            throws JSONException, BackupException {
        Object value = object.get(key);
        if (!(value instanceof String) || ((String) value).trim().isEmpty())
            throw new BackupException("The backup is missing " + key + ".");
        return (String) value;
    }

    private static boolean isValidPackageName(String value) {
        return !value.isEmpty() && value.length() <= 255
                && value.matches("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+");
    }

    private static void putPreferenceValue(SharedPreferences.Editor editor, String key, Object value) {
        if (value instanceof Boolean)
            editor.putBoolean(key, (Boolean) value);
        else if (value instanceof Integer)
            editor.putInt(key, (Integer) value);
        else if (value instanceof Long)
            editor.putLong(key, (Long) value);
        else if (value instanceof Float)
            editor.putFloat(key, (Float) value);
        else if (value instanceof String)
            editor.putString(key, (String) value);
        else
            throw new IllegalArgumentException("Unsupported preference value for " + key);
    }

    private static String createUtcTimestamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    private static String getAppVersionName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName == null ? "Unknown" : info.versionName;
        } catch (Exception ignored) {
            return "Unknown";
        }
    }

    private static String getDisplayName(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri,
                new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = cursor.getString(index);
                    if (name != null && !name.trim().isEmpty())
                        return name;
                }
            }
        } catch (Exception ignored) {
        }
        return "Selected backup file";
    }

    private static void requireBackgroundThread() {
        if (Looper.myLooper() == Looper.getMainLooper())
            throw new IllegalStateException("BackupManager file and database operations must run on a background thread.");
    }

    public static final class BackupData {
        private final String fileName;
        private final String createdAt;
        private final String appVersion;
        private final int selectionMode;
        private final List<String> packages;
        private final Map<String, Object> settings;

        private BackupData(String fileName, String createdAt, String appVersion,
                           int selectionMode, List<String> packages, Map<String, Object> settings) {
            this.fileName = fileName;
            this.createdAt = createdAt;
            this.appVersion = appVersion;
            this.selectionMode = selectionMode;
            this.packages = Collections.unmodifiableList(packages);
            this.settings = Collections.unmodifiableMap(settings);
        }

        public String getFileName() {
            return fileName;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getFormattedCreatedAt() {
            return formatBackupDate(createdAt);
        }

        public String getAppVersion() {
            return appVersion;
        }

        public int getSelectionMode() {
            return selectionMode;
        }

        public int getPackageCount() {
            return packages.size();
        }

        public int getSettingsCount() {
            return settings.size() + 1;
        }
    }

    public static class BackupException extends Exception {
        private static final long serialVersionUID = 1L;

        public BackupException(String message) {
            super(message);
        }

        public BackupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
