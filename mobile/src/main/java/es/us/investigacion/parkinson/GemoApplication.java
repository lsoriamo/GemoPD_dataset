package es.us.investigacion.parkinson;

import android.app.Application;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
// Entry point to the application. In charge of launching the Service
public class GemoApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(new FlowConfig.Builder(this).build());
        Intent intent = new Intent(this, WearManager.class);
        startService(intent);
    }
}
