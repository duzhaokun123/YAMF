package android.content.pm;

import android.content.pm.ParceledListSlice;

interface IPackageManager {
    ParceledListSlice getInstalledPackages(long flags, in int userId);
    int getPackageUid(String packageName, long flags, int userId);
    PackageInfo getPackageInfo(String packageName, long flags, int userId);
}