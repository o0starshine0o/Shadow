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

import android.os.IBinder;
import android.os.Parcel;

import com.tencent.shadow.core.common.InstalledApk;

class BinderUuidManager implements UuidManager {
    private IBinder mRemote;

    BinderUuidManager(IBinder remote) {
        mRemote = remote;
    }

    private void checkException(Parcel reply) throws FailedException, NotFoundException {
        int i = reply.readInt();
        if (i == UuidManager.TRANSACTION_CODE_FAILED_EXCEPTION) {
            throw new FailedException(reply);
        } else if (i == UuidManager.TRANSACTION_CODE_NOT_FOUND_EXCEPTION) {
            throw new NotFoundException(reply);
        } else if (i != UuidManager.TRANSACTION_CODE_NO_EXCEPTION) {
            throw new RuntimeException("不认识的Code==" + i);
        }
    }

    @Override
    public InstalledApk getPlugin(String uuid, String partKey) throws android.os.RemoteException, FailedException, NotFoundException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        InstalledApk result;
        try {
            data.writeInterfaceToken(UuidManager.DESCRIPTOR);
            data.writeString(uuid);
            data.writeString(partKey);
            mRemote.transact(UuidManager.TRANSACTION_getPlugin, data, reply, 0);
            checkException(reply);
            if ((0 != reply.readInt())) {
                result = InstalledApk.CREATOR.createFromParcel(reply);
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
    public InstalledApk getPluginLoader(String uuid) throws android.os.RemoteException, NotFoundException, FailedException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        InstalledApk result;
        try {
            data.writeInterfaceToken(UuidManager.DESCRIPTOR);
            data.writeString(uuid);
            mRemote.transact(UuidManager.TRANSACTION_getPluginLoader, data, reply, 0);
            checkException(reply);
            if ((0 != reply.readInt())) {
                result = InstalledApk.CREATOR.createFromParcel(reply);
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
    public InstalledApk getRuntime(String uuid) throws android.os.RemoteException, NotFoundException, FailedException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        InstalledApk result;
        try {
            data.writeInterfaceToken(UuidManager.DESCRIPTOR);
            data.writeString(uuid);
            mRemote.transact(UuidManager.TRANSACTION_getRuntime, data, reply, 0);
            checkException(reply);
            if ((0 != reply.readInt())) {
                result = InstalledApk.CREATOR.createFromParcel(reply);
            } else {
                result = null;
            }
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }
}
