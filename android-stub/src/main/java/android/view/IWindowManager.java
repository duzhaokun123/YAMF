package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IWindowManager extends IInterface {

    int watchRotation(IRotationWatcher watcher, int displayId) throws RemoteException;

    void removeRotationWatcher(IRotationWatcher watcher) throws RemoteException;

    abstract class Stub extends Binder implements IWindowManager {
        public static IWindowManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
