/*
 * Tencent is pleased to support the open source community by making Tencent Shadow available.
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tencent.shadow.sample.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.tencent.shadow.core.common.Logger;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.core.manager.installplugin.InstalledPlugin;
import com.tencent.shadow.core.manager.installplugin.InstalledType;
import com.tencent.shadow.core.manager.installplugin.PluginConfig;
import com.tencent.shadow.dynamic.host.FailedException;
import com.tencent.shadow.dynamic.manager.PluginManagerThatUseDynamicLoader;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class FastPluginManager extends PluginManagerThatUseDynamicLoader {

    private static final Logger mLogger = LoggerFactory.getLogger(FastPluginManager.class);

    private ExecutorService mFixedPool = Executors.newFixedThreadPool(4);

    public FastPluginManager(Context context) {
        super(context);
    }

    /**
     * 从压缩包中解压插件，安装插件的runtime，loader，.so，更新数据库，返回最新的那一条数据
     */
    public InstalledPlugin installPlugin(String zip, String hash , boolean odex) throws IOException, JSONException, InterruptedException, ExecutionException {
        // 解压插件，包含loader.apk,runtime.apk,plugin.apk和config.json
        final PluginConfig pluginConfig = installPluginFromZip(new File(zip), hash);
        final String uuid = pluginConfig.UUID;
        List<Future> futures = new LinkedList<>();
        if (pluginConfig.runTime != null && pluginConfig.pluginLoader != null) {
            Future odexRuntime = mFixedPool.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    // 指定odex目录后创建DexClassLoader，其构造方法会最终调用DexPathList.makeDexElements加载dex文件，最终将odex路径赋值给RuntimeClassLoader，ApkClassLoader
                    oDexPluginLoaderOrRunTime(uuid, InstalledType.TYPE_PLUGIN_RUNTIME,
                            pluginConfig.runTime.file);
                    return null;
                }
            });
            futures.add(odexRuntime);
            Future odexLoader = mFixedPool.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    // 指定odex目录后创建DexClassLoader，其构造方法会最终调用DexPathList.makeDexElements加载dex文件，最终将odex路径赋值给RuntimeClassLoader，ApkClassLoader
                    oDexPluginLoaderOrRunTime(uuid, InstalledType.TYPE_PLUGIN_LOADER,
                            pluginConfig.pluginLoader.file);
                    return null;
                }
            });
            futures.add(odexLoader);
        }
        for (Map.Entry<String, PluginConfig.PluginFileInfo> plugin : pluginConfig.plugins.entrySet()) {
            final String partKey = plugin.getKey();
            final File apkFile = plugin.getValue().file;
            Future extractSo = mFixedPool.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    // 将插件依赖的so文件复制到 "lib/" 目录下，更新数据库关于so的path
                    extractSo(uuid, partKey, apkFile);
                    return null;
                }
            });
            futures.add(extractSo);
            if (odex) {
                Future odexPlugin = mFixedPool.submit(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        oDexPlugin(uuid, partKey, apkFile);
                        return null;
                    }
                });
                futures.add(odexPlugin);
            }
        }

        for (Future future : futures) {
            future.get();
        }
        onInstallCompleted(pluginConfig);

        return getInstalledPlugins(1).get(0);
    }


    @SuppressLint("WrongConstant")
    public void startPluginActivity(InstalledPlugin installedPlugin, String partKey, Intent pluginIntent) throws RemoteException, TimeoutException, FailedException {
        Intent intent = convertActivityIntent(installedPlugin, partKey, pluginIntent);
        // 首先会查找是否存在和被启动的Activity具有相同的亲和性的任务栈,同一个应用程序中的activity的亲和性一样
        // 如果有，就直接把这个栈（包含目标activity）整体移动到前台，并保持栈中的状态不变，即栈中的activity顺序不变，(这一步可能展示给用户的activity不是intent里面的)
        // 如果没有，则新建一个栈来存放被启动的activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mPluginLoader.startActivityInPluginProcess(intent);
    }

    public Intent convertActivityIntent(InstalledPlugin installedPlugin, String partKey, Intent pluginIntent) throws RemoteException, TimeoutException, FailedException {
        loadPlugin(installedPlugin.UUID, partKey);
        // BinderPluginLoader
        return mPluginLoader.convertActivityIntent(pluginIntent);
    }

    private void loadPluginLoaderAndRuntime(String uuid, String partKey) throws RemoteException, TimeoutException, FailedException {
        // 首次出现了PPS，插件进程PluginProcessService的接口，https://juejin.im/post/5d1968545188255543342406
        if (mPpsController == null) {
            // 绑定PPS，拿到插件进程的IBinder，包装成PPSController
            // 注意，这个IBinder和之后loadPluginLoader的IBinder不一样
            // 这个IBinder是控制进程Service的
            bindPluginProcessService(getPluginProcessServiceName(partKey));
            waitServiceConnected(10, TimeUnit.SECONDS);
        }
        // 在插件进程内使用MUUIDManager（从宿主传递进来的binder）反过来调用宿主进程的PluginManagerThatUseDynamicLoader.getRunTime，构造一个InstalledApk返回给PPS
        // 根据InstalledApk内的path信息，将RuntimeClassLoader（这个就是下面说的DexClassLoader）插入到BootClassLoader与PathClassLoader之间
        loadRunTime(uuid);
        // 通过PpsController，在插件进程中（PluginProcessService），用反射的方式构建（LoaderImplLoader类中）出LoaderFactoryImpl
        // 在LoaderFactoryImpl中使用buildLoader方法构建出PluginLoaderBinder（注意，这个Binder不是PPSBinder了）
        // 在构建PluginLoaderBinder过程中，构建了一个DynamicPluginLoader类
        // 在DynamicPluginLoader的构造方法中用反射的方式构建出了CoreLoaderFactoryImpl
        // 在CoreLoaderFactoryImpl中使用build方法，构建出SamplePluginLoader（这个才是插件真正的Loader，同时也是IBinder）
        // 最后SamplePluginLoader通过IBinder传输被封装成BinderPluginLoader（mPluginLoader），在宿主进程中，供manager管理
        loadPluginLoader(uuid);
    }

    public void loadPlugin(String uuid, String partKey) throws RemoteException, TimeoutException, FailedException {
        // 到此为止，mPluginLoader还木有
        loadPluginLoaderAndRuntime(uuid, partKey);
        // 到这mPluginLoader就已经完成初始化了
        Map map = mPluginLoader.getLoadedPlugin();
        if (!map.containsKey(partKey)) {
            mPluginLoader.loadPlugin(partKey);
        }
        Boolean isCall = (Boolean) map.get(partKey);
        if (isCall == null || !isCall) {
            mPluginLoader.callApplicationOnCreate(partKey);
        }
    }


    /**
     * 由具体的Manager提供名称，这个名称需要在宿主中进行注册
     *
     * @param partKey 在demo中由宿主进行选择
     * @return 在宿主中注册过的Service名称（全名）
     */
    protected abstract String getPluginProcessServiceName(String partKey);

}
