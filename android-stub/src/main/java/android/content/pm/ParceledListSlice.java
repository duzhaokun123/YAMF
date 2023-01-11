package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * see <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/content/pm/ParceledListSlice.java">source</a>
 */
public class ParceledListSlice implements Parcelable {
    protected ParceledListSlice(Parcel in) {
    }

    public static final Creator<ParceledListSlice> CREATOR = new Creator<ParceledListSlice>() {
        @Override
        public ParceledListSlice createFromParcel(Parcel in) {
            return new ParceledListSlice(in);
        }

        @Override
        public ParceledListSlice[] newArray(int size) {
            return new ParceledListSlice[size];
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
