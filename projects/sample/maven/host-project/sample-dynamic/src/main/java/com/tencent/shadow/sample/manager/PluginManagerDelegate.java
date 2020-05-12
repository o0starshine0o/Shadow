package com.tencent.shadow.sample.manager;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.dynamic.host.DynamicPluginManager;
import com.tencent.shadow.dynamic.host.DynamicRuntime;
import com.tencent.shadow.dynamic.host.PluginManager;

import java.io.File;
import java.util.concurrent.Future;

import static android.os.Process.myPid;

/**
 * @author admin
 */
public class PluginManagerDelegate {
    private static final String PLUGIN_PROCESS = ":plugin";

    /**
     * 这个PluginManager对象在Manager升级前后是不变的。它内部持有具体实现，升级时更换具体实现。
     */
    private static PluginManager sPluginManager;

    public static PluginManager getPluginManager() {
        return sPluginManager;
    }

    public static void createPluginManager(Application application) {
        //Log接口Manager也需要使用，所以主进程也初始化。
        LoggerFactory.setILoggerFactory(AndroidLoggerFactory.getInstance());

        if (isProcess(application, PLUGIN_PROCESS)) {
            //在全动态架构中，Activity组件没有打包在宿主而是位于被动态加载的runtime，
            //为了防止插件crash后，系统自动恢复crash前的Activity组件，此时由于没有加载runtime而发生classNotFound异常，导致二次crash
            //因此这里恢复加载上一次的runtime
            DynamicRuntime.recoveryRuntime(application);
        }

        FixedPathPmUpdater fixedPathPmUpdater = new FixedPathPmUpdater(new File("/data/local/tmp/sample-manager-debug.apk"));
        //之前正在更新中，暗示更新出错了，应该放弃之前的缓存， //没有本地缓存
        boolean needWaitingUpdate = fixedPathPmUpdater.wasUpdating() || fixedPathPmUpdater.getLatest() == null;
        Future<File> update = fixedPathPmUpdater.update();
        if (needWaitingUpdate) {
            try {
                update.get();//这里是阻塞的，需要业务自行保证更新Manager足够快。
            } catch (Exception e) {
                throw new RuntimeException("Sample程序不容错", e);
            }
        }
        sPluginManager = new DynamicPluginManager(fixedPathPmUpdater);
    }

    private static boolean isProcess(Context context, String processName) {
        String currentProcessName = "";
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null){
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == myPid()) {
                currentProcessName = processInfo.processName;
                break;
            }
        }

        return currentProcessName.endsWith(processName);
    }
}
