package com.tencent.shadow.sample.host;

import android.app.Application;
import com.tencent.shadow.sample.manager.PluginManagerDelegate;

/**
 * @author admin
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        PluginManagerDelegate.createPluginManager(this);
    }
}
