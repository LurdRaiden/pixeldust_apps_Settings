/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.settings.gestures;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;

import com.pixeldust.settings.gestures.powermenu.GlobalPowerMenuPreferenceController;

import java.util.ArrayList;
import java.util.List;

public class PowerMenuPreferenceController extends BasePreferenceController {
    private List<AbstractPreferenceController> mGestureControllers;

    private static final String FAKE_PREF_KEY = "fake_key_only_for_get_available";

    public PowerMenuPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public CharSequence getSummary() {
        if (PowerMenuSettingsUtils.isLongPressPowerForAssistantEnabled(mContext)) {
            return mContext.getText(R.string.power_menu_summary_long_press_for_assistant);
        } else {
            return mContext.getText(R.string.power_menu_summary_long_press_for_power_menu);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        if (mGestureControllers == null) {
            mGestureControllers = buildAllPreferenceControllers(mContext);
        }
        boolean isAvailable = false;
        for (AbstractPreferenceController controller : mGestureControllers) {
            isAvailable = isAvailable || controller.isAvailable();
        }
        isAvailable = isAvailable || PowerMenuSettingsUtils.isLongPressPowerSettingAvailable(mContext);
        return isAvailable
                ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    /**
     * Get all controllers for their availability status when doing getAvailabilityStatus.
     * Do not use this method to add controllers into fragment, most of below controllers already
     * convert to TogglePreferenceController, please register them in xml.
     * The key is fake because those controllers won't be use to control preference.
     */
    private static List<AbstractPreferenceController> buildAllPreferenceControllers(
            @NonNull Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        controllers.add(new GlobalPowerMenuPreferenceController(context, FAKE_PREF_KEY));
        return controllers;
    }
}
