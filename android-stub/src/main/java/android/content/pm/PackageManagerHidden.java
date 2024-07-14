package android.content.pm;

import android.content.Intent;

import java.util.List;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(PackageManager.class)
public abstract class PackageManagerHidden {
    public abstract List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId);

    public abstract PackageInfo getPackageInfoAsUser(String packageName,
            int flags, int userId) throws PackageManager.NameNotFoundException;

    public abstract ApplicationInfo getApplicationInfoAsUser(String packageName,
            int flags, int userId) throws PackageManager.NameNotFoundException;
}
