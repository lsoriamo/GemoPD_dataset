package es.us.investigacion.parkinson.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.GridViewPager;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.CopyOnWriteArrayList;

import es.us.investigacion.parkinson.R;

public class MainActivity extends WearableActivity{

    private static CopyOnWriteArrayList<AmbientListener> ambientListeners;

    public static void addAmbientListener(AmbientListener listener) {
        if (ambientListeners != null && listener != null) {
            if (listener != null)
                ambientListeners.add(listener);
        }
    }

    public static void removeAmbientListener(AmbientListener listener) {
        if (ambientListeners != null && listener != null) {
            if (listener != null)
                ambientListeners.remove(listener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.grid_view_page);

        final GridViewPager mGridPager = (GridViewPager) findViewById(R.id.pager);
        mGridPager.setAdapter(new GridPagerAdapter(this, getFragmentManager()));
        ambientListeners = new CopyOnWriteArrayList<>();
        setAmbientEnabled();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onEnterAmbient(final Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        if (ambientListeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    for (AmbientListener listener : ambientListeners) {
                        if (listener != null)
                            listener.onEnterAmbient(ambientDetails);
                    }
                }
            });
        }
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        if (ambientListeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (AmbientListener listener : ambientListeners) {
                        if (listener != null)
                            listener.onExitAmbient();
                    }
                }
            });
        }
    }

    public interface AmbientListener {
        public void onExitAmbient();

        public void onEnterAmbient(Bundle ambientDetails);
    }
}
