/*
 * Copyright (C) 2012 The Android Open Source Project
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

package android.view;


import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.hardware.display.DeviceProductInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.proto.ProtoOutputStream;

public final class DisplayInfo implements Parcelable {
    public int layerStack;
    public int flags;
    public int type;
    public int displayId;
    public int displayGroupId;
    public DisplayAddress address;
    public DeviceProductInfo deviceProductInfo;
    public String name;
    public String uniqueId;
    public int appWidth;
    public int appHeight;
    public int smallestNominalAppWidth;
    public int smallestNominalAppHeight;
    public int largestNominalAppWidth;
    public int largestNominalAppHeight;
    public int logicalWidth;
    public int logicalHeight;
    public DisplayCutout displayCutout;
    public int rotation;
    public int modeId;
    public int defaultModeId;
    public Display.Mode[] supportedModes;
    public int colorMode;
    public int[] supportedColorModes;
    public Display.HdrCapabilities hdrCapabilities;
    public int[] userDisabledHdrTypes = {};
    public boolean minimalPostProcessingSupported;
    public int logicalDensityDpi;
    public float physicalXDpi;
    public float physicalYDpi;
    public long appVsyncOffsetNanos;
    public long presentationDeadlineNanos;
    public int state;
    public int ownerUid;
    public String ownerPackageName;
    public float refreshRateOverride;
    public int removeMode;
    public float brightnessMinimum;
    public float brightnessMaximum;
    public float brightnessDefault;
    public RoundedCorners roundedCorners;
    public int installOrientation;
    public static final Creator<DisplayInfo> CREATOR = null;

    public DisplayInfo() {
        throw new RuntimeException("Stub!");
    }

    public DisplayInfo(DisplayInfo other) {
        throw new RuntimeException("Stub!");
    }

    private DisplayInfo(Parcel source) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public boolean equals(Object o) {
        throw new RuntimeException("Stub!");
    }

    public boolean equals(DisplayInfo other) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    public void copyFrom(DisplayInfo other) {
        throw new RuntimeException("Stub!");
    }

    public void readFromParcel(Parcel source) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    public float getRefreshRate() {
        throw new RuntimeException("Stub!");
    }

    public Display.Mode getMode() {
        throw new RuntimeException("Stub!");
    }

    public Display.Mode getDefaultMode() {
        throw new RuntimeException("Stub!");
    }

    private Display.Mode findMode(int id) {
        throw new RuntimeException("Stub!");
    }

    public Display.Mode findDefaultModeByRefreshRate(float refreshRate) {
        throw new RuntimeException("Stub!");
    }

    public float[] getDefaultRefreshRates() {
        throw new RuntimeException("Stub!");
    }

    public void getAppMetrics(DisplayMetrics outMetrics) {
        throw new RuntimeException("Stub!");
    }

    public void getAppMetrics(DisplayMetrics outMetrics, DisplayAdjustments displayAdjustments) {
        throw new RuntimeException("Stub!");
    }

    public void getAppMetrics(DisplayMetrics outMetrics, CompatibilityInfo ci,
                              Configuration configuration) {
        throw new RuntimeException("Stub!");
    }

    public void getLogicalMetrics(DisplayMetrics outMetrics, CompatibilityInfo compatInfo,
                                  Configuration configuration) {
        throw new RuntimeException("Stub!");
    }

    public void getMaxBoundsMetrics(DisplayMetrics outMetrics, CompatibilityInfo compatInfo,
                                    Configuration configuration) {
        throw new RuntimeException("Stub!");
    }

    public int getNaturalWidth() {
        throw new RuntimeException("Stub!");
    }

    public int getNaturalHeight() {
        throw new RuntimeException("Stub!");
    }

    public boolean isHdr() {
        throw new RuntimeException("Stub!");
    }

    public boolean isWideColorGamut() {
        throw new RuntimeException("Stub!");
    }

    public boolean hasAccess(int uid) {
        throw new RuntimeException("Stub!");
    }

    private void getMetricsWithSize(DisplayMetrics outMetrics, CompatibilityInfo compatInfo,
                                    Configuration configuration, int width, int height) {
        throw new RuntimeException("Stub!");
    }

    public void dumpDebug(ProtoOutputStream protoOutputStream, long fieldId) {
        throw new RuntimeException("Stub!");
    }

    private static String flagsToString(int flags) {
        throw new RuntimeException("Stub!");
    }
}