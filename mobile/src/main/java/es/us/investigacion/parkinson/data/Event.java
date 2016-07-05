package es.us.investigacion.parkinson.data;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.property.Property;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
@Table(database = GemoDatabase.class)
public class Event extends AuditEntity {

    @Column
    int sessionId;

    @Column
    String type;

    @Column
    String name;

    @Column
    long timeRelative;

    @Column
    long timeAbsolute;

    public Event() {
        this.type = "";
        this.name = "";
        this.timeRelative = 0;
        this.sessionId = 0;
        this.timeAbsolute = 0;
    }

    public Event(Event event) {
        this.type = event.getType();
        this.name = event.getName();
        this.sessionId = event.getSessionId();
        this.timeRelative = event.getTimeRelative();
        this.timeAbsolute = event.getTimeAbsolute();
    }

    public Event(int sessionId, EventInProgress eventInProgress) {
        this.sessionId = sessionId;
        this.type = eventInProgress.getType();
        this.name = eventInProgress.getName();
        this.timeRelative = eventInProgress.getTimeRelative();
        this.timeAbsolute = eventInProgress.getTimeAbsolute();
    }

    public Event(int sessionId, String type, String name, long timeRelative, long timeAbsolute) {
        this.sessionId = sessionId;
        this.type = type;
        this.name = name;
        this.timeRelative = timeRelative;
        this.timeAbsolute = timeAbsolute;
    }

    public static FlowCursorList<Event> getCurrentEventsBySessionId(int sessionId) {
        try {
            return new Select().from(Event.class).as("eventtable").join(new Select(new Property(Event.class, Method.max(Event_Table.modificationDate).getNameAlias()), Event_Table.id, Event_Table.parentId).from(Event.class).groupBy(Event_Table.parentId), Join.JoinType.INNER).as("maxtimestamp").on(ConditionGroup.clause().and(Condition.column(NameAlias.builder("maxtimestamp." + Event_Table.id).build()).eq(Condition.column(NameAlias.builder("eventtable." + Event_Table.id.getQuery()).build())))).where(Event_Table.sessionId.eq(sessionId)).and(Event_Table.isEntryDeleted.eq(false)).orderBy(Event_Table.timeAbsolute, false).cursorList();
        }catch (Exception e){
            return null;
        }
    }

    public static SQLCondition onlyNonDeleted() {
        return Event_Table.isEntryDeleted.is(false);
    }

    public int getId() {
        return id;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (id != event.id) return false;
        if (sessionId != event.sessionId) return false;
        if (timeRelative != event.timeRelative) return false;
        if (timeAbsolute != event.timeAbsolute) return false;
        if (type != null ? !type.equals(event.type) : event.type != null) return false;
        return name != null ? name.equals(event.name) : event.name == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + sessionId;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (timeRelative ^ (timeRelative >>> 32));
        result = 31 * result + (int) (timeAbsolute ^ (timeAbsolute >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", sessionId=" + sessionId +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", timeRelative=" + timeRelative +
                ", timeAbsolute=" + timeAbsolute +
                '}';
    }
}
