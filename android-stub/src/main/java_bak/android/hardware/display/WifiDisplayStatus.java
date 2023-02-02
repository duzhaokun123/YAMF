package android.hardware.display;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * see <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/hardware/display/WifiDisplayStatus.java">source</a>
 */
public class WifiDisplayStatus implements Parcelable {
    protected WifiDisplayStatus(Parcel in) {
    }

    public static final Creator<WifiDisplayStatus> CREATOR = new Creator<WifiDisplayStatus>() {
        @Override
        public WifiDisplayStatus createFromParcel(Parcel in) {
            return new WifiDisplayStatus(in);
        }

        @Override
        public WifiDisplayStatus[] newArray(int size) {
            return new WifiDisplayStatus[size];
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
