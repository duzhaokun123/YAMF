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



//    int createVirtualDisplay(String name, int width, int height, int densityDpi, in Surface surface, int flags);
//
//    boolean resizeVirtualDisplay(int id, int width, int height, int densityDpi);
//
//    boolean setVirtualDisplaySurface(int id, in Surface surface);
//
//    boolean releaseVirtualDisplay(int id);
//
//    boolean releaseAll();
//
//    String getVirtualDisplayInfoS(int id);
//
//    int[] getVirtualDisplayIds();
//
//    void showOverlay();
}