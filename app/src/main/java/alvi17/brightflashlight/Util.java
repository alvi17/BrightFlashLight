package alvi17.brightflashlight;

import android.app.ActivityManager;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by User on 12/25/2016.
 */

public class Util {

    public static void saveInfo(Context context, String key, boolean value )
    {
        try
        {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply();
        }
        catch (Exception ex)
        {
            Log.e("Util"," saveInfo "+ex+"");
        }
    }
    public static boolean getInfo(Context context, String key)
    {
        try {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key,false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
