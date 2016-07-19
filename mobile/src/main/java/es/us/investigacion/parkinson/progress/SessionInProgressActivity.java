package es.us.investigacion.parkinson.progress;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.WearManager;
import es.us.investigacion.parkinson.data.Event;
import es.us.investigacion.parkinson.data.EventInProgress;
import es.us.investigacion.parkinson.data.Record;
import es.us.investigacion.parkinson.records.RecordDataActivity;
import es.us.investigacion.parkinson.records.RecordDetailActivity;

public class SessionInProgressActivity extends AppCompatActivity implements WearManager.WearManagerListener {

    public static final int DETAIL_SESSION_RESULT = 523;

    public static RecyclerView.Adapter adapter;
    private static int sessionId;
    private static boolean isRunning;
    private static boolean waitingForFinish;
    private static boolean waitingForConnect;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout noEventsLayout;
    private LinearLayout startSessionAuxLayout;
    private LinearLayout linearPleaseWait;
    private FloatingActionMenu newEvent;
    private FloatingActionButton startSession;
    private TextView noEventsTextView;
    private FloatingActionButton saveSession;
    private FloatingActionButton stopSession;
    private TextView tvChronoHint;
    private Chronometer chrono;
    private ImageView imageNoEvents;
    private Record recordReceived;
    private TextView pleaseWaitText;
    private Timer timerPleaseWait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_in_progress);

        waitingForFinish = false;
        isRunning = false;
        waitingForConnect = false;
        sessionId = getIntent().getIntExtra(RecordDataActivity.RECORD_ID, -1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SessionInProgressActivity.this.setResult(RESULT_OK);
                    SessionInProgressActivity.this.finish();
                }
            });
        }

        final FloatingActionButton startSessionAux = (FloatingActionButton) findViewById(R.id.startSessionAux);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        frameLayout.getBackground().setAlpha(0);

        tvChronoHint = (TextView) findViewById(R.id.tvChronoHint);
        chrono = (Chronometer) findViewById(R.id.chrono);
        newEvent = (FloatingActionMenu) findViewById(R.id.newEvent);
        startSessionAuxLayout = (LinearLayout) findViewById(R.id.startSessionAuxLayout);
        linearPleaseWait = (LinearLayout) findViewById(R.id.frame_please_wait);
        startSession = (FloatingActionButton) findViewById(R.id.startSession);
        saveSession = (FloatingActionButton) findViewById(R.id.saveSession);
        stopSession = (FloatingActionButton) findViewById(R.id.stopSession);
        noEventsLayout = (LinearLayout) findViewById(R.id.noeventslayout);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefreshsession);
        noEventsTextView = (TextView) findViewById(R.id.no_events_registered_text);
        imageNoEvents = (ImageView) findViewById(R.id.image_no_events);
        pleaseWaitText = (TextView) findViewById(R.id.please_wait_text);

        final String[] eventTypes = getResources().getStringArray(R.array.event_types);
        final String[] eventNames = getResources().getStringArray(R.array.event_names);
        newEvent.setClosedOnTouchOutside(true);
        newEvent.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {
                    frameLayout.setVisibility(View.VISIBLE);
                    frameLayout.getBackground().setAlpha(140);
                    frameLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            newEvent.close(true);
                            frameLayout.setVisibility(View.GONE);
                            return true;
                        }
                    });
                } else {
                    frameLayout.getBackground().setAlpha(0);
                    frameLayout.setOnTouchListener(null);
                    frameLayout.setVisibility(View.GONE);
                }
            }
        });

        for (int i = 0; i < eventTypes.length; i++) {
            final com.github.clans.fab.FloatingActionButton eventButton = new com.github.clans.fab.FloatingActionButton(this);
            eventButton.setButtonSize(FloatingActionButton.SIZE_MINI);
            eventButton.setLabelText(eventNames[i]);
            eventButton.setImageResource(R.drawable.fab_add);
            eventButton.setColorNormalResId(android.R.color.holo_green_dark);
            eventButton.setColorPressedResId(android.R.color.holo_green_light);
            newEvent.addMenuButton(eventButton);
            final int pos = i;
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WearManager.sendEventToWear(SessionInProgressActivity.this, eventTypes[pos]);
                    newEvent.close(true);
                    EventInProgress event = new EventInProgress(eventTypes[pos], eventNames[pos], SystemClock.elapsedRealtime() - chrono.getBase(), Calendar.getInstance().getTimeInMillis());
                    event.save();
                }
            });
        }

        if (sessionId == -1) {
            adapter = new SessionInProgressAdapter(SessionInProgressActivity.this);
            WearManager.addListener(this);
            WearManager.getSessionState(this);
            timerPleaseWait = new Timer();
            timerPleaseWait.schedule(new TimerTask() {
                @Override
                public void run() {
                    SessionInProgressActivity.this.setResult(RESULT_CANCELED);
                    SessionInProgressActivity.this.finish();
                }
            }, 10000);
        } else {
            adapter = new EventsAdapter(SessionInProgressActivity.this, sessionId);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateEmptySpace();
                recyclerView.invalidate();
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
            }
        });

        saveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recordReceived != null) {
                    Intent intent = new Intent(SessionInProgressActivity.this, RecordDetailActivity.class);
                    intent.putExtra(RecordDataActivity.RECORD_ID, recordReceived.getId());
                    intent.putExtra(RecordDataActivity.RECORD_FROM_SESSION_IN_PROGRESS, true);
                    SessionInProgressActivity.this.startActivityForResult(intent, DETAIL_SESSION_RESULT);
                } else {
                    linearPleaseWait.setVisibility(View.VISIBLE);
                    pleaseWaitText.setText(R.string.session_please_wait);
                    waitingForFinish = true;
                    Snackbar.make(swipeRefresh, getString(R.string.session_please_wait), Snackbar.LENGTH_SHORT);
                }
            }
        });

        stopSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvChronoHint.setText(R.string.session_starting_view);
                tvChronoHint.setVisibility(View.VISIBLE);
                WearManager.stopSessionWear(SessionInProgressActivity.this);
            }
        });

        View.OnClickListener startClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvChronoHint.setText(R.string.session_finishing_view);
                tvChronoHint.setVisibility(View.VISIBLE);
                WearManager.startSessionWear(SessionInProgressActivity.this);
            }
        };
        startSession.setOnClickListener(startClickListener);
        startSessionAux.setOnClickListener(startClickListener);
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        SessionInProgressActivity.this.setResult(RESULT_OK);
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        if (isRunning) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.alert_dialog_in_progress_question)).setPositiveButton(getString(R.string.alert_dialog_in_progress_positive), dialogClickListener)
                    .setNegativeButton(getString(R.string.alert_dialog_in_progress_negative), dialogClickListener).show();
        } else {
            SessionInProgressActivity.this.setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WearManager.removeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionId == -1) {
            waitingForConnect = true;
            WearManager.connectAsync(this);
        }
        adapter.notifyDataSetChanged();
        updateSessionState(SessionStateAction.initial);
    }

    private void updateEmptySpace() {
        if (!waitingForConnect) {
            imageNoEvents.setImageResource(R.drawable.head_brain);
            if (adapter.getItemCount() == 0) {
                noEventsLayout.setVisibility(View.VISIBLE);
                swipeRefresh.setVisibility(View.GONE);
            } else {
                noEventsLayout.setVisibility(View.GONE);
                swipeRefresh.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void userSessionFinished(String userName) {
        updateSessionState(SessionStateAction.finish);
    }

    @Override
    public void userSessionStarted(String userName, Date date) {
    }

    @Override
    public void sessionStartedOnWear(boolean correct) {
        if (correct) {
            updateSessionState(SessionStateAction.start);
        } else {
            Snackbar.make(swipeRefresh, getString(R.string.session_launch_problem_snackbar), Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void sessionStoppedOnWear(boolean correct) {
        if (correct) {
            updateSessionState(SessionStateAction.finish);
        } else {
            Snackbar.make(swipeRefresh, getString(R.string.session_finish_problem_snackbar), Snackbar.LENGTH_SHORT);
        }
    }

    private void updateSessionState(SessionStateAction sessionState) {
        if (sessionState.compareTo(SessionStateAction.start) == 0 && !isRunning) {
            isRunning = true;
            chrono.setBase(SystemClock.elapsedRealtime());
            chrono.start();
            chrono.setVisibility(View.VISIBLE);
            tvChronoHint.setVisibility(View.GONE);
            saveSession.setVisibility(View.GONE);
            startSession.setVisibility(View.INVISIBLE);
            stopSession.setVisibility(View.VISIBLE);
            startSessionAuxLayout.setVisibility(View.GONE);
            newEvent.setVisibility(View.VISIBLE);
            noEventsTextView.setText(R.string.no_events_available_1);
            linearPleaseWait.setVisibility(View.GONE);
            updateEmptySpace();
            Snackbar.make(swipeRefresh, getString(R.string.session_launched_snackbar), Snackbar.LENGTH_SHORT);
        } else if (sessionState.compareTo(SessionStateAction.finish) == 0 && isRunning) {
            isRunning = false;
            chrono.stop();
            tvChronoHint.setText(R.string.session_finished);
            tvChronoHint.setVisibility(View.VISIBLE);
            saveSession.setVisibility(View.VISIBLE);
            startSession.setVisibility(View.INVISIBLE);
            stopSession.setVisibility(View.GONE);
            startSessionAuxLayout.setVisibility(View.GONE);
            newEvent.setVisibility(View.GONE);
            noEventsTextView.setText(R.string.no_events_available_4);
            linearPleaseWait.setVisibility(View.GONE);
            updateEmptySpace();
            Snackbar.make(swipeRefresh, getString(R.string.session_finished_snackbar), Snackbar.LENGTH_SHORT);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(R.string.title_activity_session_finished);
        } else if (sessionState.compareTo(SessionStateAction.initial) == 0) {
            isRunning = false;
            updateEmptySpace();
            chrono.setVisibility(View.GONE);
            chrono.stop();
            tvChronoHint.setVisibility(View.GONE);
            saveSession.setVisibility(View.GONE);
            stopSession.setVisibility(View.GONE);
            newEvent.setVisibility(View.GONE);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(R.string.title_activity_session_new);

            if (sessionId > -1) {
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.title_activity_session_stored);
                startSession.setVisibility(View.GONE);
                startSessionAuxLayout.setVisibility(View.GONE);
                linearPleaseWait.setVisibility(View.GONE);
                noEventsTextView.setText(R.string.no_events_available_5);
            } else if (!waitingForConnect) {
                startSession.setVisibility(View.VISIBLE);
                startSessionAuxLayout.setVisibility(View.VISIBLE);
                linearPleaseWait.setVisibility(View.GONE);
            } else if (waitingForConnect) {
                startSession.setVisibility(View.GONE);
                startSessionAuxLayout.setVisibility(View.GONE);
                linearPleaseWait.setVisibility(View.VISIBLE);
                pleaseWaitText.setText(R.string.session_please_wait_connecting);
            }
        } else if (sessionState.compareTo(SessionStateAction.resume) == 0) {
            isRunning = true;
            chrono.setVisibility(View.VISIBLE);
            chrono.start();
            tvChronoHint.setVisibility(View.GONE);
            saveSession.setVisibility(View.GONE);
            startSession.setVisibility(View.INVISIBLE);
            stopSession.setVisibility(View.VISIBLE);
            startSessionAuxLayout.setVisibility(View.GONE);
            newEvent.setVisibility(View.VISIBLE);
            noEventsTextView.setText(R.string.no_events_available_1);
            linearPleaseWait.setVisibility(View.GONE);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(R.string.title_activity_session_in_progress);
            updateEmptySpace();
            Snackbar.make(swipeRefresh, getString(R.string.session_already_running_snackbar), Snackbar.LENGTH_SHORT);
        } else if (sessionState.compareTo(SessionStateAction.disconnected) == 0) {
            if (sessionId == -1) {
                startSessionAuxLayout.setVisibility(View.GONE);
                noEventsTextView.setText(R.string.no_device_connected);
                noEventsLayout.setVisibility(View.VISIBLE);
                swipeRefresh.setVisibility(View.GONE);
                imageNoEvents.setImageResource(R.drawable.wear_icon);
                startSession.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void newStateReceived(String state, Date startTime) {
        waitingForConnect = false;
        timerPleaseWait.cancel();
        timerPleaseWait.purge();
        if (state.equals(getString(R.string.state_running))) {
            updateSessionState(SessionStateAction.resume);
            chrono.setBase(SystemClock.elapsedRealtime() - (Calendar.getInstance().getTimeInMillis() - startTime.getTime()));
        } else {
            updateSessionState(SessionStateAction.initial);
            removeAllEvents();
        }
        WearManager.launchWearApp(SessionInProgressActivity.this);
    }

    private void removeAllEvents() {
        for (EventInProgress event : new Select().from(EventInProgress.class).queryList()) {
            event.delete();
        }
    }

    @Override
    public void preferencesChanged(Boolean continuousSync, Integer frequency, String researcher) {

    }

    @Override
    public void eventReceivedInWear(String eventId) {

    }

    @Override
    public void onRecordReceived(Record record) {
        recordReceived = record;
        for (EventInProgress event : new Select().from(EventInProgress.class).queryList()) {
            Event newEvent = new Event(record.getId(), event);
            newEvent.save();
        }
        if (waitingForFinish) {
            Intent intent = new Intent(SessionInProgressActivity.this, RecordDetailActivity.class);
            intent.putExtra(RecordDataActivity.RECORD_ID, recordReceived.getId());
            intent.putExtra(RecordDataActivity.RECORD_FROM_SESSION_IN_PROGRESS, true);
            SessionInProgressActivity.this.startActivityForResult(intent, DETAIL_SESSION_RESULT);
        }
    }

    @Override
    public void onDeviceConnected(boolean success) {
        if (!success) {
            waitingForConnect = false;
            updateSessionState(SessionStateAction.disconnected);
        } else {
            WearManager.getSessionState(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SessionInProgressActivity.this.setResult(RESULT_OK);
        this.finish();
    }

    private enum SessionStateAction {initial, start, finish, resume, disconnected}
}
