package io.github.duzhaokun123.yamf.xposed;

import android.view.Surface;

interface IYAMFManager {
    String getVersionName();

    int getVersionCode();

    int getUid();

    /*
    * taskId: 0 for no task to move
    */
    int createWindow(int densityDpi, int flags, int taskId);

    long getBuildTime();

    String getConfigJson();

    void updateConfig(String newConfig);
}