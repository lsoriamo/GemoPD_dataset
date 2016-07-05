package es.us.investigacion.parkinson.view.record.detail;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.Select;

import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.database.Record;

public class RecordListActivity extends Fragment implements WearableListView.ClickListener{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_record_list, container, false);
        super.onCreate(savedInstanceState);
        final TextView header = new TextView(getActivity());
        header.setText(R.string.record_list_header);
        WearableListView listView =
                (WearableListView) view.findViewById(R.id.wearable_list);
        BoxInsetLayout box = (BoxInsetLayout)view.findViewById(R.id.watch_view_record_list);
        box.addView(header,0);
        listView.addOnScrollListener(new WearableListView.OnScrollListener() {
            @Override
            public void onScroll(int i) {
                header.setY(header.getY() - i);
            }

            @Override
            public void onAbsoluteScrollChange(int i) {
            }

            @Override
            public void onScrollStateChanged(int i) {}

            @Override
            public void onCentralPositionChanged(int i) {}
        });
        listView.setAdapter(new RecordListAdapter(getActivity()));
        listView.setClickListener(this);
        listView.setGreedyTouchMode(true);
        return view;
    }

    @Override
    public void onClick(WearableListView.ViewHolder v) {
        String tagPatientSelected = (String) v.itemView.getTag();
        Intent intent = new Intent(getActivity(), RecordDetailedListActivity.class);
        intent.putExtra(RecordDetailedListActivity.RECORD_PATIENT, tagPatientSelected);
        startActivity(intent);
    }

    @Override
    public void onTopEmptyRegionClick() {
    }
}
