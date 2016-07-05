package es.us.investigacion.parkinson.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.view.ViewGroup;

import es.us.investigacion.parkinson.DeviceListenerService;
import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.database.Record;
import es.us.investigacion.parkinson.view.record.detail.RecordListActivity;

/**
 * Created by LuisMiguel on 23/06/2016.
 */
public class GridPagerAdapter extends FragmentGridPagerAdapter {

    private Context mContext;

    public GridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        Fragment fragment;
        if (row == 0 && col == 0) {
            fragment = new RecordFragment();
        } else if (row == 0 && col == 1) {
            fragment = new OpenOnPhoneFragment();
        } else if (row == 1 && col == 0) {
            fragment = new SettingsCardFragment();
        } else {
            fragment = new RecordListActivity();
        }
        return fragment;
    }

    @Override
    public int getRowCount() {
        return 3;
    }

    @Override
    public int getColumnCount(int rowNum) {
        if (rowNum == 0) {
            return 2;
        } else {
            return 1;
        }
    }
}
