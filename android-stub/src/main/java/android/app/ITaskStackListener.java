package android.app;

import android.content.ComponentName;
import android.hardware.display.IDisplayManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.window.TaskSnapshot;

public interface ITaskStackListener extends IInterface {
    // https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/app/ITaskStackListener.aidl
    abstract class Stub extends Binder implements ITaskStackListener {
        @Override
        public IBinder asBinder() {
            throw new UnsupportedOperationException();
        }
    }
}
