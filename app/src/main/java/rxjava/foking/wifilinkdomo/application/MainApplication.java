package rxjava.foking.wifilinkdomo.application;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;


import java.lang.reflect.Field;
import java.text.SimpleDateFormat;


/**
 * Created by zhaoqy on 2015/12/17.
 */
public class MainApplication extends Application {
    private static MainApplication mInstance;
    public static String location_province;
    public static String location_city;
    public static double latitude, longitude;
    public static String
            location_district;
    public static Long deviceId;
    public static boolean set_city_flg;
    public static boolean first_flg, first_device_flg;
    public TextView mLocationResult, logMsg;


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(getApplicationContext());


    }
}
