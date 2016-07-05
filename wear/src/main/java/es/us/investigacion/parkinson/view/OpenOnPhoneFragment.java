package es.us.investigacion.parkinson.view;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.wearable.Wearable;

import es.us.investigacion.parkinson.DeviceListenerService;
import es.us.investigacion.parkinson.R;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
public class OpenOnPhoneFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_open_phone, container, false);
        super.onCreate(savedInstanceState);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
                startActivity(intent);
                DeviceListenerService.launchAppOnDevice(getActivity());
            }
        };

        LinearLayout openLayout = (LinearLayout) view.findViewById(R.id.openLayout);
        openLayout.setOnClickListener(onClickListener);

        return view;
    }
}
