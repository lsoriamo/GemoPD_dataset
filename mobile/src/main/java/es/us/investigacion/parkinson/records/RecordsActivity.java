package es.us.investigacion.parkinson.records;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.WearManager;
import es.us.investigacion.parkinson.data.Record;
import es.us.investigacion.parkinson.progress.SessionInProgressActivity;

public class RecordsActivity extends AppCompatActivity implements WearManager.WearManagerListener {

    public static final String HISTORY_PARENT_ID = "historyParentId";

    public static RecordsViewAdapter adapter;
    private SwipeRefreshLayout swiperefresh;
    private RecyclerView recyclerView;
    private LinearLayout nodatalayout;
    private ArrayList<Record> itemsList = new ArrayList<>();
    private String node;
    private LinearLayout frame_please_wait;
    private Timer timerPleaseWait;
    private Handler handlerPleaseWait;
    private int historyParentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        historyParentId = getIntent().getIntExtra(HISTORY_PARENT_ID, -1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecordsActivity.this.finish();
                }
            });
        }

        if (historyParentId > -1)
            toolbar.setTitle(R.string.change_history);

        nodatalayout = (LinearLayout) findViewById(R.id.nodatalayout);
        frame_please_wait = (LinearLayout) findViewById(R.id.frame_please_wait);
        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordsViewAdapter(RecordsActivity.this, historyParentId);
        recyclerView.setAdapter(adapter);
        handlerPleaseWait = new Handler();
        updateEmptySpace();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateEmptySpace();
                recyclerView.invalidate();
            }
        });

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.notifyDataSetChanged();
                swiperefresh.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (historyParentId == -1) {
            WearManager.addListener(this);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    WearManager.connectAsync(RecordsActivity.this);
                    Intent intent = new Intent(RecordsActivity.this, SessionInProgressActivity.class);
                    startActivityForResult(intent, 1);
                }
            });
            adapter.notifyDataSetChanged();
        }else{
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        WearManager.removeListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        WearManager.removeListener(this);
        super.onDestroy();
    }

    private void updateEmptySpace() {
        if (adapter.getItemCount() == 0) {
            nodatalayout.setVisibility(View.VISIBLE);
            swiperefresh.setVisibility(View.GONE);
        } else {
            nodatalayout.setVisibility(View.GONE);
            swiperefresh.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void userSessionFinished(String userName) {

    }

    @Override
    public void userSessionStarted(String userName, Date date) {

    }

    @Override
    public void sessionStartedOnWear(boolean correct) {

    }

    @Override
    public void sessionStoppedOnWear(boolean correct) {

    }

    @Override
    public void newStateReceived(String state, Date startTime) {

    }

    @Override
    public void preferencesChanged(Boolean continuousSync, Integer frequency, String researcher) {

    }

    @Override
    public void eventReceivedInWear(String eventId) {

    }

    @Override
    public void onRecordReceived(Record record) {

    }

    @Override
    public void onDeviceConnected(boolean success) {
        if (success) {
            WearManager.launchWearApp(RecordsActivity.this);
        }
        timerPleaseWait.cancel();
        timerPleaseWait.purge();
        frame_please_wait.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED){
            Snackbar.make(nodatalayout, getString(R.string.start_session_instructions_error), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
