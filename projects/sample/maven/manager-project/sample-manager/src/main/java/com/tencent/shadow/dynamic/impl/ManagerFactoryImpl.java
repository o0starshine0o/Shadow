package com.tencent.shadow.dynamic.impl;

import android.content.Context;

import com.tencent.shadow.dynamic.host.ManagerFactory;
import com.tencent.shadow.dynamic.host.PluginManagerImpl;
import com.tencent.shadow.sample.manager.SamplePluginManager;

/**
 * 此类包名及类名固定
 * 因为需要在ManagerImplLoader中（宿主apk），通过反射来构建这个类
 * @author admin
 */
public final class ManagerFactoryImpl implements ManagerFactory {
    @Override
    public PluginManagerImpl buildManager(Context context) {
        return new SamplePluginManager(context);
    }
}
