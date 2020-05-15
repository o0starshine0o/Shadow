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
import android.database.DefaultDatabaseErrorHandler;
import android.os.Build;
import android.os.Bundle;

/**
 * Manager的动态化
 * 使用方持有的接口
 * 我们将接口打包在宿主中，接口就轻易不能更新了。但是它的实现总是可以更新的
 *
 * @author cubershi
 */
public interface PluginManager {

    /**
     * 这就是Manager的唯一方法，宿主中只会调用这个方法
     *
     * @param context 传入当前界面的Context以便打开下一个插件Activity
     * @param formId  标识本次请求的来源位置，用于区分入口，用来让Manager的实现逻辑分辨这一次enter是从哪里来的
     * @param bundle  将所有插件中可能用到的参数通过Bundle传给插件
     * @param callback 用于从PluginManager实现中返回View，供Manager可以返回一个动态加载的View作为插件的Loading View
     */
    void enter(Context context, long formId, Bundle bundle, EnterCallback callback);


    /**
     * 宿主通过这个方法，获取插件app中类的实例（无跨进程相关内容）
     *
     * @param context 传入当前界面的Context，以便加载插件
     * @param pluginZipPath  插件的zip目录
     * @param partKey  插件的partKey
     * @param name  需要实例化的插件中的类名
     * @return 从插件中获取到的类
     */
    Class<?> getPluginClass(Context context, String pluginZipPath, String partKey, String name);
}
