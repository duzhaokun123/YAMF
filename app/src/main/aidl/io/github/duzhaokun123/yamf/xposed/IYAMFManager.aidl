package io.github.duzhaokun123.yamf.xposed;

import android.view.Surface;
import io.github.duzhaokun123.yamf.xposed.IOpenCountListener;

interface IYAMFManager {
    String getVersionName();

    int getVersionCode();

    int getUid();

    int createWindow(boolean appList);

    long getBuildTime();

    String getConfigJson();

    void updateConfig(String newConfig);

    void registerOpenCountListener(IOpenCountListener iOpenCountListener);

    void unregisterOpenCountListener(IOpenCountListener iOpenCountListener);

    void openAppList();
}