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

package com.tencent.shadow.sample.host;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.sample.constant.Constant;


/**
 * @author admin
 */
public class PluginLoadActivity extends Activity {

    private ViewGroup mViewGroup;

    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        mViewGroup = findViewById(R.id.container);

//        startPlugin();
        loadFragment();

    }


    public void startPlugin() {

        PluginHelper.getInstance().singlePool.execute(new Runnable() {
            @Override
            public void run() {
                HostApplication.getApp().loadPluginManager(PluginHelper.getInstance().pluginManagerFile);

                Bundle bundle = new Bundle();
                bundle.putString(Constant.KEY_PLUGIN_ZIP_PATH, PluginHelper.getInstance().pluginZipFile.getAbsolutePath());
                bundle.putString(Constant.KEY_PLUGIN_PART_KEY, getIntent().getStringExtra(Constant.KEY_PLUGIN_PART_KEY));
                bundle.putString(Constant.KEY_ACTIVITY_CLASSNAME, getIntent().getStringExtra(Constant.KEY_ACTIVITY_CLASSNAME));

                // 注意，这个enter是标准的代理模式，代理的是插件的PluginManagerImpl, SamplePluginManager
                HostApplication.getApp().getPluginManager().enter(PluginLoadActivity.this, Constant.FROM_ID_START_ACTIVITY, bundle, new EnterCallback() {
                    @Override
                    public void onShowLoadingView(final View view) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mViewGroup.addView(view);
                            }
                        });
                    }

                    @Override
                    public void onCloseLoadingView() {
                        finish();
                    }

                    @Override
                    public void onEnterComplete() {

                    }
                });
            }
        });
    }

    void loadFragment() {
        PluginHelper.getInstance().singlePool.execute(new Runnable() {
            @Override
            public void run() {

                HostApplication.getApp().loadPluginManager(PluginHelper.getInstance().pluginManagerFile);

                String pluginZipPath = PluginHelper.getInstance().pluginZipFile.getAbsolutePath();
                String partKey = getIntent().getStringExtra(Constant.KEY_PLUGIN_PART_KEY);
                String name = getIntent().getStringExtra(Constant.KEY_ACTIVITY_CLASSNAME);

                // 注意，这个enter是标准的代理模式，代理的是插件的PluginManagerImpl, SamplePluginManager
                Class<?> clazz = HostApplication.getApp().getPluginManager().getPluginClass(PluginLoadActivity.this, pluginZipPath, partKey, name);
                if (clazz != null){
                    try {
                        Fragment fragment = (Fragment)clazz.newInstance();
                        getFragmentManager().beginTransaction().add(R.id.fragmentContainer, fragment).commit();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewGroup.removeAllViews();
    }
}
