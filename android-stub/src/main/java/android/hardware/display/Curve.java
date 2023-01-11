package android.hardware.display;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * see <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/hardware/display/Curve.java">source</a>
 */
public class Curve implements Parcelable {
    protected Curve(Parcel in) {
    }

    public static final Creator<Curve> CREATOR = new Creator<Curve>() {
        @Override
        public Curve createFromParcel(Parcel in) {
            return new Curve(in);
        }

        @Override
        public Curve[] newArray(int size) {
            return new Curve[size];
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
