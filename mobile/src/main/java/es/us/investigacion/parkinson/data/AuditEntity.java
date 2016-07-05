package es.us.investigacion.parkinson.data;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Calendar;

/**
 * Created by LuisMiguel on 01/07/2016.
 */
@Table(database = GemoDatabase.class)
public class AuditEntity extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    int id;

    @Column
    int parentId;

    @Column
    boolean isEntryChanged;

    @Column
    boolean isEntryDeleted;

    @Column
    boolean isEntryAdded;

    @Column
    int entryServerId;

    @Column
    long modificationDate;

    protected AuditEntity() {
        this.isEntryChanged = false;
        this.isEntryAdded = false;
        this.isEntryDeleted = false;
        this.modificationDate = 0;
        this.entryServerId = 0;

    }

    @Override
    public void save() {
        setEntryAdded(true);
        setEntryChanged(false);
        setEntryDeleted(false);
        modificationDate = Calendar.getInstance().getTimeInMillis();
        super.insert();
        parentId = this.getId();
        super.update();
    }

    @Override
    public void update() {
        setEntryAdded(false);
        setEntryChanged(true);
        setEntryDeleted(false);
        modificationDate = Calendar.getInstance().getTimeInMillis();
        super.insert();
    }

    @Override
    public void delete() {
        setEntryAdded(false);
        setEntryChanged(false);
        setEntryDeleted(true);
        modificationDate = Calendar.getInstance().getTimeInMillis();
        super.insert();
    }

    public boolean isEntryChanged() {
        return isEntryChanged;
    }

    protected void setEntryChanged(boolean entryChanged) {
        isEntryChanged = entryChanged;
    }

    public boolean isEntryDeleted() {
        return isEntryDeleted;
    }

    protected void setEntryDeleted(boolean entryDeleted) {
        isEntryDeleted = entryDeleted;
    }

    public boolean isEntryAdded() {
        return isEntryAdded;
    }

    protected void setEntryAdded(boolean entryAdded) {
        isEntryAdded = entryAdded;
    }

    public int getEntryServerId() {
        return entryServerId;
    }

    public void setEntryServerId(int entryServerId) {
        this.entryServerId = entryServerId;
    }

    public long getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(long modificationDate) {
        this.modificationDate = modificationDate;
    }

    public int getParentId() {
        return parentId;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuditEntity that = (AuditEntity) o;

        if (id != that.id) return false;
        return entryServerId == that.entryServerId;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + entryServerId;
        return result;
    }

    @Override
    public String toString() {
        return "AuditEntity{" +
                "id=" + id +
                ", isEntryChanged=" + isEntryChanged +
                ", isEntryDeleted=" + isEntryDeleted +
                ", isEntryAdded=" + isEntryAdded +
                ", entryServerId=" + entryServerId +
                '}';
    }

    public enum AuditType {New, Changed, Deleted}
}
