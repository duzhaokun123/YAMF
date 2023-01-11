package android.hardware.display;

import android.os.Binder;

public interface IVirtualDisplayCallback {
    public IVirtualDisplayCallback asInterface(Binder binder);
    public static class Stub implements IVirtualDisplayCallback {

        @Override
        public IVirtualDisplayCallback asInterface(Binder binder) {
            return null;
        }
    }
}
