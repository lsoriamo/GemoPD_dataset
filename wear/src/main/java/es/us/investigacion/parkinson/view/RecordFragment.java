package es.us.investigacion.parkinson.view;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import es.us.investigacion.parkinson.DeviceListenerService;
import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.database.Record;

public class RecordFragment extends Fragment implements MainActivity.AmbientListener, DeviceListenerService.DeviceServiceListener {
    private TextView buttonText;
    private LinearLayout startButton;
    private LinearLayout delayed_confirmation_layout;
    private LinearLayout mainLayout;
    private DelayedConfirmationView delayed_confirmation;
    private boolean isQuestionStop = false;
    private Timer clickAgainTimer;

    @Override
    public void onResume() {
        super.onResume();
        delayed_confirmation_layout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);
        buttonText = (TextView) view.findViewById(R.id.buttonText);
        startButton = (LinearLayout) view.findViewById(R.id.startButton);
        mainLayout = (LinearLayout) view.findViewById(R.id.mainLayout);
        delayed_confirmation_layout = (LinearLayout) view.findViewById(R.id.delayed_confirmation_layout);
        delayed_confirmation = (DelayedConfirmationView) view.findViewById(R.id.delayed_confirmation);
        delayed_confirmation_layout.setVisibility(View.GONE);
        clickAgainTimer = new Timer();
        final Handler handler = new Handler();

        View.OnClickListener clickButton = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DeviceListenerService.isSessionRunning() && isQuestionStop) {
                    finishSession();
                    clickAgainTimer.cancel();
                    clickAgainTimer.purge();
                } else if(DeviceListenerService.isSessionRunning() && !isQuestionStop) {
                    isQuestionStop = true;
                    startButton.setBackgroundResource(android.R.color.holo_orange_dark);
                    buttonText.setText(R.string.click_again);
                    clickAgainTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            isQuestionStop = false;
                            handler.post(new Runnable() {
                                public void run() {
                                    setViewRecordInProgress(DeviceListenerService.isSessionRunning());
                                }
                            });
                        }
                    }, 5000);
                }else{
                    mainLayout.setVisibility(View.GONE);
                    delayed_confirmation_layout.setVisibility(View.VISIBLE);
                    delayed_confirmation.setTotalTimeMs(4000);
                    delayed_confirmation.setListener(new DelayedConfirmationView.DelayedConfirmationListener() {
                        @Override
                        public void onTimerFinished(View view) {
                            delayed_confirmation.reset();
                            Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
                            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                                    ConfirmationActivity.SUCCESS_ANIMATION);
                            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                                    getString(R.string.delayed_started));
                            startActivity(intent);
                            delayed_confirmation_layout.setVisibility(View.GONE);
                            mainLayout.setVisibility(View.VISIBLE);
                            startSession();
                        }

                        @Override
                        public void onTimerSelected(View view) {
                            delayed_confirmation.reset();
                            Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
                            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                                    ConfirmationActivity.FAILURE_ANIMATION);
                            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                                    getString(R.string.delayed_cancelled));
                            startActivity(intent);
                        }
                    });

                    delayed_confirmation.start();
                }
            }
        };

        setViewRecordInProgress(DeviceListenerService.isSessionRunning());

        startButton.setOnClickListener(clickButton);
        MainActivity.addAmbientListener(this);
        DeviceListenerService.addDeviceServiceListener(this);
        return view;
    }

    private void setViewRecordInProgress(boolean b) {
        if (b) {
            startButton.setBackgroundResource(R.color.dark_red);
            buttonText.setText(R.string.stop);
        }else{
            startButton.setBackgroundResource(R.color.dark_blue);
            buttonText.setText(R.string.start);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.removeAmbientListener(this);
        DeviceListenerService.removeDeviceServiceListener(this);
    }

    private void startSession() {
        DeviceListenerService.startSession(getActivity());
    }

    private void finishSession() {
        DeviceListenerService.finishSession(getActivity());
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        try {
            buttonText.setTextColor(Color.WHITE);
            buttonText.getPaint().setAntiAlias(false);
            startButton.setBackgroundResource(R.color.dark_grey);
            if (DeviceListenerService.isSessionRunning()) {
                buttonText.setText(R.string.running);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onExitAmbient() {
        try {
            buttonText.setTextColor(getActivity().getResources().getColor(R.color.primary_text_dark));
            buttonText.getPaint().setAntiAlias(true);
            setViewRecordInProgress(DeviceListenerService.isSessionRunning());
        } catch (Exception e) {
        }
    }

    @Override
    public void onSessionFinished(String sourceNodeId) {
        setViewRecordInProgress(false);
    }

    @Override
    public void onSessionStarted(String sourceNodeId) {
        setViewRecordInProgress(true);
    }

    @Override
    public void onSessionPaused(String sourceNodeId) {

    }

    @Override
    public void onNewRecord(Record record) {

    }

    @Override
    public void onFileSentToPhone() {

    }

    @Override
    public void onSuccessAppLaunchedOnDevice(String sourceNodeId) {

    }

    @Override
    public void onSuccessSessionStoppedAck(String sourceNodeId) {

    }

    @Override
    public void onSuccessSessionRecordDataAck(String sourceNodeId) {
        Toast.makeText(getActivity(), R.string.session_sync, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessSessionFileAck(String sourceNodeId) {

    }

    @Override
    public void onSuccessPreferencesChangedAck(String sourceNodeId) {

    }

    @Override
    public void onSuccessSessionStartedAck(String sourceNodeId) {

    }

    @Override
    public void onNewEventReceived(String eventId, String sourceNodeId) {
        Toast.makeText(getActivity(), R.string.new_event_received_view, Toast.LENGTH_SHORT).show();
    }
}
