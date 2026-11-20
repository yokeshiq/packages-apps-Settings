package com.android.settings.island;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class IslandFragment extends SettingsPreferenceFragment {

    private static final String KEY_ON = "island_switch_on";
    private static final String KEY_CHARGE = "island_switch_charge";
    private static final String KEY_MEDIA = "island_switch_media";
    private static final String KEY_SMS = "island_switch_sms";
    private static final String KEY_NOTIF = "island_switch_notif";
    private static final String KEY_NOTIF_APPS = "island_notif_apps";

    private static final String G_ON = "aura_island_on";
    private static final String G_CHARGE = "aura_island_charge";
    private static final String G_MEDIA = "aura_island_media";
    private static final String G_SMS = "aura_island_sms";
    private static final String G_NOTIF = "aura_island_notif";

    private SwitchPreference mOn;
    private SwitchPreference mCharge;
    private SwitchPreference mMedia;
    private SwitchPreference mSms;
    private SwitchPreference mNotif;
    private Preference mNotifApps;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.island_settings);
        final Context context = getPrefContext();

        mOn = (SwitchPreference) findPreference(KEY_ON);
        mCharge = (SwitchPreference) findPreference(KEY_CHARGE);
        mMedia = (SwitchPreference) findPreference(KEY_MEDIA);
        mSms = (SwitchPreference) findPreference(KEY_SMS);
        mNotif = (SwitchPreference) findPreference(KEY_NOTIF);
        mNotifApps = findPreference(KEY_NOTIF_APPS);

        mOn.setChecked(isOn(context, G_ON));
        mCharge.setChecked(isOn(context, G_CHARGE));
        mMedia.setChecked(isOn(context, G_MEDIA));
        mSms.setChecked(isOn(context, G_SMS));
        mNotif.setChecked(isOn(context, G_NOTIF));

        mOn.setOnPreferenceChangeListener((preference, newValue) -> {
            final boolean on = Boolean.TRUE.equals(newValue);
            putOn(context, G_ON, on);
            setServiceEnabled(context, on);
            updateSubEnabled(on);
            return true;
        });
        mCharge.setOnPreferenceChangeListener((preference, newValue) -> {
            putOn(context, G_CHARGE, Boolean.TRUE.equals(newValue));
            return true;
        });
        mMedia.setOnPreferenceChangeListener((preference, newValue) -> {
            putOn(context, G_MEDIA, Boolean.TRUE.equals(newValue));
            return true;
        });
        mSms.setOnPreferenceChangeListener((preference, newValue) -> {
            putOn(context, G_SMS, Boolean.TRUE.equals(newValue));
            return true;
        });
        mNotif.setOnPreferenceChangeListener((preference, newValue) -> {
            final boolean notifOn = Boolean.TRUE.equals(newValue);
            putOn(context, G_NOTIF, notifOn);
            if (mNotifApps != null) mNotifApps.setEnabled(mOn.isChecked() && notifOn);
            return true;
        });
        updateSubEnabled(mOn.isChecked());
    }

    private static void setServiceEnabled(Context context, boolean enabled) {
        final android.content.Intent intent = new android.content.Intent().setComponent(
                new android.content.ComponentName("com.aura.island",
                        "com.aura.island.IslandService"));
        if (enabled) {
            context.startForegroundServiceAsUser(intent, android.os.UserHandle.SYSTEM);
        } else {
            context.stopServiceAsUser(intent, android.os.UserHandle.SYSTEM);
            context.getSystemService(android.app.ActivityManager.class)
                    .forceStopPackageAsUser("com.aura.island", android.os.UserHandle.USER_ALL);
        }
    }

    private void updateSubEnabled(boolean on) {
        mCharge.setEnabled(on);
        mMedia.setEnabled(on);
        mSms.setEnabled(on);
        mNotif.setEnabled(on);
        if (mNotifApps != null) mNotifApps.setEnabled(on && mNotif.isChecked());
    }

    private static boolean isOn(Context c, String key) {
        return Settings.Global.getInt(c.getContentResolver(), key, 1) == 1;
    }

    private static void putOn(Context c, String key, boolean v) {
        Settings.Global.putInt(c.getContentResolver(), key, v ? 1 : 0);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }
}
