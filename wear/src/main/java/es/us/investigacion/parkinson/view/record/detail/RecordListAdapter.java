package es.us.investigacion.parkinson.view.record.detail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.database.Record;
import es.us.investigacion.parkinson.database.RecordSimpleQuery;
import es.us.investigacion.parkinson.database.Record_Table;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
public class RecordListAdapter extends WearableListView.Adapter {

    private final Context mContext;
    private List<RecordSimpleQuery> mDataset;


    public RecordListAdapter(Context context) {
        this.mContext = context;
        refreshData();
        FlowContentObserver observer = new FlowContentObserver();
        observer.registerForContentChanges(context, Record.class);
        observer.addModelChangeListener(new FlowContentObserver.OnModelStateChangedListener() {
            @Override
            public void onModelStateChanged(@Nullable Class<? extends Model> table, BaseModel.Action action, @NonNull SQLCondition[] primaryKeyValues) {
                refreshData();
            }
        });
    }

    private void refreshData() {
        this.mDataset = new Select(Method.count(Record_Table.patient).as("sessions"), Record_Table.patient).from(Record.class).groupBy(Record_Table.patient).queryCustomList(RecordSimpleQuery.class);
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        return new WearableListView.ViewHolder(new RecordViewHolder(mContext));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder,
                                 int position) {
        RecordViewHolder itemHolder = (RecordViewHolder) viewHolder.itemView;
        TextView patient  = (TextView) itemHolder.findViewById(R.id.name);
        TextView numSessions  = (TextView) itemHolder.findViewById(R.id.sessions);
        patient.setText(mDataset.get(position).getPatient());
        numSessions.setText(Integer.toString(mDataset.get(position).getSessions()));

        final ImageView imageView = (ImageView) itemHolder.findViewById(R.id.circleSelector);
        imageView.setImageResource(R.drawable.circle_with_border_unselected);
        viewHolder.itemView.setTag(mDataset.get(position).getPatient());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class RecordViewHolder extends LinearLayout implements WearableListView.OnCenterProximityListener {
        private TextView name;
        private TextView sessions;
        private ImageView circleSelector;

        public RecordViewHolder(Context context) {
            super(context);
            View.inflate(context, R.layout.activity_record_list_item, this);
            name = (TextView) findViewById(R.id.name);
            sessions = (TextView) findViewById(R.id.sessions);
            circleSelector = (ImageView) findViewById(R.id.circleSelector);
        }

        @Override
        public void onCenterPosition(boolean b) {
            circleSelector.animate().scaleX(1f).scaleY(1f).alpha(1);
            circleSelector.setImageResource(R.drawable.circle_with_border_selected);
        }

        @Override
        public void onNonCenterPosition(boolean b) {
            circleSelector.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
            circleSelector.setImageResource(R.drawable.circle_with_border_unselected);
        }
    }
}
