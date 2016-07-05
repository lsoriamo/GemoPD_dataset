package es.us.investigacion.parkinson.records;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.io.File;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.data.Record;
import es.us.investigacion.parkinson.data.Record_Table;
import es.us.investigacion.parkinson.progress.SessionInProgressActivity;

public class RecordDetailActivity extends AppCompatActivity {

    private static SimpleDateFormat simpleDateFormat;
    private static SimpleDateFormat simpleDateFormatFile;
    private Record record;
    private TextInputLayout[] textInputLayouts;
    private boolean editModeOn;
    private EditText[] editTextLayouts;
    private Switch switch_sync;
    private FloatingActionButton viewDataButton;
    private FloatingActionButton viewEventsButton;
    private FloatingActionButton editButton;
    private FloatingActionButton historyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecordDetailActivity.this.finish();
                }
            });
        }

        simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format_card));
        simpleDateFormatFile = new SimpleDateFormat(getString(R.string.date_format_file));
        editModeOn = false;
        int recordId = getIntent().getExtras().getInt(RecordDataActivity.RECORD_ID);
        final boolean fromSessionInProgress = getIntent().getBooleanExtra(RecordDataActivity.RECORD_FROM_SESSION_IN_PROGRESS, false);
        record = new Select().from(Record.class).where(Record_Table.id.is(recordId)).querySingle();

        if (record == null){
            Toast.makeText(RecordDetailActivity.this, R.string.record_not_exists, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(record.getPatient());

        viewDataButton = (FloatingActionButton) findViewById(R.id.viewdata);
        viewEventsButton = (FloatingActionButton) findViewById(R.id.viewevents);
        editButton = (FloatingActionButton) findViewById(R.id.fab);
        historyButton = (FloatingActionButton) findViewById(R.id.history);

        textInputLayouts = new TextInputLayout[15];
        editTextLayouts = new EditText[14];

        textInputLayouts[0] = (TextInputLayout) findViewById(R.id.input_layout_id);
        textInputLayouts[1] = (TextInputLayout) findViewById(R.id.input_layout_patient_id);
        textInputLayouts[2] = (TextInputLayout) findViewById(R.id.input_layout_patient_name);
        textInputLayouts[3] = (TextInputLayout) findViewById(R.id.input_layout_patient_surname);
        textInputLayouts[4] = (TextInputLayout) findViewById(R.id.input_layout_start);
        textInputLayouts[5] = (TextInputLayout) findViewById(R.id.input_layout_end);
        textInputLayouts[6] = (TextInputLayout) findViewById(R.id.input_layout_researcher);
        textInputLayouts[7] = (TextInputLayout) findViewById(R.id.input_layout_description);
        textInputLayouts[8] = (TextInputLayout) findViewById(R.id.input_layout_location);
        textInputLayouts[9] = (TextInputLayout) findViewById(R.id.input_layout_device_name);
        textInputLayouts[10] = (TextInputLayout) findViewById(R.id.input_layout_device_version);
        textInputLayouts[11] = (TextInputLayout) findViewById(R.id.input_layout_device_id);
        textInputLayouts[12] = (TextInputLayout) findViewById(R.id.input_layout_size);
        textInputLayouts[13] = (TextInputLayout) findViewById(R.id.input_layout_freq);
        textInputLayouts[14] = (TextInputLayout) findViewById(R.id.input_layout_sync);

        editTextLayouts[0] = (EditText) findViewById(R.id.edit_text_id);
        editTextLayouts[1] = (EditText) findViewById(R.id.edit_text_patient_id);
        editTextLayouts[2] = (EditText) findViewById(R.id.edit_text_patient_name);
        editTextLayouts[3] = (EditText) findViewById(R.id.edit_text_patient_surname);
        editTextLayouts[4] = (EditText) findViewById(R.id.edit_text_start);
        editTextLayouts[5] = (EditText) findViewById(R.id.edit_text_end);
        editTextLayouts[6] = (EditText) findViewById(R.id.edit_text_researcher);
        editTextLayouts[7] = (EditText) findViewById(R.id.edit_text_description);
        editTextLayouts[8] = (EditText) findViewById(R.id.edit_text_location);
        editTextLayouts[9] = (EditText) findViewById(R.id.edit_text_device_name);
        editTextLayouts[10] = (EditText) findViewById(R.id.edit_text_device_version);
        editTextLayouts[11] = (EditText) findViewById(R.id.edit_text_device_id);
        editTextLayouts[12] = (EditText) findViewById(R.id.edit_text_size);
        editTextLayouts[13] = (EditText) findViewById(R.id.edit_text_freq);

        switch_sync = (Switch) findViewById(R.id.switch_sync);

        setDatePickerDialog(editTextLayouts[4], textInputLayouts[4]);
        setDatePickerDialog(editTextLayouts[5], textInputLayouts[5]);

        updateViewFromRecord(false);
        viewDataButton.setVisibility(View.VISIBLE);
        viewDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewAccActivity = new Intent(RecordDetailActivity.this, RecordDataActivity.class);
                viewAccActivity.putExtra(RecordDataActivity.RECORD_PATIENT, record.getPatient());
                viewAccActivity.putExtra(RecordDataActivity.RECORD_DATE, record.getStartTime());
                startActivity(viewAccActivity);
            }
        });

        historyButton.setVisibility(View.VISIBLE);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewAccActivity = new Intent(RecordDetailActivity.this, RecordsActivity.class);
                viewAccActivity.putExtra(RecordsActivity.HISTORY_PARENT_ID, record.getParentId());
                startActivity(viewAccActivity);
            }
        });

        viewEventsButton.setVisibility(View.VISIBLE);
        viewEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewEventsActivity = new Intent(RecordDetailActivity.this, SessionInProgressActivity.class);
                viewEventsActivity.putExtra(RecordDataActivity.RECORD_ID, record.getParentId());
                startActivity(viewEventsActivity);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editModeOn) {
                    if (submitForm()) {
                        setEditMode(false);
                        Snackbar.make(view, getString(R.string.session_saved), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        if (fromSessionInProgress)
                            RecordDetailActivity.this.finish();
                    }
                } else {
                    setEditMode(true);
                }
            }
        });

        if (fromSessionInProgress) {
            setEditMode(true);
        }else{
            putAllEnabled(false);
        }
    }

    private void setEditMode(boolean state) {
        if (state) {
            viewDataButton.setVisibility(View.GONE);
            viewEventsButton.setVisibility(View.GONE);
            historyButton.setVisibility(View.GONE);
            editButton.setImageResource(android.R.drawable.ic_menu_save);
            editModeOn = true;
            putAllEnabled(true);
        }else{
            editModeOn = false;
            viewDataButton.setVisibility(View.VISIBLE);
            viewEventsButton.setVisibility(View.VISIBLE);
            historyButton.setVisibility(View.VISIBLE);
            putAllEnabled(false);
            editButton.setImageResource(android.R.drawable.ic_menu_edit);
        }
    }

    private void updateViewFromRecord(boolean enabled) {
        editTextLayouts[0].setText(Integer.toString(record.getParentId()));
        editTextLayouts[1].setText(record.getPatient());
        editTextLayouts[2].setText(record.getPatientName());
        editTextLayouts[3].setText(record.getPatientSurname());
        editTextLayouts[4].setText(simpleDateFormat.format(new Time(record.getStartTime())));
        editTextLayouts[5].setText(simpleDateFormat.format(new Time(record.getEndTime())));
        editTextLayouts[6].setText(record.getResearcher());
        editTextLayouts[7].setText(record.getDescription());
        editTextLayouts[8].setText(record.getLocation());
        editTextLayouts[9].setText(record.getDeviceName());
        editTextLayouts[10].setText(record.getDeviceVersion());
        editTextLayouts[11].setText(record.getDeviceId());
        editTextLayouts[12].setText(Long.toString(record.getNumberOfData()));
        editTextLayouts[13].setText(Integer.toString(record.getAccFrequency()));
        switch_sync.setChecked(record.isAutosync());

        if (!enabled) {
            for (EditText t : editTextLayouts) {
                if (t.getText().toString().isEmpty()) {
                    t.setText(R.string.hint_record_no_value);
                }
            }
        }
    }

    private void putAllEnabled(boolean enabled) {
        for (EditText t : editTextLayouts) {
            t.setEnabled(enabled);
        }
        editTextLayouts[0].setEnabled(false);
        switch_sync.setEnabled(enabled);
        updateViewFromRecord(enabled);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean submitForm() {
        boolean isCorrect = true;
        for (int i = 0; i < editTextLayouts.length; i++) {
            isCorrect = isCorrect || validateTextView(i);
        }
        if (isCorrect) {
            try {
                File file = null;
                try {
                    file = new File(getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath());
                    file = new File(file.getAbsolutePath() + getString(R.string.file_folder) + record.getPatient().trim() + "/" + getString(R.string.file_prefix) + simpleDateFormatFile.format(new Date(record.getStartTime())) + getString(R.string.file_extension));
                } catch (Exception ex) {
                    Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
                }
                record.setAccFrequency(Integer.parseInt(editTextLayouts[13].getText().toString()));
                record.setAutosync(switch_sync.isChecked());
                record.setDescription(editTextLayouts[7].getText().toString());
                record.setDeviceId(editTextLayouts[11].getText().toString());
                record.setDeviceName(editTextLayouts[9].getText().toString());
                record.setDeviceVersion(editTextLayouts[10].getText().toString());
                record.setEndTime(simpleDateFormat.parse(editTextLayouts[5].getText().toString()).getTime());
                record.setLocation(editTextLayouts[8].getText().toString());
                record.setNumberOfData(Integer.parseInt(editTextLayouts[12].getText().toString()));
                record.setPatient(editTextLayouts[1].getText().toString());
                record.setPatientName(editTextLayouts[2].getText().toString());
                record.setPatientSurname(editTextLayouts[3].getText().toString());
                record.setResearcher(editTextLayouts[6].getText().toString());
                record.setStartTime(simpleDateFormat.parse(editTextLayouts[4].getText().toString()).getTime());
                record.update();
                if (file != null) {
                    File fileDest = new File(getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath());
                    fileDest = new File(fileDest.getAbsolutePath() + getString(R.string.file_folder) + record.getPatient().trim() + "/");
                    if (!fileDest.exists()) {
                        fileDest.mkdirs();
                    }
                    fileDest = new File(fileDest, getString(R.string.file_prefix) + simpleDateFormatFile.format(new Date(record.getStartTime())) + getString(R.string.file_extension));
                    file.renameTo(fileDest);
                }
            } catch (ParseException p) {
                isCorrect = false;
            }
        }
        return isCorrect;
    }

    private boolean validateTextView(int index) {
        if (index == 13 || index == 5 || index == 12 || index == 1 || index == 6 || index == 4) {
            if (editTextLayouts[index].getText().toString().trim().isEmpty()) {
                textInputLayouts[index].setError(getString(R.string.err_msg_name_init) + " " + editTextLayouts[index].getHint().toString() + " " + getString(R.string.err_msg_name_end));
                requestFocus(editTextLayouts[index]);
                return false;
            }
        }

        if (index == 13 || index == 12) {
            try {
                Integer.parseInt(editTextLayouts[index].getText().toString());
            } catch (NumberFormatException e) {
                textInputLayouts[index].setError(getString(R.string.err_msg_name_init) + " " + editTextLayouts[index].getHint().toString() + " " + getString(R.string.err_msg_name_end_number));
                requestFocus(editTextLayouts[index]);
                return false;
            }
        }

        if (index == 5 || index == 4) {
            try {
                simpleDateFormat.parse(editTextLayouts[index].getText().toString()).getTime();
            } catch (ParseException p) {
                textInputLayouts[index].setError(getString(R.string.err_msg_name_init) + " " + editTextLayouts[index].getHint().toString() + " " + getString(R.string.err_msg_name_end_date));
                requestFocus(editTextLayouts[index]);
                return false;
            }
        }

        textInputLayouts[index].setErrorEnabled(false);
        return true;
    }

    private void setDatePickerDialog(final EditText editText, final TextInputLayout inputLayout) {
        final Calendar newDate = Calendar.getInstance();
        try {
            newDate.setTime(simpleDateFormat.parse(editText.getText().toString()));
        } catch (ParseException p) {
        }

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClickView) {
                datePickerClick(newDate, editText);
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    datePickerClick(newDate, editText);
                }
            }
        });
    }

    private void datePickerClick(final Calendar newDate, final EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(RecordDetailActivity.this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker datePicker, final int year, final int monthOfYear, final int dayOfMonth) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(RecordDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        newDate.set(year, monthOfYear, dayOfMonth, hour, minute);
                        editText.setText(simpleDateFormat.format(newDate.getTime()));
                    }
                }, newDate.get(Calendar.HOUR), newDate.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }

        }, newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;
        private int index;

        private MyTextWatcher(View view, int index) {
            this.view = view;
            this.index = index;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            validateTextView(index);
        }
    }

}
