package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * see <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/content/pm/ParceledListSlice.java">source</a>
 */
public class ParceledListSlice<T> implements Parcelable {

    public List<T> getList() {
        return null;
    }
    protected ParceledListSlice(Parcel in) {
    }

    public static final Creator<ParceledListSlice<?>> CREATOR = null;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
