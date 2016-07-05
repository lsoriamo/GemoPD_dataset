package es.us.investigacion.parkinson.view.record.detail;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.database.Record;
import es.us.investigacion.parkinson.database.Record_Table;

public class RecordDetailedListActivity extends WearableActivity {
    public static final String RECORD_PATIENT = "record_patient";
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail_list);
        final GridViewPager pager = (GridViewPager) findViewById(R.id.grid_view_sessions);
        String patient = getIntent().getStringExtra(RECORD_PATIENT);
        List<Record> sessions = new Select(Record_Table.id).from(Record.class).where(Record_Table.patient.eq(patient)).queryList();
        pager.setAdapter(new SampleGridPagerAdapter(this, getFragmentManager(), sessions));
    }

    public class SampleGridPagerAdapter extends FragmentGridPagerAdapter {

        private final Context mContext;
        private List<Record> mSessionIds;

        public SampleGridPagerAdapter(Context ctx, FragmentManager fm, List<Record> mSessionIds) {
            super(fm);
            this.mContext = ctx;
            this.mSessionIds = mSessionIds;
        }

        @Override
        public Fragment getFragment(int row, int column) {
            Bundle bundle = new Bundle();
            bundle.putInt(RecordDetailedFragment.RECORD_KEY, mSessionIds.get(row).getId());
            Fragment fragment = new RecordDetailedFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getRowCount() {
            return mSessionIds.size();
        }

        @Override
        public int getColumnCount(int i) {
            return 1;
        }
    }

}
