package android.app;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class ActivityTaskManager {
    /**
     * Information you can retrieve about a root task in the system.
     * @hide
     */
    public static class RootTaskInfo extends TaskInfo implements Parcelable {
        // TODO(b/148895075): Move some of the fields to TaskInfo.
        public Rect bounds = new Rect();
        public int[] childTaskIds;
        public String[] childTaskNames;
        public Rect[] childTaskBounds;
        public int[] childTaskUserIds;
        public boolean visible;
        // Index of the stack in the display's stack list, can be used for comparison of stack order
        public int position;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }

        public static final Parcelable.Creator<RootTaskInfo> CREATOR = null;

        public RootTaskInfo() {
        }

        private RootTaskInfo(Parcel source) {

        }
    }
}
