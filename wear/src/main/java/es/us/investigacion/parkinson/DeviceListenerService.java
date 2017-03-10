package es.us.investigacion.parkinson;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import es.us.investigacion.parkinson.database.Record;
import es.us.investigacion.parkinson.view.MainActivity;

/**
 * Created by LuisMiguel on 25/06/2016.
 */
public class DeviceListenerService extends WearableListenerService implements SensorEventListener {
    private static final int sensorDelay = 20;//SensorManager.SENSOR_DELAY_NORMAL;
    public static GoogleApiClient mGoogleApiClient;
    public static Node localNode;
    public static List<Node> remoteNodes;
    private static CopyOnWriteArrayList<DeviceServiceListener> listeners = new CopyOnWriteArrayList<>();
    private static boolean sessionRunning = false;
    private static SharedPreferences sharedPref;
    private static FileOutputStream fOut;
    private static BufferedOutputStream bufferOut;
    private static SensorManager mSensorManager;
    private static Date startTime;
    private static Timer timer;
    private static File file;
    private final IBinder mBinder = new ServiceBinder();
    public List<String> filesToSendPool;

    public static boolean isSessionRunning() {
        return sessionRunning;
    }

    public static void startSession(final Context context) {
        if (!isSessionRunning()) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, localNode.getId(), context.getString(R.string.path_prefix) + context.getString(R.string.start_session_local), new String().getBytes());
        }
    }

    public static void finishSession(final Context context) {
        if (isSessionRunning()) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, localNode.getId(), context.getString(R.string.path_prefix) + context.getString(R.string.stop_session_local), new String().getBytes());
        }
    }

    public static void addDeviceServiceListener(DeviceServiceListener listener) {
        if (listeners != null && listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeDeviceServiceListener(DeviceServiceListener listener) {
        if (listeners != null && listener != null) {
            listeners.remove(listener);
        }
    }

    public static void launchAppOnDevice(Context context) {
        if (mGoogleApiClient != null && remoteNodes != null) {
            for (Node node : remoteNodes) {
                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), context.getString(R.string.path_prefix) + context.getString(R.string.launch_app_phone), new String().getBytes());
            }
        }
    }

    public static void preferencesChanged(Context context) {
        if (mGoogleApiClient != null && remoteNodes != null) {
            for (Node node : remoteNodes) {
                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), context.getString(R.string.path_prefix) + context.getString(R.string.preferences_changed), new String(sharedPref.getBoolean(context.getString(R.string.preferences_saved_autosync), true) + context.getString(R.string.delimiter) + sharedPref.getInt(context.getString(R.string.preferences_saved_autosyncfreq), 0) + context.getString(R.string.delimiter) + sharedPref.getString(context.getString(R.string.preferences_saved_username), "") + context.getString(R.string.delimiter) + new String(sharedPref.getString(context.getString(R.string.preferences_saved_researcher), ""))).getBytes());
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        filesToSendPool = new ArrayList<>();
        connectToPhone();
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        connectToPhone();
        return super.bindService(service, conn, flags);
    }

    private void connectToPhone() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Toast.makeText(DeviceListenerService.this.getApplicationContext(), R.string.device_not_connected + connectionResult.toString(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                                @Override
                                public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                                    if (!getConnectedNodesResult.getNodes().isEmpty()) {
                                        remoteNodes = getConnectedNodesResult.getNodes();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .build();
            mGoogleApiClient.connect();
        }

        if (localNode == null) {
            Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
                @Override
                public void onResult(@NonNull NodeApi.GetLocalNodeResult getLocalNodeResult) {
                    localNode = getLocalNodeResult.getNode();
                }
            });
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.launch_app_wear))) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
            Wearable.MessageApi.sendMessage(mGoogleApiClient, messageEvent.getSourceNodeId(), getString(R.string.path_prefix) + getString(R.string.launch_app_wear_ack), new String().getBytes());
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.stop_session_local))) {
            finishSessionPrivate(messageEvent.getSourceNodeId());
            Wearable.MessageApi.sendMessage(mGoogleApiClient, messageEvent.getSourceNodeId(), getString(R.string.path_prefix) + getString(R.string.stop_session_local_ack), new String().getBytes());
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.start_session_local))) {
            startSessionPrivate(messageEvent.getSourceNodeId());
            Wearable.MessageApi.sendMessage(mGoogleApiClient, messageEvent.getSourceNodeId(), getString(R.string.path_prefix) + getString(R.string.start_session_local_ack), new String().getBytes());
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.launch_app_phone_ack))) {
            onSuccessAppLaunchedOnDevice(messageEvent.getSourceNodeId());
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.session_started_ack))) {
            onSuccessSessionStartedAck(messageEvent.getSourceNodeId());
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.session_stopped_ack))) {
            onSuccessSessionStoppedAck(messageEvent.getSourceNodeId());
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.record_data_map_ack))) {
            onSuccessSessionRecordDataAck(messageEvent.getSourceNodeId());
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.session_file_ack))) {
            String fileName = new String(messageEvent.getData());
            new File(fileName).delete();
            filesToSendPool.remove(fileName);
            sendFilesPool();
            onSuccessSessionFileAck(messageEvent.getSourceNodeId());
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.preferences_changed_ack))) {
            onSuccessPreferencesChangedAck(messageEvent.getSourceNodeId());
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.get_state))) {
            String state = getString(R.string.state_stopped);
            if (isSessionRunning())
                state = getString(R.string.state_running) + getString(R.string.delimiter) + startTime.getTime();

            if (mGoogleApiClient != null && remoteNodes != null) {
                for (Node node : remoteNodes) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), getString(R.string.path_prefix) + getString(R.string.get_state_response), state.getBytes());
                }
            }
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.new_event_received))) {
            String eventId = new String(messageEvent.getData());
            onNewEventReceived(eventId, messageEvent.getSourceNodeId());
            saveEventInFile(eventId);
            Wearable.MessageApi.sendMessage(mGoogleApiClient, messageEvent.getSourceNodeId(), getString(R.string.path_prefix) + getString(R.string.new_event_received_ack), eventId.getBytes());
        }
    }

    private void saveEventInFile(String eventId) {
        if (isSessionRunning() && fOut != null) {
            try {
                fOut.write((getString(R.string.event_value_prefix) + ";" + eventId + "\n").getBytes());
            } catch (IOException e) {
            }
        }
    }

    private void finishSessionPrivate(final String sourceNodeId) {
        sessionRunning = false;
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR));
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED));
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... objects) {
                    sendFileToPhone(file);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    long numberOfData = 0;
                    try {
                        LineNumberReader lineReader = new LineNumberReader(new FileReader(file));
                        lineReader.skip(Long.MAX_VALUE);
                        numberOfData = lineReader.getLineNumber() + 1;
                        lineReader.close();


                        Record record = new Record(sharedPref.getString(getString(R.string.preferences_saved_username), ""),
                                sharedPref.getString(getString(R.string.preferences_saved_researcher), ""),
                                startTime.getTime(), Calendar.getInstance().getTimeInMillis(), sensorDelay,
                                numberOfData, sharedPref.getBoolean(getString(R.string.preferences_saved_autosync), true),
                                sharedPref.getString(getString(R.string.preferences_saved_description), ""),
                                sharedPref.getString(getString(R.string.preferences_saved_location), ""),
                                Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID),
                                Build.MODEL, Integer.toString(Build.VERSION.SDK_INT));
                        record.save();
                        onNewRecord(record);
                        startTime = null;
                        PutDataMapRequest dataMap = PutDataMapRequest.create(getString(R.string.path_prefix) + getString(R.string.record_data_map));
                        record.putToDataMap(dataMap.getDataMap());
                        PutDataRequest request = dataMap.asPutDataRequest();
                        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                                .putDataItem(mGoogleApiClient, request);
                        onSessionFinished(sourceNodeId);
                    } catch (Exception e) {
                    }
                }
            }.execute();
        } else {
            onSessionFinished(sourceNodeId);
        }
        if (mGoogleApiClient != null && remoteNodes != null) {
            for (Node node : remoteNodes) {
                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), getString(R.string.path_prefix) + getString(R.string.session_stopped), new String(sharedPref.getString(getString(R.string.preferences_saved_username), "") + getString(R.string.session_stopped) + Calendar.getInstance().getTimeInMillis() + getString(R.string.session_stopped) + new String(sharedPref.getString(getString(R.string.preferences_saved_researcher), ""))).getBytes());
            }
        }
    }

    private void startSessionPrivate(final String sourceNodeId) {
        sessionRunning = true;
        startTime = Calendar.getInstance().getTime();
        createNewTempFile();

        mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        if (sharedPref.getBoolean(getString(R.string.preferences_saved_autosync), false)) {
            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    sendFileToPhone(file);
                }
            };
            timer.schedule(timerTask, sharedPref.getInt(getString(R.string.preferences_saved_autosyncfreq), 15000), sharedPref.getInt(getString(R.string.preferences_saved_autosyncfreq), 15000));
        }

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorDelay);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR), sensorDelay);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), sensorDelay);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorDelay);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED), sensorDelay);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), sensorDelay);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), sensorDelay);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), sensorDelay);
        if (mGoogleApiClient != null && remoteNodes != null) {
            for (Node node : remoteNodes) {
                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), getString(R.string.path_prefix) + getString(R.string.session_started), new String(sharedPref.getString(getString(R.string.preferences_saved_username), "") + getString(R.string.delimiter) + Calendar.getInstance().getTimeInMillis() + getString(R.string.delimiter) + new String(sharedPref.getString(getString(R.string.preferences_saved_researcher), ""))).getBytes());
            }
        }
        onSessionStarted(sourceNodeId);

    }

    private void createNewTempFile() {
        try {
            if (fOut != null) {
                bufferOut.close();
                fOut.flush();
                fOut.close();
            }
            file = File.createTempFile(getString(R.string.path_prefix) + Calendar.getInstance().getTimeInMillis(), getString(R.string.temp_file_extension), getApplicationContext().getCacheDir());
            fOut = new FileOutputStream(file);
            bufferOut = new BufferedOutputStream(fOut);
        } catch (IOException e) {
        }
    }

    public void onSensorChanged(SensorEvent event) {
        String st = "";
        try {

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    st = System.currentTimeMillis() + ";" + Sensor.TYPE_ACCELEROMETER + ";" + event.values[0] + ";" + event.values[1] + ";" + event.values[2] + "\n";
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    st = System.currentTimeMillis() + ";" + Sensor.TYPE_ROTATION_VECTOR + ";" + event.values[0] + ";" + event.values[1] + ";" + event.values[2] + ";" + event.values[3] + "\n";
                    break;
                case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                    st = System.currentTimeMillis() + ";" + Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR + ";" + event.values[0] + ";" + event.values[1] + ";" + event.values[2] + ";" + event.values[3] + "\n";
                    break;
                case Sensor.TYPE_GRAVITY:
                    st = System.currentTimeMillis() + ";" + Sensor.TYPE_GRAVITY + ";" + event.values[0] + ";" + event.values[1] + ";" + event.values[2] + "\n";
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    st = System.currentTimeMillis() + ";" + Sensor.TYPE_GYROSCOPE + ";" + event.values[0] + ";" + event.values[1] + ";" + event.values[2] + "\n";
                    break;
                case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                    st = System.currentTimeMillis() + ";" + Sensor.TYPE_GYROSCOPE_UNCALIBRATED + ";" + event.values[0] + ";" + event.values[1] + ";" + event.values[2] + ";" + event.values[3] + "\n";
                    break;
                case Sensor.TYPE_LIGHT:
                    st = System.currentTimeMillis() + ";" + Sensor.TYPE_LIGHT + ";" + event.values[0] + "\n";
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    st = System.currentTimeMillis() + ";" + Sensor.TYPE_LINEAR_ACCELERATION + ";" + event.values[0] + ";" + event.values[1] + ";" + event.values[2] + "\n";
                    break;
                default:
                    st = "";
            }
            if (!st.isEmpty())
                bufferOut.write(st.getBytes());
        } catch (Exception e) {
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void sendFilesPool() {
        FileInputStream fileInputStream = null;
        if (filesToSendPool.size() > 0) {
            File tempFile = new File(filesToSendPool.get(0));
            byte[] bFile = new byte[(int) tempFile.length()];
            try {
                fileInputStream = new FileInputStream(tempFile);
                fileInputStream.read(bFile);
                fileInputStream.close();
            } catch (Exception e) {
            }

            Asset asset = Asset.createFromBytes(bFile);
            PutDataMapRequest dataMap = PutDataMapRequest.create(getString(R.string.path_prefix) + getString(R.string.session_file));
            dataMap.getDataMap().putAsset(getString(R.string.session_file_asset), asset);
            dataMap.getDataMap().putString(getString(R.string.session_file_name), tempFile.getAbsolutePath());
            PutDataRequest request = dataMap.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(mGoogleApiClient, request);
        }
    }

    private void sendFileToPhone(final File file) {
        filesToSendPool.add(file.getAbsolutePath());
        createNewTempFile();
        sendFilesPool();
    }

    private void onSessionFinished(final String sourceNodeId) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onSessionFinished(sourceNodeId);
                    }
                }
            });
        }
    }

    private void onSessionStarted(final String sourceNodeId) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onSessionStarted(sourceNodeId);
                    }
                }
            });
        }
    }

    private void onSessionPaused(final String sourceNodeId) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onSessionPaused(sourceNodeId);
                    }
                }
            });
        }
    }

    private void onNewRecord(final Record record) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onNewRecord(record);
                    }
                }
            });
        }
    }

    private void onFileSentToPhone() {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onFileSentToPhone();
                    }
                }
            });
        }
    }

    private void onSuccessAppLaunchedOnDevice(final String sourceNodeId) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onSuccessAppLaunchedOnDevice(sourceNodeId);
                    }
                }
            });
        }
    }

    private void onSuccessSessionStartedAck(final String sourceNodeId) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onSuccessSessionStartedAck(sourceNodeId);
                    }
                }
            });
        }
    }

    private void onSuccessPreferencesChangedAck(final String sourceNodeId) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onSuccessPreferencesChangedAck(sourceNodeId);
                    }
                }
            });
        }
    }

    private void onSuccessSessionFileAck(final String sourceNodeId) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onSuccessSessionFileAck(sourceNodeId);
                    }
                }
            });
        }
    }

    private void onSuccessSessionRecordDataAck(final String sourceNodeId) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onSuccessSessionRecordDataAck(sourceNodeId);
                    }
                }
            });
        }
    }

    private void onSuccessSessionStoppedAck(final String sourceNodeId) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onSuccessSessionStoppedAck(sourceNodeId);
                    }
                }
            });
        }
    }

    private void onNewEventReceived(final String eventId, final String sourceNodeId) {
        if (listeners != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeviceServiceListener listener : listeners) {
                        if (listener != null)
                            listener.onNewEventReceived(eventId, sourceNodeId);
                    }
                }
            });
        }
    }

    public interface DeviceServiceListener {
        public void onSessionFinished(String sourceNodeId);

        public void onSessionStarted(String sourceNodeId);

        public void onSessionPaused(String sourceNodeId);

        public void onNewRecord(Record record);

        public void onFileSentToPhone();

        public void onSuccessAppLaunchedOnDevice(String sourceNodeId);

        public void onSuccessSessionStoppedAck(String sourceNodeId);

        public void onSuccessSessionRecordDataAck(String sourceNodeId);

        public void onSuccessSessionFileAck(String sourceNodeId);

        public void onSuccessPreferencesChangedAck(String sourceNodeId);

        public void onSuccessSessionStartedAck(String sourceNodeId);

        public void onNewEventReceived(String eventId, String sourceNodeId);
    }

    public class ServiceBinder extends Binder {
        public DeviceListenerService getServiceInstance() {
            return DeviceListenerService.this;
        }
    }
}
