package es.us.investigacion.parkinson.progress;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.text.SimpleDateFormat;
import java.util.Date;

import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.data.AuditEntity;
import es.us.investigacion.parkinson.data.Event;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventRowViewHolder> {
    private Context context;
    private FlowCursorList<Event> cursor;
    private int lastPosition = -1;
    private int sessionId = -1;

    public EventsAdapter(Context context, Integer sessionId) {
        this.context = context;
        this.sessionId = sessionId;
        refreshData();
        FlowContentObserver observer = new FlowContentObserver();
        observer.registerForContentChanges(context, Event.class);
        observer.addModelChangeListener(new FlowContentObserver.OnModelStateChangedListener() {
            @Override
            public void onModelStateChanged(@Nullable Class<? extends Model> table, BaseModel.Action action, @NonNull SQLCondition[] primaryKeyValues) {
                refreshData();
            }
        });
    }

    public void refreshData() {
        new AsyncTask<Void, Void, FlowCursorList<Event>>() {
            @Override
            protected FlowCursorList<Event> doInBackground(Void... voids) {
                return Event.getCurrentEventsBySessionId(sessionId);
            }

            @Override
            protected void onPostExecute(FlowCursorList<Event> result) {
                if (result != null) {
                    EventsAdapter.this.cursor = result;
                    EventsAdapter.this.notifyDataSetChanged();
                }
            }
        }.execute();

    }

    @Override
    public int getItemCount() {
        if (cursor == null) {
            return 0;
        } else {
            return cursor.getCount();
        }
    }

    @Override
    public EventRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.list_row_event, null);
        EventRowViewHolder viewHolder = new EventRowViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final EventRowViewHolder rowViewHolder, final int position) {
        final Event item = cursor.getItem(position);
        rowViewHolder.event_name.setText(item.getName());
        Date date = new Date(item.getTimeAbsolute());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.date_format_card));
        rowViewHolder.time.setText(simpleDateFormat.format(date));

        rowViewHolder.optionRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRemoveAnimation(rowViewHolder, -1);
                Snackbar.make(view, R.string.event_deleted, Snackbar.LENGTH_LONG).
                        setAction(R.string.undo_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }

                        }).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        switch (event) {
                            case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                setLoadAnimation(rowViewHolder, -1);
                                rowViewHolder.swipeLayout.setVisibility(View.VISIBLE);
                                break;
                            default:
                                item.delete();
                                refreshData();
                                break;
                        }
                    }
                }).show();
            }
        });

        rowViewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        rowViewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, rowViewHolder.wrapper);
        setLoadAnimation(rowViewHolder, position);
    }

    private void setLoadAnimation(EventRowViewHolder rowViewHolder, int position) {
        if (position > lastPosition || position == -1) {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.record_list_animation);
            anim.setDuration(1000);
            rowViewHolder.itemView.startAnimation(anim);
            if (position > -1)
                lastPosition = position;
        }
    }

    private void setRemoveAnimation(final EventRowViewHolder rowViewHolder, int position) {
        if (position > lastPosition || position == -1) {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.record_list_animation_delete);
            anim.setDuration(500);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    rowViewHolder.swipeLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rowViewHolder.itemView.startAnimation(anim);
            if (position > -1)
                lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(EventRowViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        ((EventRowViewHolder) holder).itemView.clearAnimation();
    }

    public class EventRowViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView event_name;
        public TextView time;
        public SwipeLayout swipeLayout;
        public LinearLayout optionRemove;
        public LinearLayout wrapper;

        public EventRowViewHolder(View view) {
            super(view);
            this.event_name = (TextView) view.findViewById(R.id.event_name);
            this.imageView = (ImageView) view.findViewById(R.id.image);
            this.time = (TextView) view.findViewById(R.id.time);
            this.swipeLayout = (SwipeLayout) view.findViewById(R.id.swipeLayout);
            this.wrapper = (LinearLayout) view.findViewById(R.id.bottom_wrapper);
            this.optionRemove = (LinearLayout) view.findViewById(R.id.option_remove);
        }
    }

}
