package com.tencent.shadow.core.runtime;

import android.app.Activity;
import android.app.Dialog;

import com.tencent.shadow.core.runtime.container.PluginContainerActivity;

/**
 * 以转调到静态方法方式重新实现Dialog支持
 * @author admin
 */
public class ShadowDialogSupport {

    /**
     * 在插件中设置activity的时候因为拿到的是Shadow的Activity，不能直接给Dialog
     * 需要从Shadow的Activity中取出真实的Android的Activity，设置给Dialog
     */
    public static void dialogSetOwnerActivity(Dialog dialog, ShadowActivity activity) {
        Activity hostActivity = (Activity) activity.hostActivityDelegator.getHostActivity();
        dialog.setOwnerActivity(hostActivity);
    }

    /**
     * 获取Dialog的Activity时，因为是在插件中使用，需要拿到Shadow的Activity
     */
    public static ShadowActivity dialogGetOwnerActivity(Dialog dialog) {
        PluginContainerActivity ownerActivity = (PluginContainerActivity) dialog.getOwnerActivity();
        if (ownerActivity != null) {
            return (ShadowActivity) PluginActivity.get(ownerActivity);
        } else {
            return null;
        }
    }

}
