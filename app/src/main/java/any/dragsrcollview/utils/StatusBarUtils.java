package any.dragsrcollview.utils;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by anyrsan on 2017/12/17.
 */

public class StatusBarUtils {


    public static int dp2px(Resources res, float paramFloat) {
        return (int) (0.5F + paramFloat * res.getDisplayMetrics().density);
    }

    // 获取状态栏高度，一般为 25dp
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
