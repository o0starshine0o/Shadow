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

package com.tencent.shadow.sample.host.manager;

import com.tencent.shadow.dynamic.host.DynamicPluginManager;
import com.tencent.shadow.dynamic.host.PluginManager;

import java.io.File;

public class Shadow {

    public static PluginManager getPluginManager(File apk){
        // Manager的动态化实现（只有一个方法，具体实现由插件apk完成）：https://juejin.im/post/5d1b466f6fb9a07ed524b995#heading-1
        final FixedPathPmUpdater fixedPathPmUpdater = new FixedPathPmUpdater(apk);
        File tempPm = fixedPathPmUpdater.getLatest();
        if (tempPm != null) {
            return new DynamicPluginManager(fixedPathPmUpdater);
        }
        return null;
    }

}
