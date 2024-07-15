// IYAMFManager.aidl
package com.mja.reyamf.xposed;

import com.mja.reyamf.xposed.IOpenCountListener;
// Declare any non-default types here with import statements

interface IYAMFManager {
    String getVersionName();

    int getVersionCode();

    int getUid();

    void createWindow();

    long getBuildTime();

    String getConfigJson();

    void updateConfig(String newConfig);

    void registerOpenCountListener(IOpenCountListener iOpenCountListener);

    void unregisterOpenCountListener(IOpenCountListener iOpenCountListener);

    void openAppList();

    void currentToWindow();

    void resetAllWindow();

    void launchSideBar();
}