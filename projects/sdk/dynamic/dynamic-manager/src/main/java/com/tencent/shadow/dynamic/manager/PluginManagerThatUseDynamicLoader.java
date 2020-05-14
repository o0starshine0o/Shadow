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

package com.tencent.shadow.dynamic.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;

import com.tencent.shadow.core.common.Logger;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.dynamic.host.FailedException;
import com.tencent.shadow.dynamic.host.PluginManagerImpl;
import com.tencent.shadow.dynamic.host.PluginProcessService;
import com.tencent.shadow.dynamic.host.PpsController;
import com.tencent.shadow.dynamic.host.PpsStatus;
import com.tencent.shadow.dynamic.loader.PluginLoader;

public abstract class PluginManagerThatUseDynamicLoader extends BaseDynamicPluginManager implements PluginManagerImpl {

    private static final Logger mLogger = LoggerFactory.getLogger(PluginManagerThatUseDynamicLoader.class);
    /**
     * 插件进程PluginProcessService的接口
     */
    protected PpsController mPpsController;

    /**
     * 插件加载服务端接口6
     */
    protected PluginLoader mPluginLoader;

    protected PluginManagerThatUseDynamicLoader(Context context) {
        super(context);
    }

    /**
     * 位于宿主进程中
     */
    @Override
    protected void onPluginServiceConnected(ComponentName name, IBinder service) {
        // 拿到了插件进程的IBinder
        mPpsController = PluginProcessService.wrapBinder(service);
        try {
            // 这里要进行跨进程的通信了：宿主进程把自己的PluginManager包裹成Binder传递给插件进程
            // 这个Binder具有3种能力，插件进程可以使用：1、获取插件；2、获取Loader；3、获取RunTime
            mPpsController.setUuidManager(new UuidManagerBinder(PluginManagerThatUseDynamicLoader.this));
        } catch (DeadObjectException e) {
            if (mLogger.isErrorEnabled()) {
                mLogger.error("onServiceConnected RemoteException:" + e);
            }
        } catch (RemoteException e) {
            if (e.getClass().getSimpleName().equals("TransactionTooLargeException")) {
                if (mLogger.isErrorEnabled()) {
                    mLogger.error("onServiceConnected TransactionTooLargeException:" + e);
                }
            } else {
                throw new RuntimeException(e);
            }
        }

        try {
            // 这里要进行跨进程的通信了，第一次的话，基本为空，因为还木有loadPluginLoader操作
            IBinder iBinder = mPpsController.getPluginLoader();
            if (iBinder != null) {
                mPluginLoader = new BinderPluginLoader(iBinder);
            }
        } catch (RemoteException e) {
            if (mLogger.isErrorEnabled()) {
                mLogger.error("onServiceConnected mPpsController getPluginLoader:", e);
            }
        }
    }

    @Override
    protected void onPluginServiceDisconnected(ComponentName name) {
        mPpsController = null;
        mPluginLoader = null;
    }

    public final void loadRunTime(String uuid) throws RemoteException, FailedException {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("loadRunTime mPpsController:" + mPpsController);
        }
        PpsStatus ppsStatus = mPpsController.getPpsStatus();
        if (!ppsStatus.runtimeLoaded) {
            mPpsController.loadRuntime(uuid);
        }
    }

    public final void loadPluginLoader(String uuid) throws RemoteException, FailedException {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("loadPluginLoader mPluginLoader:" + mPluginLoader);
        }
        if (mPluginLoader == null) {
            PpsStatus ppsStatus = mPpsController.getPpsStatus();
            if (!ppsStatus.loaderLoaded) {
                mPpsController.loadPluginLoader(uuid);
            }
            IBinder iBinder = mPpsController.getPluginLoader();
            mPluginLoader = new BinderPluginLoader(iBinder);
        }
    }
}
