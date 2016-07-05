package es.us.investigacion.parkinson.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;

/**
 * Created by LuisMiguel on 27/06/2016.
 */
@QueryModel(database = GemoDatabase.class)
public class RecordSimpleQuery extends BaseQueryModel {
    @Column
    int sessions;
    @Column
    String patient;

    public RecordSimpleQuery(int sessions, String patient) {
        this.sessions = sessions;
        this.patient = patient;
    }

    public RecordSimpleQuery() {
        this.sessions = 0;
        this.patient = "";
    }

    public int getSessions() {
        return sessions;
    }

    public void setSessions(int sessions) {
        this.sessions = sessions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecordSimpleQuery that = (RecordSimpleQuery) o;

        if (sessions != that.sessions) return false;
        return patient != null ? patient.equals(that.patient) : that.patient == null;

    }

    @Override
    public int hashCode() {
        int result = sessions;
        result = 31 * result + (patient != null ? patient.hashCode() : 0);
        return result;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    @Override
    public String toString() {
        return "RecordSimpleQuery{" +
                "sessions=" + sessions +
                ", patient='" + patient + '\'' +
                '}';
    }
}
