package com.tencent.shadow.test.dynamic.manager;

import android.content.Context;
import android.os.Bundle;

import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.core.manager.installplugin.InstalledPlugin;
import com.tencent.shadow.dynamic.host.ApkClassLoader;
import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.dynamic.host.PluginManagerImpl;
import com.tencent.shadow.test.lib.constant.Constant;

final public class TestDynamicPluginManager implements PluginManagerImpl {
    final private ActivityTestDynamicPluginManager activityPluginManager;
    final private ServiceTestDynamicPluginManager serviceTestDynamicPluginManager;
    public TestDynamicPluginManager(Context context) {
        this.activityPluginManager = new ActivityTestDynamicPluginManager(context);
        this.serviceTestDynamicPluginManager = new ServiceTestDynamicPluginManager(context);
    }

    @Override
    public void onCreate(Bundle bundle) {
        activityPluginManager.onCreate(bundle);
        serviceTestDynamicPluginManager.onCreate(bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        activityPluginManager.onSaveInstanceState(bundle);
        serviceTestDynamicPluginManager.onSaveInstanceState(bundle);
    }

    @Override
    public void onDestroy() {
        activityPluginManager.onDestroy();
        serviceTestDynamicPluginManager.onDestroy();
    }

    @Override
    public void enter(Context context, long fromId, Bundle bundle, EnterCallback callback) {
        if (fromId == Constant.FROM_ID_BIND_SERVICE) {
            serviceTestDynamicPluginManager.enter(context, fromId, bundle, callback);
        } else {
            activityPluginManager.enter(context, fromId, bundle, callback);
        }
    }

//    @Override
//    public Class<?> getPluginClass(Context context, String pluginZipPath, String partKey, String name) {
//        try {
//            InstalledPlugin installedPlugin = serviceTestDynamicPluginManager.installPlugin(pluginZipPath, null, true);
//            serviceTestDynamicPluginManager.loadPlugin(installedPlugin.UUID, partKey);
//            InstalledPlugin.Part part = installedPlugin.getPart(partKey);
//            InstalledApk installedApk = new InstalledApk(part.pluginFile, part.oDexDir, part.libraryDir);
//            ApkClassLoader pluginLoaderClassLoader = new ApkClassLoader(installedApk,getClass().getClassLoader(),new String[]{},1);
//            return pluginLoaderClassLoader.loadClass(name);
//        } catch (Exception e) {
//            return null;
//        }
//    }
}
