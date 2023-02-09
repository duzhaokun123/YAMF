package android.view;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(WindowManager.LayoutParams.class)
public class WindowLayoutParamsHidden {
    public int privateFlags;

    public static final int PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY = 0x00100000;
}
