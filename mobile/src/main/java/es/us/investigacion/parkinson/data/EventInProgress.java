package es.us.investigacion.parkinson.data;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
@Table(database = GemoDatabase.class)
public class EventInProgress extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    int id;

    @Column
    String type;

    @Column
    String name;

    @Column
    long timeRelative;

    @Column
    long timeAbsolute;

    public EventInProgress() {
        this.type = "";
        this.name = "";
        this.timeRelative = 0;
    }

    public EventInProgress(String type, String name, long timeRelative, long timeAbsolute) {
        this.type = type;
        this.name = name;
        this.timeRelative = timeRelative;
        this.timeAbsolute = timeAbsolute;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimeRelative() {
        return timeRelative;
    }

    public void setTimeRelative(long timeRelative) {
        this.timeRelative = timeRelative;
    }

    public long getTimeAbsolute() {
        return timeAbsolute;
    }

    public void setTimeAbsolute(long timeAbsolute) {
        this.timeAbsolute = timeAbsolute;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventInProgress that = (EventInProgress) o;

        if (id != that.id) return false;
        if (timeRelative != that.timeRelative) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (timeRelative ^ (timeRelative >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "EventInProgress{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", timeRelative=" + timeRelative +
                '}';
    }
}
