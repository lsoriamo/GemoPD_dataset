package es.us.investigacion.parkinson.data;

import com.google.android.gms.wearable.DataMap;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.property.Property;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
@Table(database = GemoDatabase.class)
public class Record extends AuditEntity {

    @Column
    String patient;

    @Column
    String patientName;

    @Column
    String patientSurname;

    @Column
    String researcher;

    @Column
    long startTime;

    @Column
    long endTime;

    @Column
    int accFrequency;

    @Column
    long numberOfData;

    @Column
    boolean autosync;

    @Column
    String description;

    @Column
    String location;

    @Column
    String deviceId;

    @Column
    String deviceName;

    @Column
    String deviceVersion;

    public Record(String patient, String patientName, String patientSurname, String researcher, long startTime, long endTime, int accFrequency, long numberOfData, boolean autosync, String description, String location, String deviceId, String deviceName, String deviceVersion) {
        this.patient = patient;
        this.patientName = patientName;
        this.patientSurname = patientSurname;
        this.researcher = researcher;
        this.startTime = startTime;
        this.endTime = endTime;
        this.accFrequency = accFrequency;
        this.numberOfData = numberOfData;
        this.autosync = autosync;
        this.description = description;
        this.location = location;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceVersion = deviceVersion;
    }

    public Record() {
        this.patient = "";
        this.patientName = "";
        this.patientSurname = "";
        this.researcher = "";
        this.startTime = 0;
        this.endTime = 0;
        this.accFrequency = -1;
        this.numberOfData = -1;
        this.autosync = false;
        this.description = "";
        this.location = "";
        this.deviceId = "";
        this.deviceName = "";
        this.deviceVersion = "";
    }

    public Record(DataMap map) {
        this(map.getString("patient"),
                map.getString("patientName"),
                map.getString("patientSurname"),
                map.getString("researcher"),
                map.getLong("startTime"),
                map.getLong("endTime"),
                map.getInt("accFrequency"),
                map.getLong("numberOfData"),
                map.getBoolean("autosync"),
                map.getString("description"),
                map.getString("location"),
                map.getString("deviceId"),
                map.getString("deviceName"),
                map.getString("deviceVersion"));
    }

    public static FlowCursorList<Record> getCurrentSessions() {
        try {
            return new Select().from(Record.class).as("recordtable").join(new Select(new Property(Record.class, Method.max(Record_Table.modificationDate).getNameAlias()), Record_Table.id, Record_Table.parentId).from(Record.class).groupBy(Record_Table.parentId), Join.JoinType.INNER).as("maxtimestamp").on(ConditionGroup.clause().and(Condition.column(NameAlias.builder("maxtimestamp." + Record_Table.id).build()).eq(Condition.column(NameAlias.builder("recordtable." + Record_Table.id.getQuery()).build())))).where(Record_Table.isEntryDeleted.eq(false)).orderBy(Record_Table.startTime, false).cursorList();
        }catch (Exception e){
            return null;
        }
    }

    public static FlowCursorList<Record> getSessionHistory(int parentId) {
        try {
            return new Select().from(Record.class).where(Record_Table.parentId.eq(parentId)).orderBy(Record_Table.startTime, false).cursorList();
        }catch (Exception e){
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getResearcher() {
        return researcher;
    }

    public void setResearcher(String researcher) {
        this.researcher = researcher;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getAccFrequency() {
        return accFrequency;
    }

    public void setAccFrequency(int accFrequency) {
        this.accFrequency = accFrequency;
    }

    public long getNumberOfData() {
        return numberOfData;
    }

    public void setNumberOfData(long numberOfData) {
        this.numberOfData = numberOfData;
    }

    public boolean isAutosync() {
        return autosync;
    }

    public void setAutosync(boolean autosync) {
        this.autosync = autosync;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientSurname() {
        return patientSurname;
    }

    public void setPatientSurname(String patientSurname) {
        this.patientSurname = patientSurname;
    }

    public DataMap putToDataMap(DataMap map) {
        map.putString("patient", patient);
        map.putString("patientName", patientName);
        map.putString("patientSurname", patientSurname);
        map.putString("researcher", researcher);
        map.putLong("startTime", startTime);
        map.putLong("endTime", endTime);
        map.putInt("accFrequency", accFrequency);
        map.putLong("numberOfData", numberOfData);
        map.putBoolean("autosync", autosync);
        map.putString("description", description);
        map.putString("location", location);
        map.putString("deviceId", deviceId);
        map.putString("deviceName", deviceName);
        map.putString("deviceVersion", deviceVersion);

        return map;
    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", patient='" + patient + '\'' +
                ", patientName='" + patientName + '\'' +
                ", patientSurname='" + patientSurname + '\'' +
                ", researcher='" + researcher + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", accFrequency=" + accFrequency +
                ", numberOfData=" + numberOfData +
                ", autosync=" + autosync +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Record record = (Record) o;

        if (id != record.id) return false;
        if (startTime != record.startTime) return false;
        if (endTime != record.endTime) return false;
        if (accFrequency != record.accFrequency) return false;
        if (numberOfData != record.numberOfData) return false;
        if (autosync != record.autosync) return false;
        if (patient != null ? !patient.equals(record.patient) : record.patient != null)
            return false;
        if (researcher != null ? !researcher.equals(record.researcher) : record.researcher != null)
            return false;
        if (description != null ? !description.equals(record.description) : record.description != null)
            return false;
        if (location != null ? !location.equals(record.location) : record.location != null)
            return false;
        if (deviceId != null ? !deviceId.equals(record.deviceId) : record.deviceId != null)
            return false;
        return deviceName != null ? deviceName.equals(record.deviceName) : record.deviceName == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (patient != null ? patient.hashCode() : 0);
        result = 31 * result + (researcher != null ? researcher.hashCode() : 0);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (endTime ^ (endTime >>> 32));
        result = 31 * result + accFrequency;
        result = 31 * result + (int) (numberOfData ^ (numberOfData >>> 32));
        result = 31 * result + (autosync ? 1 : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        return result;
    }
}
