package es.us.investigacion.parkinson.records;

import android.content.Context;
import android.content.Intent;
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

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.daimajia.swipe.SwipeLayout;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import es.us.investigacion.parkinson.R;
import es.us.investigacion.parkinson.data.Record;

/**
 * Created by LuisMiguel on 24/06/2016.
 */
public class RecordsViewAdapter extends RecyclerView.Adapter<RecordsViewAdapter.RecordRowViewHolder> {
    private Context context;
    private FlowCursorList<Record> cursor;
    private Set<Integer> drawn;
    private int historyParentId = -1;

    public RecordsViewAdapter(Context context) {
        this.context = context;
        drawn = new HashSet<>();
        refreshData();
        FlowContentObserver observer = new FlowContentObserver();
        observer.registerForContentChanges(context, Record.class);
        observer.addModelChangeListener(new FlowContentObserver.OnModelStateChangedListener() {
            @Override
            public void onModelStateChanged(@Nullable Class<? extends Model> table, BaseModel.Action action, @NonNull SQLCondition[] primaryKeyValues) {
                refreshData();
            }
        });
    }

    public RecordsViewAdapter(Context context, int historyParentId) {
        this(context);
        this.historyParentId= historyParentId;
        refreshData();
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
    }

    public void refreshData() {
        new AsyncTask<Void, Void, FlowCursorList<Record>>() {
            @Override
            protected FlowCursorList<Record> doInBackground(Void... voids) {
                if (historyParentId == -1)
                    return Record.getCurrentSessions();
                else
                    return Record.getSessionHistory(historyParentId);
            }

            @Override
            protected void onPostExecute(FlowCursorList<Record> result) {
                if (result != null) {
                    RecordsViewAdapter.this.cursor = result;
                    RecordsViewAdapter.this.notifyDataSetChanged();
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
    public RecordRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.list_row_record, null);
        RecordRowViewHolder viewHolder = new RecordRowViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecordRowViewHolder rowViewHolder, final int position) {
        final Record item = cursor.getItem(position);
        rowViewHolder.patient.setText(item.getPatient());
        Date date = new Date(item.getStartTime());
        if (historyParentId > -1){
            date = new Date(item.getModificationDate());
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.date_format_card));
        rowViewHolder.date.setText(simpleDateFormat.format(date));
        View.OnClickListener viewDetailsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RecordDetailActivity.class);
                intent.putExtra(RecordDataActivity.RECORD_PARENT_ID, item.getParentId());
                intent.putExtra(RecordDataActivity.RECORD_ID, item.getId());
                context.startActivity(intent);

            }
        };

        if (historyParentId == -1) {
            rowViewHolder.swipeLayout.setEnabled(true);
            rowViewHolder.swipeLayout.getSurfaceView().setOnClickListener(viewDetailsClickListener);
            rowViewHolder.optionView.setOnClickListener(viewDetailsClickListener);

            rowViewHolder.optionRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawn.remove(item.getId());
                    playRemoveAnimation(rowViewHolder, position);
                    Snackbar.make(view, R.string.session_deleted, Snackbar.LENGTH_LONG).
                            setAction(R.string.undo_action, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }

                            }).setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            switch (event) {
                                case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                    playLoadAnimation(rowViewHolder, item);
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
        }else{
            rowViewHolder.swipeLayout.setEnabled(false);
        }

        final ColorGenerator generator = ColorGenerator.MATERIAL;
        String firstLetter = "U";

        if (historyParentId == -1) {
            if (item.getPatient().length() > 0) {
                firstLetter = item.getPatient().substring(0, 1);
            }
        }else{
            if (item.isEntryAdded())
                firstLetter = "A";
            else if (item.isEntryChanged())
                firstLetter = "C";
            else if (item.isEntryDeleted())
                firstLetter = "E";
        }

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .width(50)
                .height(50)
                .endConfig()
                .buildRound(firstLetter, generator.getColor(firstLetter));
        rowViewHolder.imageView.setImageDrawable(drawable);
        rowViewHolder.setIsRecyclable(true);
        playLoadAnimation(rowViewHolder, item);
    }

    public void playLoadAnimation(RecordRowViewHolder rowViewHolder, Record item) {
        if (!drawn.contains(item.getId())) {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.record_list_animation);
            anim.setDuration(1000);
            rowViewHolder.itemView.startAnimation(anim);
            drawn.add(item.getId());
        }
    }

    public void playRemoveAnimation(final RecordRowViewHolder rowViewHolder, int position) {
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
    }

    @Override
    public void onViewDetachedFromWindow(RecordRowViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        ((RecordRowViewHolder) holder).itemView.clearAnimation();
    }

    @Override
    public void onViewAttachedToWindow(RecordRowViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    public class RecordRowViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView patient;
        public TextView date;
        public SwipeLayout swipeLayout;
        public LinearLayout optionRemove;
        public LinearLayout optionView;
        public LinearLayout wrapper;

        public RecordRowViewHolder(View view) {
            super(view);
            this.patient = (TextView) view.findViewById(R.id.patient);
            this.imageView = (ImageView) view.findViewById(R.id.image);
            this.date = (TextView) view.findViewById(R.id.date);
            this.swipeLayout = (SwipeLayout) view.findViewById(R.id.swipeLayout);
            this.wrapper = (LinearLayout) view.findViewById(R.id.bottom_wrapper);
            this.optionView = (LinearLayout) view.findViewById(R.id.option_view);
            this.optionRemove = (LinearLayout) view.findViewById(R.id.option_remove);
        }
    }

}
