package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/content/pm/UserInfo.java">see</a>
 */
public class UserInfo implements Parcelable {
    public int id;
    public String name;

    public String toFullString() {
        return null;
    }

    public boolean isPrimary() {
        return false;
    }

    public boolean isProfile() {
        return false;
    }

    protected UserInfo(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserInfo> CREATOR = null;
}