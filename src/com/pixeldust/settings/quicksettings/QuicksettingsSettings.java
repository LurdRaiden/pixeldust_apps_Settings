/*
 * Copyright (C) 2022 The PixelDust Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.pixeldust.settings.quicksettings;

import android.os.Bundle;
import android.content.Context;
import android.provider.Settings;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.os.UserHandle;
import android.content.ContentResolver;

@SearchIndexable
public class QuicksettingsSettings extends SettingsPreferenceFragment {

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.PIXELDUST;
    }

       public static void reset(Context mContext) {
       final ContentResolver resolver = mContext.getContentResolver();
       Settings.System.putIntForUser(resolver,
                Settings.System.QS_LAYOUT_COLUMNS_LANDSCAPE, 4, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_LAYOUT_COLUMNS, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_VERTICAL_LAYOUT, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_LABEL_HIDE, 0, UserHandle.USER_CURRENT);
     }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quicksettings_settings);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.quicksettings_settings);
}
