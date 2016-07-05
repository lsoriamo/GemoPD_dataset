package es.us.investigacion.parkinson.view.record.detail;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.text.SimpleDateFormat;
import java.util.Date;

import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.database.Record;
import es.us.investigacion.parkinson.database.Record_Table;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
public class RecordDetailedFragment extends Fragment {

    public static final String RECORD_KEY = "record_key";
    private SimpleDateFormat sDateFormat;

    private TextView patientId;
    private TextView researcherId;
    private TextView startTimeId;
    private TextView dataSizeId;
    private BoxInsetLayout detailContent;

    private Record record;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.card_record_detail, container, false);
        sDateFormat = new SimpleDateFormat(getActivity().getString(R.string.date_format_card));
        patientId = (TextView) view.findViewById(R.id.tvPatient);
        researcherId = (TextView) view.findViewById(R.id.tvResearcher);
        startTimeId = (TextView) view.findViewById(R.id.tvStartTime);
        dataSizeId = (TextView) view.findViewById(R.id.tvDataSize);
        detailContent = (BoxInsetLayout) view.findViewById(R.id.detailContent);

        Bundle bundle = this.getArguments();
        int recordId = bundle.getInt(RECORD_KEY);
        record = new Select().from(Record.class).where(Record_Table.id.is(recordId)).querySingle();

        if (record != null) {
            researcherId.setText(record.getResearcher());
            patientId.setText(record.getPatient());
            Date date = new Date();
            date.setTime(record.getStartTime());
            startTimeId.setText(sDateFormat.format(date));
            dataSizeId.setText(Long.toString(record.getNumberOfData()));
            detailContent.setVisibility(View.VISIBLE);
        } else {
            detailContent.setVisibility(View.GONE);
        }

        return view;
    }
}
