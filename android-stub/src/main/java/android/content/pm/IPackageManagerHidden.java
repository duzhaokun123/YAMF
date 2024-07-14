package android.content.pm;

import android.content.ComponentName;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(IPackageManager.class)
public interface IPackageManagerHidden {
    @RequiresApi(33)
    ActivityInfo getActivityInfo(ComponentName className, long flags, int userId)
            throws RemoteException;

    ActivityInfo getActivityInfo(ComponentName className, int flags, int userId)
            throws RemoteException;
}
