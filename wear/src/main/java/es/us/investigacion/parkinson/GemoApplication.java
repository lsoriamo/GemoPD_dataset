package es.us.investigacion.parkinson;

import android.app.Application;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
public class GemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
        FlowManager.init(new FlowConfig.Builder(this).build());
        Intent service = new Intent(this, DeviceListenerService.class);
        this.startService(service);
    }
}
