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

package com.tencent.shadow.dynamic.host;

import android.content.Context;
import android.os.Bundle;

import com.tencent.shadow.core.common.Logger;
import com.tencent.shadow.core.common.LoggerFactory;

import java.io.File;

public final class DynamicPluginManager implements PluginManager {

    final private PluginManagerUpdater mUpdater;
    // 典型的代理模式
    private PluginManagerImpl mManagerImpl;
    private long mLastModified;
    private static final Logger mLogger = LoggerFactory.getLogger(DynamicPluginManager.class);

    public DynamicPluginManager(PluginManagerUpdater updater) {
        if (updater.getLatest() == null) {
            throw new IllegalArgumentException("构造DynamicPluginManager时传入的PluginManagerUpdater" +
                    "必须已经已有本地文件，即getLatest()!=null");
        }
        mUpdater = updater;
    }

    @Override
    public void enter(Context context, long fromId, Bundle bundle, EnterCallback callback) {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("enter fromId:" + fromId + " callback:" + callback);
        }
        updateManagerImpl(context);
        mManagerImpl.enter(context, fromId, bundle, callback);
        mUpdater.update();
    }

    @Override
    public <T> T getPluginClass(Context context, Class<T> clazz, String name) {
        updateManagerImpl(context);
        mUpdater.update();
        return mManagerImpl.getPluginClass(context, clazz, name);
    }

    public void release() {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("release");
        }
        if (mManagerImpl != null) {
            mManagerImpl.onDestroy();
            mManagerImpl = null;
        }
    }

    /**
     * 更新插件的实现
     * 比对上次更新时间，若不相等，则以最新文件进行更新
     */
    private void updateManagerImpl(Context context) {
        File latestManagerImplApk = mUpdater.getLatest();
        long lastModified = latestManagerImplApk.lastModified();
        if (mLogger.isInfoEnabled()) {
            mLogger.info("mLastModified != lastModified : " + (mLastModified != lastModified));
        }
        if (mLastModified != lastModified) {
            // loader保存了apk和odex的目录
            ManagerImplLoader implLoader = new ManagerImplLoader(context, latestManagerImplApk);
            // 创建ClassLoader（含白名单），代理Context
            PluginManagerImpl newImpl = implLoader.load();
            // 执行老控件的保存和销毁，新控件的创建
            Bundle state;
            if (mManagerImpl != null) {
                state = new Bundle();
                mManagerImpl.onSaveInstanceState(state);
                mManagerImpl.onDestroy();
            } else {
                state = null;
            }
            newImpl.onCreate(state);
            mManagerImpl = newImpl;
            mLastModified = lastModified;
        }
    }

    public PluginManager getManagerImpl() {
        return mManagerImpl;
    }
}
