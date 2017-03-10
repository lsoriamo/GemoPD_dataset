package es.us.investigacion.parkinson;

import android.app.Application;
import android.content.Intent;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
public class GemoApplication extends Application {
    public static PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getString(R.string.app_name));
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
        FlowManager.init(new FlowConfig.Builder(this).build());
        Intent service = new Intent(this, DeviceListenerService.class);
        this.startService(service);
    }
}
