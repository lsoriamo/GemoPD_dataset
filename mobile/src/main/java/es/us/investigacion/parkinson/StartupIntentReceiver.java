package es.us.investigacion.parkinson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import es.us.investigacion.parkinson.records.RecordsActivity;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
public class StartupIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, RecordsActivity.class);
        context.startService(serviceIntent);
    }
}
