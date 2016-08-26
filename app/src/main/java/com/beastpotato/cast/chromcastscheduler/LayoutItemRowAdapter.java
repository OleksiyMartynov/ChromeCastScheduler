package com.beastpotato.cast.chromcastscheduler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beastpotato.cast.chromcastscheduler.models.ScheduledItem;

import java.util.ArrayList;
import java.util.List;

public class LayoutItemRowAdapter extends RecyclerView.Adapter<LayoutItemRowAdapter.ViewHolder> {

    private List<ScheduledItem> objects = new ArrayList();
    private OnItemDeleteClickListener deleteClickListener;

    public LayoutItemRowAdapter(List<ScheduledItem> items, OnItemDeleteClickListener deleteClickListener) {
        this.objects = items;
        this.deleteClickListener = deleteClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        initializeViews(objects.get(position), holder);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    private void initializeViews(final ScheduledItem object, ViewHolder holder) {
        holder.rowTime.setText(object.hour + ":" + object.minute);
        holder.rowName.setText(object.name);
        holder.rowUrl.setText(object.url);
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteClickListener != null) {
                    deleteClickListener.onDelete(object);
                }
            }
        });
    }

    public void setDeleteClickListener(OnItemDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    public interface OnItemDeleteClickListener {
        void onDelete(ScheduledItem item);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView rowIcon, deleteIcon;
        private TextView rowTime;
        private TextView rowName;
        private TextView rowUrl;


        public ViewHolder(View view) {
            super(view);
            rowIcon = (ImageView) view.findViewById(R.id.row_icon);
            deleteIcon = (ImageView) view.findViewById(R.id.row_delete);
            rowTime = (TextView) view.findViewById(R.id.row_time);
            rowName = (TextView) view.findViewById(R.id.row_name);
            rowUrl = (TextView) view.findViewById(R.id.row_url);
        }
    }
}
