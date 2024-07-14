package android.window;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskSnapshot implements Parcelable {
    protected TaskSnapshot(Parcel in) {}

    @Override
    public void writeToParcel(Parcel dest, int flags) {}

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TaskSnapshot> CREATOR = new Creator<TaskSnapshot>() {
        @Override
        public TaskSnapshot createFromParcel(Parcel in) {
            return new TaskSnapshot(in);
        }

        @Override
        public TaskSnapshot[] newArray(int size) {
            return new TaskSnapshot[size];
        }
    };
}
