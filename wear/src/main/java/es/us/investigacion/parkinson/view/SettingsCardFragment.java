package es.us.investigacion.parkinson.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import es.us.investigacion.parkinson.DeviceListenerService;
import es.us.investigacion.parkinson.R;

public class SettingsCardFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int SPEECH_REQUEST_CODE_PATIENT = 0;
    private static final int SPEECH_REQUEST_CODE_RESEARCHER = 1;
    private LinearLayout configLayout;
    private Switch switchAutoSync;
    private SharedPreferences sharedPref;
    private EditText patientId;
    private ImageView speechPatient;
    private EditText researcherId;
    private ImageView speechResearcher;
    private TextView connectionState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_config, container, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        configLayout = (LinearLayout) view.findViewById(R.id.configLayout);
        connectionState = (TextView) view.findViewById(R.id.connectionState);
        DeviceListenerService.mGoogleApiClient.registerConnectionCallbacks(this);
        DeviceListenerService.mGoogleApiClient.registerConnectionFailedListener(this);

        if (!DeviceListenerService.mGoogleApiClient.isConnected()) {
            connectionState.setText(R.string.pref_not_connected);
            connectionState.setTextColor(getResources().getColor(R.color.dark_red));
        }

        patientId = (EditText) view.findViewById(R.id.patientId);
        patientId.setText(sharedPref.getString(getString(R.string.preferences_saved_username), ""));
        speechPatient = (ImageView) view.findViewById(R.id.speechPatient);
        buildVoiceRecognitionOnTouch(SPEECH_REQUEST_CODE_PATIENT, patientId);
        buildVoiceRecognitionOnTouch(SPEECH_REQUEST_CODE_PATIENT, speechPatient);

        researcherId = (EditText) view.findViewById(R.id.researcherId);
        researcherId.setText(sharedPref.getString(getString(R.string.preferences_saved_researcher), ""));
        speechResearcher = (ImageView) view.findViewById(R.id.speechResearcher);
        buildVoiceRecognitionOnTouch(SPEECH_REQUEST_CODE_RESEARCHER, researcherId);
        buildVoiceRecognitionOnTouch(SPEECH_REQUEST_CODE_RESEARCHER, speechResearcher);


        switchAutoSync = (Switch) view.findViewById(R.id.switchAutoSync);
        switchAutoSync.setChecked(sharedPref.getBoolean(getString(R.string.preferences_saved_autosync), true));
        switchAutoSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.preferences_saved_autosync), b);
                editor.commit();
                preferencesChanged();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroy();
        DeviceListenerService.mGoogleApiClient.unregisterConnectionCallbacks(this);
        DeviceListenerService.mGoogleApiClient.unregisterConnectionFailedListener(this);
    }

    public void buildVoiceRecognitionOnTouch(final int requestCode, final View viewTouched) {
        viewTouched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, requestCode);
                viewTouched.requestFocus();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        connectionState.setText(R.string.pref_connected);
        connectionState.setTextColor(getResources().getColor(R.color.green));
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectionState.setText(R.string.pref_not_connected);
        connectionState.setTextColor(getResources().getColor(R.color.dark_red));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        connectionState.setText(R.string.pref_not_connected);
        connectionState.setTextColor(getResources().getColor(R.color.dark_red));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == SPEECH_REQUEST_CODE_PATIENT || requestCode == SPEECH_REQUEST_CODE_RESEARCHER) && resultCode == Activity.RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            SharedPreferences.Editor editor = sharedPref.edit();
            if (requestCode == SPEECH_REQUEST_CODE_PATIENT) {
                patientId.setText(spokenText);
                editor.putString(getString(R.string.preferences_saved_username), spokenText);
            } else {
                researcherId.setText(spokenText);
                editor.putString(getString(R.string.preferences_saved_researcher), spokenText);
            }
            editor.commit();
            preferencesChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void preferencesChanged() {
        DeviceListenerService.preferencesChanged(getActivity());
    }
}
