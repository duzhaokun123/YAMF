package com.mja.reyamf.common.model

data class Config(
    var reduceDPI: Int = 50,
    /*
     * VIRTUAL_DISPLAY_FLAG_SECURE                          1 << 2
     * VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT            1 << 7
     * VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS  1 << 9
     * VIRTUAL_DISPLAY_FLAG_TRUSTED                         1 << 10
     */
    var flags: Int = 1668,
    var coloredController: Boolean = false,
    /*
     * 0: move task only
     * 1: start activity only
     * 2: move task, failback to start activity
    */
    var windowfy: Int = 0,
    /*
     * 0: TextureView
     * 1: SurfaceView
    */
    var surfaceView: Int = 0,
    var recentBackHome: Boolean = false,
    var showImeInWindow: Boolean = false,
    var defaultWindowWidth: Int = 280,
    var defaultWindowHeight: Int = 380,
    var hookLauncher: HookLauncher = HookLauncher(),
    var showForceShowIME: Boolean = false,
    var portraitY: Int = 0,
    var landscapeY: Int = 0,
    var favApps: MutableList<FavApps> = mutableListOf(),
    var launchSideBarAtBoot: Boolean = false,
    var enableSidebar: Boolean = true
) {
    data class HookLauncher(
        var hookRecents: Boolean = true,
        var hookTaskbar: Boolean = true,
        var hookPopup: Boolean = true,
        var hookTransientTaskbar: Boolean = false,
    )
}