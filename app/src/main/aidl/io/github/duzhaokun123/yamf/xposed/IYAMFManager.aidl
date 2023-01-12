package io.github.duzhaokun123.yamf.xposed;

import android.view.Surface;

interface IYAMFManager {
    String getVersionName();

    int getVersionCode();

    int getUid();

    int createWindow();

    long getBuildTime();

    String getConfigJson();

    void updateConfig(String newConfig);

    int getOpenCount();
}