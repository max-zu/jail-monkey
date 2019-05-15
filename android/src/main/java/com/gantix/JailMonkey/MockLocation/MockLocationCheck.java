package com.gantix.JailMonkey.MockLocation;

import android.content.Context;
import android.provider.Settings;
import android.os.Build;
import android.app.AppOpsManager;

public class MockLocationCheck {

    //returns true if mock location enabled, false if not enabled.
    public static boolean isMockLocationOn(Context context) {
        if ("0".equals(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION))) {
            return false;
        } else {
            return true;
        }
    }


    public static boolean isMockLocationEnabled(Context context) {
        boolean isMockLocation = false;
        try {
            //if marshmallow
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID)== AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockLocation = !"0".equals(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION));
            }
        } catch (Exception e) {
            return isMockLocation;
        }
        return isMockLocation;
    }
}
