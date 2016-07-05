package es.us.investigacion.parkinson;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import es.us.investigacion.parkinson.data.Record;
import es.us.investigacion.parkinson.records.RecordsActivity;

/**
 * Created by LuisMiguel on 22/06/2016.
 */
public class WearManager extends WearableListenerService implements DataApi.DataListener {
    private static CopyOnWriteArrayList<WearManagerListener> listeners = new CopyOnWriteArrayList<>();
    private static GoogleApiClient mGoogleApiClient;
    private static File file;
    private static SimpleDateFormat sDateFormat;
    private static String node;
    private static String localNode;

    public static void addListener(WearManagerListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public static void removeListener(WearManagerListener listener) {
        if (listeners != null && listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public static void connectAsync(final Context context) {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                updateConnectionNodes();
            }else{
                mGoogleApiClient.connect();
            }
        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            Toast.makeText(context.getApplicationContext(), context.getString(R.string.wear_connected),
                                    Toast.LENGTH_SHORT).show();
                            updateConnectionNodes();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            onDeviceConnected(false);
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            onDeviceConnected(false);
                            if (context != null) {
                                Toast.makeText(context, context.getString(R.string.wear_not_connected),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .build();
            mGoogleApiClient.connect();
        }
    }

    public static void launchWearApp(Context context) {
        if (mGoogleApiClient != null && localNode != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, localNode, context.getString(R.string.path_prefix) + context.getString(R.string.launch_app_wear), new String().getBytes());
        }
    }

    public static boolean isConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected() && node != null;
    }

    public static void getSessionState(Context context) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, context.getString(R.string.path_prefix) + context.getString(R.string.get_state), new String().getBytes());
    }

    public static void sendEventToWear(Context context, String eventId) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, context.getString(R.string.path_prefix) + context.getString(R.string.new_event_received), eventId.getBytes());
    }

    public static void stopSessionWear(Context context) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, context.getString(R.string.path_prefix) + context.getString(R.string.stop_session_local), new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        sessionStoppedOnWear(sendMessageResult.getStatus().isSuccess());
                    }
                }
        );
    }

    public static void startSessionWear(Context context) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, context.getString(R.string.path_prefix) + context.getString(R.string.start_session_local), new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        sessionStartedOnWear(sendMessageResult.getStatus().isSuccess());
                    }
                }
        );
    }

    private static void sessionStartedOnWear(final boolean correct) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (WearManagerListener listener : listeners) {
                    if (listener != null)
                        listener.sessionStartedOnWear(correct);
                }
            }
        });
    }

    private static void sessionStoppedOnWear(final boolean correct) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (WearManagerListener listener : listeners) {
                    if (listener != null)
                        listener.sessionStoppedOnWear(correct);
                }
            }
        });
    }

    private static void onDeviceConnected(final boolean success) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (WearManagerListener listener : listeners) {
                    if (listener != null)
                        listener.onDeviceConnected(success);
                }
            }
        });
    }

    private static void updateConnectionNodes() {
        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetLocalNodeResult getLocalNodeResult) {
                if (getLocalNodeResult.getNode() != null) {
                    localNode = getLocalNodeResult.getNode().getId();
                }
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        if (!getConnectedNodesResult.getNodes().isEmpty()) {
                            node = getConnectedNodesResult.getNodes().get(0).getId();
                            onDeviceConnected(true);
                        } else {
                            node = null;
                            onDeviceConnected(false);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sDateFormat = new SimpleDateFormat(getString(R.string.date_format_file));
        connectAsync(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectFromWear();
    }

    private void disconnectFromWear() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    private void createNewFile(String username, Date date) {
        File storage = new File(getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath());
        File dir = new File(storage.getAbsolutePath() + getString(R.string.file_folder) + username.trim() + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String dateStr = sDateFormat.format(date);
        file = new File(dir, getString(R.string.file_prefix) + dateStr + getString(R.string.file_extension));
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void onPeerDisconnected(Node node) {
        super.onPeerDisconnected(node);
        onDeviceConnected(false);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals(getString(R.string.path_prefix) + getString(R.string.session_file))) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset asset = dataMapItem.getDataMap().getAsset(getString(R.string.session_file_asset));
                final String fileName = dataMapItem.getDataMap().getString(getString(R.string.session_file_name));

                AsyncTask<Asset, Void, Boolean> asyncTask = new AsyncTask<Asset, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Asset... assets) {
                        try {
                            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                                    mGoogleApiClient, assets[0]).await().getInputStream();
                            if (assetInputStream == null) {
                                return false;
                            }

                            FileOutputStream fOut = new FileOutputStream(file, true);
                            int nRead;
                            byte[] data = new byte[16384];
                            while ((nRead = assetInputStream.read(data, 0, data.length)) != -1) {
                                fOut.write(data, 0, nRead);
                            }

                            fOut.flush();
                            fOut.close();
                            String[] paths = new String[1];
                            paths[0] = file.getAbsolutePath();
                            MediaScannerConnection.scanFile(WearManager.this, paths, null, null);
                        } catch (Exception e) {
                            return false;
                        }
                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean isCorrect) {
                        Wearable.MessageApi.sendMessage(
                                mGoogleApiClient, node, getString(R.string.path_prefix) + getString(R.string.session_file_ack), fileName.getBytes());
                        if (!isCorrect)
                            Toast.makeText(WearManager.this, R.string.error_creating_file, Toast.LENGTH_SHORT).show();
                    }
                };
                asyncTask.execute(asset);
            } else if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals(getString(R.string.path_prefix) + getString(R.string.record_data_map))) {
                try {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Record record = new Record(dataMapItem.getDataMap());
                    record.save();
                    onRecordReceived(record);
                    File storage = new File(getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath());
                    String dateStr = sDateFormat.format(new Date(record.getStartTime()));
                    File fileCopy = new File(storage.getAbsolutePath() + getString(R.string.file_folder) + record.getPatient().trim() + "/" + getString(R.string.file_prefix) + dateStr + getString(R.string.file_extension));
                    file.renameTo(fileCopy);
                    Toast.makeText(getApplicationContext(), getString(R.string.info_synchronized),
                            Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), getString(R.string.info_synchronized),
                            Toast.LENGTH_SHORT).show();
                }
                Wearable.MessageApi.sendMessage(
                        mGoogleApiClient, node, getString(R.string.path_prefix) + getString(R.string.record_data_map_ack), new String().getBytes());
            }
        }
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.preferences_changed))) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, getString(R.string.path_prefix) + getString(R.string.preferences_changed_ack), new String().getBytes());
            preferencesChanged(new String(messageEvent.getData()));
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.session_started))) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, getString(R.string.path_prefix) + getString(R.string.session_started_ack), new String().getBytes());
            userSessionStarted(new String(messageEvent.getData()));
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.session_stopped))) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, getString(R.string.path_prefix) + getString(R.string.session_stopped_ack), new String().getBytes());
            userSessionFinished(new String(messageEvent.getData()));
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.launch_app_phone))) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, getString(R.string.path_prefix) + getString(R.string.launch_app_phone_ack), new String().getBytes());
            launchMobileApp();
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.start_session_local_ack))) {

        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.stop_session_local_ack))) {

        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.new_event_received_ack))) {
            eventReceivedInWear(new String(messageEvent.getData()));
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.get_state_response))) {
            newStateReceived(new String(messageEvent.getData()));
        } else if (messageEvent.getPath().equals(getString(R.string.path_prefix) + getString(R.string.launch_app_wear))) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, node, getString(R.string.path_prefix) + getString(R.string.launch_app_wear), new String().getBytes());
        }
    }

    private void userSessionFinished(String data) {
        String[] dataSplit = data.split(getString(R.string.delimiter));
        final String username = dataSplit[0];
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (WearManagerListener listener : listeners) {
                    if (listener != null)
                        listener.userSessionFinished(username);
                }
            }
        });
    }

    private void userSessionStarted(String data) {
        String[] dataSplit = data.split(getString(R.string.delimiter));
        final String username = dataSplit[0];
        final Date date = Calendar.getInstance().getTime();
        date.setTime(Long.parseLong(dataSplit[1]));
        createNewFile(username, date);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (WearManagerListener listener : listeners) {
                    if (listener != null)
                        listener.userSessionStarted(username, date);
                }
            }
        });
    }

    private void preferencesChanged(String data) {
        String[] dataSplit = data.split(getString(R.string.delimiter));
        final Boolean continuousSync = Boolean.parseBoolean(dataSplit[0]);
        final Integer frequency = Integer.parseInt(dataSplit[1]);
        final String researcher = dataSplit[2];

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (WearManagerListener listener : listeners) {
                    if (listener != null)
                        listener.preferencesChanged(continuousSync, frequency, researcher);
                }
            }
        });
    }

    private void eventReceivedInWear(final String eventId) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (WearManagerListener listener : listeners) {
                    if (listener != null)
                        listener.eventReceivedInWear(eventId);
                }
            }
        });
    }

    private void onRecordReceived(final Record record) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (WearManagerListener listener : listeners) {
                    if (listener != null)
                        listener.onRecordReceived(record);
                }
            }
        });
    }

    private void newStateReceived(final String data) {
        String[] dataSplit = data.split(getString(R.string.delimiter));
        if (dataSplit.length > 0) {
            final String state = dataSplit[0];
            Date startTime = Calendar.getInstance().getTime();
            if (dataSplit.length > 1)
                startTime = new Date(Long.parseLong(dataSplit[1]));
            final Date finalStartTime = startTime;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (WearManagerListener listener : listeners) {
                        if (listener != null)
                            listener.newStateReceived(state, finalStartTime);
                    }
                }
            });
        }
    }

    private void launchMobileApp() {
        ActivityManager am = (ActivityManager) WearManager.this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);

        boolean isActivityFound = false;

        if (foregroundTaskInfo.topActivity.getPackageName().toString()
                .equalsIgnoreCase(this.getPackageName().toString())) {
            isActivityFound = true;
        }

        if (!isActivityFound) {
            Intent startIntent = new Intent(this, RecordsActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }

    //endregion


    public interface WearManagerListener {

        public void userSessionFinished(String userName);

        public void userSessionStarted(String userName, Date date);

        public void sessionStartedOnWear(boolean correct);

        public void sessionStoppedOnWear(boolean correct);

        public void newStateReceived(String state, Date startTime);

        public void preferencesChanged(Boolean continuousSync, Integer frequency, String researcher);

        public void eventReceivedInWear(String eventId);

        public void onRecordReceived(Record record);

        public void onDeviceConnected(boolean success);
    }
}
