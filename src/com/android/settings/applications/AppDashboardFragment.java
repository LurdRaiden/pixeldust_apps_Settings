/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.applications;

import android.app.settings.SettingsEnums;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.util.custom.systemUtils;
import com.android.internal.util.pixeldust.PixeldustUtils;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Settings page for apps. */
@SearchIndexable
public class AppDashboardFragment extends DashboardFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "AppDashboardFragment";
    private AppsPreferenceController mAppsPreferenceController;

    private static final String QUICKSWITCH_LAUNCHER_KEY = "quickswitch_default_launcher";
    private static final String SYS_DEFAULT_LAUNCHER_KEY = "persist.sys.default_launcher";
    private ListPreference mQuickSwitchLauncherPref;

    private SharedPreferences preferences;

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AppsPreferenceController(context));
        return controllers;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final PreferenceScreen screen = getPreferenceScreen();

        mQuickSwitchLauncherPref = findPreference(QUICKSWITCH_LAUNCHER_KEY);
        initQuickSwitchLauncherPref();
        mQuickSwitchLauncherPref.setOnPreferenceChangeListener(this);
    }

    private void initQuickSwitchLauncherPref() {
        preferences = getContext().getSharedPreferences("system_property_store_"
                + String.valueOf(mQuickSwitchLauncherPref.getKey()), Context.MODE_PRIVATE);
        String savedValue = preferences.getString(mQuickSwitchLauncherPref.getKey(), null);
        if (savedValue != null) {
            mQuickSwitchLauncherPref.setValue(savedValue);
            mQuickSwitchLauncherPref.setSummary("%s");
        } else {
            mQuickSwitchLauncherPref.setSummary(R.string.quickswitch_launcher_summary);
        }
    }

   @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getContext().getContentResolver();
        if (preference == mQuickSwitchLauncherPref) {
            int value = Integer.parseInt((String) newValue);
            String[] launcherPackages = getContext().getResources().
                    getStringArray(com.android.internal.R.array.config_launcherPackages);
            final String defaultHomePackage = launcherPackages[value];
            if (PixeldustUtils.isPackageInstalled(getContext(), defaultHomePackage)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(mQuickSwitchLauncherPref.getKey(), defaultHomePackage);
                editor.apply();
                SystemProperties.set(SYS_DEFAULT_LAUNCHER_KEY, defaultHomePackage);
                systemUtils.showSystemRestartDialog(getContext());
                return true;
            } else {
                Toast.makeText(getActivity(),
                        (R.string.quickswitch_launcher_toast),
                        Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.MANAGE_APPLICATIONS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_apps_and_notifications;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.apps;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(SpecialAppAccessPreferenceController.class).setSession(getSettingsLifecycle());
        mAppsPreferenceController = use(AppsPreferenceController.class);
        mAppsPreferenceController.setFragment(this /* fragment */);
        getSettingsLifecycle().addObserver(mAppsPreferenceController);

        final HibernatedAppsPreferenceController hibernatedAppsPreferenceController =
                use(HibernatedAppsPreferenceController.class);
        getSettingsLifecycle().addObserver(hibernatedAppsPreferenceController);
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.apps;
                    return Arrays.asList(sir);
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context);
                }
            };
}
