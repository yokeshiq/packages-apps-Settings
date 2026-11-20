package com.android.settings.island;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceScreen;

import com.android.settings.SettingsPreferenceFragment;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IslandNotificationAppsFragment extends SettingsPreferenceFragment {

    public static final String G_NOTIF_APPS = "aura_island_notif_apps";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requireActivity().setTitle("选择灵动岛推送应用");
        final Context context = getPrefContext();
        final PackageManager pm = context.getPackageManager();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(screen);

        final Set<String> selected = loadSelected(context);
        final List<ApplicationInfo> userApps = new ArrayList<>();
        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                    && !context.getPackageName().equals(app.packageName)
                    && !"com.aura.island".equals(app.packageName)) {
                userApps.add(app);
            }
        }

        final Collator collator = Collator.getInstance();
        Collections.sort(userApps, (a, b) -> {
            boolean sa = selected.contains(a.packageName);
            boolean sb = selected.contains(b.packageName);
            if (sa != sb) return sa ? -1 : 1;
            return collator.compare(pm.getApplicationLabel(a).toString(),
                    pm.getApplicationLabel(b).toString());
        });

        for (ApplicationInfo app : userApps) {
            final String pkg = app.packageName;
            final CheckBoxPreference pref = new CheckBoxPreference(context);
            pref.setKey(pkg);
            pref.setTitle(pm.getApplicationLabel(app));
            pref.setSummary(pkg);
            pref.setIcon(pm.getApplicationIcon(app));
            pref.setPersistent(false);
            pref.setChecked(selected.contains(pkg));
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                final Set<String> current = loadSelected(context);
                if (Boolean.TRUE.equals(newValue)) {
                    current.add(pkg);
                } else {
                    current.remove(pkg);
                }
                Settings.Global.putString(context.getContentResolver(),
                        G_NOTIF_APPS, TextUtils.join(",", current));
                return true;
            });
            screen.addPreference(pref);
        }
    }

    public static Set<String> loadSelected(Context context) {
        final Set<String> set = new HashSet<>();
        final String raw = Settings.Global.getString(
                context.getContentResolver(), G_NOTIF_APPS);
        if (!TextUtils.isEmpty(raw)) {
            for (String s : raw.split(",")) {
                final String t = s.trim();
                if (!t.isEmpty()) set.add(t);
            }
        }
        return set;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }
}
