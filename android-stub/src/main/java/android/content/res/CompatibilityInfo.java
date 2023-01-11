/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.content.res;

import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.InsetsSourceControl;
import android.view.InsetsState;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class CompatibilityInfo implements Parcelable {

    public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = null;
    public static final int DEFAULT_NORMAL_SHORT_DIMENSION = 0;
    public static final float MAXIMUM_ASPECT_RATIO = 0;
    private final int mCompatibilityFlags = 0;
    private static final int SCALING_REQUIRED = 0;
    private static final int ALWAYS_NEEDS_COMPAT = 0;
    private static final int NEVER_NEEDS_COMPAT = 0;
    private static final int NEEDS_SCREEN_COMPAT = 0;
    private static final int NEEDS_COMPAT_RES = 0;
    private static final int HAS_OVERRIDE_SCALING = 0;
    public final int applicationDensity = 0;
    public final float applicationScale = 0;
    public final float applicationInvertedScale = 0;

    @Deprecated
    public CompatibilityInfo(ApplicationInfo appInfo, int screenLayout, int sw,
                             boolean forceCompat) {
        throw new RuntimeException("Stub!");
    }

    public CompatibilityInfo(ApplicationInfo appInfo, int screenLayout, int sw,
                             boolean forceCompat, float overrideScale) {
        throw new RuntimeException("Stub!");
    }

    private CompatibilityInfo(int compFlags,
                              int dens, float scale, float invertedScale) {
        throw new RuntimeException("Stub!");
    }

    private CompatibilityInfo() {
        throw new RuntimeException("Stub!");
    }

    public boolean isScalingRequired() {
        throw new RuntimeException("Stub!");
    }

    public boolean supportsScreen() {
        throw new RuntimeException("Stub!");
    }

    public boolean neverSupportsScreen() {
        throw new RuntimeException("Stub!");
    }

    public boolean alwaysSupportsScreen() {
        throw new RuntimeException("Stub!");
    }

    public boolean needsCompatResources() {
        throw new RuntimeException("Stub!");
    }

    public Translator getTranslator() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }

    public class Translator {
        final public float applicationScale;
        final public float applicationInvertedScale;
        private Rect mContentInsetsBuffer = null;
        private Rect mVisibleInsetsBuffer = null;
        private Region mTouchableAreaBuffer = null;

        Translator(float applicationScale, float applicationInvertedScale) {
            throw new RuntimeException("Stub!");
        }

        Translator() {
            throw new RuntimeException("Stub!");
        }

        public void translateRegionInWindowToScreen(Region transparentRegion) {
            throw new RuntimeException("Stub!");
        }

        public void translateCanvas(Canvas canvas) {
            throw new RuntimeException("Stub!");
        }

        public void translateEventInScreenToAppWindow(MotionEvent event) {
            throw new RuntimeException("Stub!");
        }

        public void translateWindowLayout(WindowManager.LayoutParams params) {
            throw new RuntimeException("Stub!");
        }

        public float translateLengthInAppWindowToScreen(float length) {
            throw new RuntimeException("Stub!");
        }

        public void translateRectInAppWindowToScreen(Rect rect) {
            throw new RuntimeException("Stub!");
        }

        public void translateRectInScreenToAppWindow(Rect rect) {
            throw new RuntimeException("Stub!");
        }

        public void translateInsetsStateInScreenToAppWindow(InsetsState state) {
            throw new RuntimeException("Stub!");
        }

        public void translateSourceControlsInScreenToAppWindow(InsetsSourceControl[] controls) {
            throw new RuntimeException("Stub!");
        }

        public void translatePointInScreenToAppWindow(PointF point) {
            throw new RuntimeException("Stub!");
        }

        public void translateLayoutParamsInAppWindowToScreen(LayoutParams params) {
            throw new RuntimeException("Stub!");
        }

        public Rect getTranslatedContentInsets(Rect contentInsets) {
            throw new RuntimeException("Stub!");
        }

        public Rect getTranslatedVisibleInsets(Rect visibleInsets) {
            throw new RuntimeException("Stub!");
        }

        public Region getTranslatedTouchableArea(Region touchableArea) {
            throw new RuntimeException("Stub!");
        }
    }

    public void applyToDisplayMetrics(DisplayMetrics inoutDm) {
        throw new RuntimeException("Stub!");
    }

    public void applyToConfiguration(int displayDensity, Configuration inoutConfig) {
        throw new RuntimeException("Stub!");
    }

    public static float computeCompatibleScaling(DisplayMetrics dm, DisplayMetrics outDm) {
        throw new RuntimeException("Stub!");
    }

    public static final Parcelable.Creator<CompatibilityInfo> CREATOR = null;

    private CompatibilityInfo(Parcel source) {
        throw new RuntimeException("Stub!");
    }
}