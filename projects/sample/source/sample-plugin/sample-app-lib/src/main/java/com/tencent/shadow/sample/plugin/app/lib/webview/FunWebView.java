package com.tencent.shadow.sample.plugin.app.lib.webview;

import android.content.Context;
import android.util.AttributeSet;

import com.tencent.smtt.sdk.WebView;

import java.util.Map;

public class FunWebView extends WebView {
    public FunWebView(Context context, boolean b) {
        super(context, b);
    }

    public FunWebView(Context context) {
        super(context);
    }

    public FunWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public FunWebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public FunWebView(Context context, AttributeSet attributeSet, int i, boolean b) {
        super(context, attributeSet, i, b);
    }

    public FunWebView(Context context, AttributeSet attributeSet, int i, Map<String, Object> map, boolean b) {
        super(context, attributeSet, i, map, b);
    }
}
