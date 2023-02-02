package android.hardware.display;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * see <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/hardware/display/BrightnessInfo.java">source</a>
 */
public class BrightnessInfo implements Parcelable {
    protected BrightnessInfo(Parcel in) {
    }

    public static final Creator<BrightnessInfo> CREATOR = new Creator<BrightnessInfo>() {
        @Override
        public BrightnessInfo createFromParcel(Parcel in) {
            return new BrightnessInfo(in);
        }

        @Override
        public BrightnessInfo[] newArray(int size) {
            return new BrightnessInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
