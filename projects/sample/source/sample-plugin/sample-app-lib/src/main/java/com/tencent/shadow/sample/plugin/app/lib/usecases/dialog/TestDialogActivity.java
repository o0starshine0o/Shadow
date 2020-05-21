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

package com.tencent.shadow.sample.plugin.app.lib.usecases.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import com.tencent.shadow.sample.plugin.app.lib.R;
import com.tencent.shadow.sample.plugin.app.lib.gallery.BaseActivity;
import com.tencent.shadow.sample.plugin.app.lib.gallery.cases.entity.UseCase;

public class TestDialogActivity extends BaseActivity {

    public static class Case extends UseCase {
        @Override
        public String getName() {
            return "Dialog 相关测试";
        }

        @Override
        public String getSummary() {
            return "测试show Dialog";
        }

        @Override
        public Class getPageClass() {
            return TestDialogActivity.class;
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_activity);
    }

    public void show(View view) {
        TestDialog dialog = new TestDialog(this);
        dialog.setContentView(R.layout.layout_dialog);

        dialog.show();
    }

    /**
     * 返回值类型"Dialog"会被替换为"ShadowDialog"，因为"ShadowDialog"是"Dialog"的子类，而不是"AlertDialog"的超类
     * 所以这边会报错
     */
    public Dialog showAlert(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null);
        builder.setView(dialogView);
        // 返回的是AlertDialog，不能用Dialog的子类"ShadowDialog"来接受这个返回值
        AlertDialog dialog = builder.show();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
}
