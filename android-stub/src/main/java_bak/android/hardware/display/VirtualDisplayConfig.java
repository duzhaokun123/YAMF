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

package android.hardware.display;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.Surface;

/**
 * see <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/hardware/display/VirtualDisplayConfig.java">source</a>
 */
public final class VirtualDisplayConfig implements Parcelable {
    private String mName;
    private int mWidth;
    private int mHeight;
    private int mDensityDpi;
    private int mFlags;
    private Surface mSurface;
    private String mUniqueId;
    private int mDisplayIdToMirror;
    private boolean mWindowManagerMirroring;

    public String getName() {
        return mName;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getDensityDpi() {
        return mDensityDpi;
    }

    public int getFlags() {
        return mFlags;
    }

    public Surface getSurface() {
        return mSurface;
    }

    public String getUniqueId() {
        return mUniqueId;
    }

    public int getDisplayIdToMirror() {
        return mDisplayIdToMirror;
    }

    public boolean isWindowManagerMirroring() {
        return mWindowManagerMirroring;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public int describeContents() { return 0; }

    public static final Parcelable.Creator<VirtualDisplayConfig> CREATOR = null;

    public static final class Builder {
        private String mName;
        private int mWidth;
        private int mHeight;
        private int mDensityDpi;
        private int mFlags;
        private Surface mSurface;
        private String mUniqueId;
        private int mDisplayIdToMirror;
        private boolean mWindowManagerMirroring;
        private long mBuilderFieldsSet = 0L;
        public Builder(
                String name,
                int width,
                int height,
                int densityDpi) {
            throw new RuntimeException("Stub!");
        }

        public  Builder setName(String value) {
            throw new RuntimeException("Stub!");
        }

        public Builder setWidth(int value) {
            throw new RuntimeException("Stub!");
        }

        public Builder setHeight(int value) {
            throw new RuntimeException("Stub!");
        }

        public Builder setDensityDpi(int value) {
            throw new RuntimeException("Stub!");
        }

        public Builder setFlags(int value) {
            throw new RuntimeException("Stub!");
        }

        public Builder setSurface(Surface value) {
            throw new RuntimeException("Stub!");
        }

        public Builder setUniqueId(String value) {
            throw new RuntimeException("Stub!");
        }

        public Builder setDisplayIdToMirror(int value) {
            throw new RuntimeException("Stub!");
        }

        public Builder setWindowManagerMirroring(boolean value) {
            throw new RuntimeException("Stub!");
        }

        public VirtualDisplayConfig build() {
            throw new RuntimeException("Stub!");
        }

        private void checkNotUsed() {
            throw new RuntimeException("Stub!");
        }
    }

    private void __metadata() {}
}