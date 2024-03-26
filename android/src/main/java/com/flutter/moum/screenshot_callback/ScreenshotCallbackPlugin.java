package com.flutter.moum.screenshot_callback;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;

import androidx.annotation.NonNull;


import com.akexorcist.screenshotdetection.ScreenshotDetectionDelegate;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ScreenshotCallbackPlugin implements MethodCallHandler, FlutterPlugin
        , ActivityAware,ScreenshotDetectionDelegate.ScreenshotDetectionListener
{
    private static MethodChannel channel;
    private static final String ttag = "screenshot_callback";

    private Context applicationContext;

    private Handler handler;

    private ScreenshotDetectionDelegate screenshotDelegate;

    private Activity activity;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getApplicationContext(), binding.getBinaryMessenger());
    }

    private void onAttachedToEngine(Context applicationContext, BinaryMessenger messenger) {
        this.applicationContext = applicationContext;
        channel = new MethodChannel(messenger, "flutter.moum/screenshot_callback");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        applicationContext = null;
        if (channel != null) {
            channel.setMethodCallHandler(null);
            channel = null;
        }
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("initialize")) {
            handler = new Handler(Looper.getMainLooper());
            screenshotDelegate = new ScreenshotDetectionDelegate(activity, this);
            screenshotDelegate.startScreenshotDetection();
            result.success("initialize");
        } else if (call.method.equals("dispose")) {
            screenshotDelegate.stopScreenshotDetection();
            screenshotDelegate = null;
            result.success("dispose");
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onScreenCaptured(@NonNull String s) {
        channel.invokeMethod("onCallback", s);
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        channel.invokeMethod("onCallback", null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }
}
