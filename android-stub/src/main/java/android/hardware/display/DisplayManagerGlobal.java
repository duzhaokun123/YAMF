package android.hardware.display;

import java.util.concurrent.Executor;

/**
 * see <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/hardware/display/DisplayManagerGlobal.java">source</a>
 */
public class DisplayManagerGlobal {
    public static final class VirtualDisplayCallback extends IVirtualDisplayCallback.Stub {
        public VirtualDisplayCallback(VirtualDisplay.Callback callback, Executor executor) {

        }
    }
}
