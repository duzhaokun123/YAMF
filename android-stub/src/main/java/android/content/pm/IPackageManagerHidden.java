package android.content.pm;

import android.content.ComponentName;
import android.os.RemoteException;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(IPackageManager.class)
public interface IPackageManagerHidden {
    ActivityInfo getActivityInfo(ComponentName className, long flags, int userId)
            throws RemoteException;
}
