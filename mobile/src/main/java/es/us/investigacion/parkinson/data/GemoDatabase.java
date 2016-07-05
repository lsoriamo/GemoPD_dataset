package es.us.investigacion.parkinson.data;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
@Database(name = GemoDatabase.NAME, version = GemoDatabase.VERSION)
public class GemoDatabase {
    public static final String NAME = "GemoPdDataBase";

    public static final int VERSION = 4;
}
