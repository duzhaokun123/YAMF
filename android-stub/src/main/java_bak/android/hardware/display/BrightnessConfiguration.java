package android.hardware.display;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * see <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/hardware/display/BrightnessConfiguration.java">source</a>
 */
public class BrightnessConfiguration  implements Parcelable {
    protected BrightnessConfiguration(Parcel in) {
    }

    public static final Creator<BrightnessConfiguration> CREATOR = new Creator<BrightnessConfiguration>() {
        @Override
        public BrightnessConfiguration createFromParcel(Parcel in) {
            return new BrightnessConfiguration(in);
        }

        @Override
        public BrightnessConfiguration[] newArray(int size) {
            return new BrightnessConfiguration[size];
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
