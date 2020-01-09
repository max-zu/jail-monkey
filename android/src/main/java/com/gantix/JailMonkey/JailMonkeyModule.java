package com.gantix.JailMonkey;

import android.app.Activity;

import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.provider.Settings;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import android.Manifest;
import android.content.pm.PackageManager;


import android.content.Context;
import android.provider.Settings;
import android.os.Build;


import android.location.Location;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import static com.gantix.JailMonkey.AdbEnabled.AdbEnabled.AdbEnabled;
import static com.gantix.JailMonkey.ExternalStorage.ExternalStorageCheck.isOnExternalStorage;
import static com.gantix.JailMonkey.HookDetection.HookDetectionCheck.hookDetected;
import static com.gantix.JailMonkey.MockLocation.MockLocationCheck.isMockLocationOn;
import static com.gantix.JailMonkey.Rooted.RootedCheck.isJailBroken;


public class JailMonkeyModule extends ReactContextBaseJavaModule {

    ReactApplicationContext reactContext;


    public JailMonkeyModule(ReactApplicationContext reactContext, boolean loadConstantsAsynchronously) {
        super(reactContext);

        this.reactContext = reactContext;

    }

    @Override
    public String getName() {
        return "JailMonkey";
    }


    @ReactMethod
    public void isDevelopmentSettingsMode(Promise p) {
        boolean isDevelopmentSettingsMode;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isDevelopmentSettingsMode = Settings.System.getInt(this.reactContext.getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0) != 1;
        } else {
            isDevelopmentSettingsMode = Settings.Global.getInt(this.reactContext.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        }
        p.resolve(isDevelopmentSettingsMode);
    }


    @ReactMethod
    public void isDebuggedMode(Promise p) {
        boolean isDebuggedMode;
        if (Debug.isDebuggerConnected()) {
            isDebuggedMode = true;
        } else {
            isDebuggedMode = (this.reactContext.getApplicationContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
        p.resolve(isDebuggedMode);
    }

    @ReactMethod
    public void isMockLocationEnabled(final Promise promise) {
        Activity activity = getCurrentActivity();
        final ReactContext context = getReactApplicationContext();

        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
        {
            promise.resolve(false);
            return;
        }


        FusedLocationProviderClient mFusedLocationClient;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity,
                        new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                boolean isMock = false;
                                if (location != null) {
                                    // Logic to handle location object

                                    if (Build.VERSION.SDK_INT >= 18) {
                                        isMock = location.isFromMockProvider();
                                    } else {
                                        if ("0".equals(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION)))
                                            isMock =  false;
                                        else {
                                            isMock =  true;
                                        }
                                    }
                                }
                                promise.resolve(isMock);
                                return;
                            }
                        }
                ).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                promise.resolve(false);
                return;
            }
        }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        promise.reject(e.toString());
                        return;
                    }
                }
        );
    }

    @Override
    public @Nullable
    Map<String, Object> getConstants() {
        ReactContext context = getReactApplicationContext();
        final Map<String, Object> constants = new HashMap<>();
        constants.put("isJailBroken", isJailBroken(context));
        constants.put("hookDetected", hookDetected(context));
        constants.put("canMockLocation", isMockLocationOn(context));
        constants.put("isOnExternalStorage", isOnExternalStorage(context));
        constants.put("AdbEnabled", AdbEnabled(context));
        return constants;
    }
}
