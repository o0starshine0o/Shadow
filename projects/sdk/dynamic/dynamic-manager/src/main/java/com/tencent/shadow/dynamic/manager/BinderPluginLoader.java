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
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.tencent.shadow.dynamic.loader.PluginLoader;
import com.tencent.shadow.dynamic.loader.PluginServiceConnection;

import java.util.Map;

class BinderPluginLoader implements PluginLoader {
    final private IBinder mRemote;

    BinderPluginLoader(IBinder remote) {
        mRemote = remote;
    }

    @Override
    public void loadPlugin(String partKey) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeString(partKey);
            mRemote.transact(TRANSACTION_loadPlugin, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override
    public Map getLoadedPlugin() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        Map result;
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            mRemote.transact(TRANSACTION_getLoadedPlugin, data, reply, 0);
            reply.readException();
            ClassLoader cl = this.getClass().getClassLoader();
            result = reply.readHashMap(cl);
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    @Override
    public void callApplicationOnCreate(String partKey) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeString(partKey);
            mRemote.transact(TRANSACTION_callApplicationOnCreate, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override
    public Intent convertActivityIntent(Intent pluginActivityIntent) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        Intent result;
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            if ((pluginActivityIntent != null)) {
                data.writeInt(1);
                pluginActivityIntent.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            mRemote.transact(TRANSACTION_convertActivityIntent, data, reply, 0);
            reply.readException();
            if ((0 != reply.readInt())) {
                result = Intent.CREATOR.createFromParcel(reply);
            } else {
                result = null;
            }
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    @Override
    public ComponentName startPluginService(Intent pluginServiceIntent) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ComponentName result;
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            if ((pluginServiceIntent != null)) {
                data.writeInt(1);
                pluginServiceIntent.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            mRemote.transact(TRANSACTION_startPluginService, data, reply, 0);
            reply.readException();
            if ((0 != reply.readInt())) {
                result = ComponentName.CREATOR.createFromParcel(reply);
            } else {
                result = null;
            }
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    @Override
    public boolean stopPluginService(Intent pluginServiceIntent) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean result;
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            if ((pluginServiceIntent != null)) {
                data.writeInt(1);
                pluginServiceIntent.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            mRemote.transact(TRANSACTION_stopPluginService, data, reply, 0);
            reply.readException();
            result = (0 != reply.readInt());
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    @Override
    public boolean bindPluginService(Intent pluginServiceIntent, PluginServiceConnection connection, int flags) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean result;
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            if ((pluginServiceIntent != null)) {
                data.writeInt(1);
                pluginServiceIntent.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            data.writeStrongBinder((((connection != null)) ? (new PluginServiceConnectionBinder(connection)) : (null)));
            data.writeInt(flags);
            mRemote.transact(TRANSACTION_bindPluginService, data, reply, 0);
            reply.readException();
            result = (0 != reply.readInt());
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    @Override
    public void unbindService(PluginServiceConnection conn) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeStrongBinder((((conn != null)) ? (new PluginServiceConnectionBinder(conn)) : (null)));
            mRemote.transact(TRANSACTION_unbindService, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override
    public void startActivityInPluginProcess(Intent intent) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            intent.writeToParcel(data, 0);
            mRemote.transact(TRANSACTION_startActivityInPluginProcess, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
