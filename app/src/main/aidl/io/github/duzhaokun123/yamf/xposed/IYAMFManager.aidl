package io.github.duzhaokun123.yamf.xposed;

import android.view.Surface;
import io.github.duzhaokun123.yamf.xposed.IOpenCountListener;

interface IYAMFManager {
    String getVersionName();

    int getVersionCode();

    int getUid();

    void createWindow();

    long getBuildTime();

    void registerOpenCountListener(IOpenCountListener iOpenCountListener);

    void unregisterOpenCountListener(IOpenCountListener iOpenCountListener);

    void openAppList();

    void currentToWindow();

    void resetAllWindow();
}