package com.beastpotato.cast.chromcastscheduler.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beastpotato.cast.chromcastscheduler.R;
import com.beastpotato.cast.chromcastscheduler.models.ScheduledItem;

import java.util.ArrayList;
import java.util.List;

public class LayoutItemRowAdapter extends RecyclerView.Adapter<LayoutItemRowAdapter.ViewHolder> {

    private List<ScheduledItem> objects = new ArrayList();
    private OnItemDeleteClickListener deleteClickListener;
    private OnItemClickListener onItemClickListener;

    public LayoutItemRowAdapter(List<ScheduledItem> items, OnItemDeleteClickListener deleteClickListener) {
        this.objects = items;
        this.deleteClickListener = deleteClickListener;
    }

    public void setData(List<ScheduledItem> items) {
        objects = items;
        notifyDataSetChanged();
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

    private void initializeViews(final ScheduledItem object, final ViewHolder holder) {
        holder.rowTime.setText(object.hour + ":" + object.minute);
        holder.rowName.setText(object.name);
        holder.rowDeviceName.setText(object.deciveName);
        holder.rowUrl.setText(object.url);
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteClickListener != null) {
                    deleteClickListener.onDelete(object);
                }
            }
        });
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(object, holder.view);
                }
            }
        });
    }

    public void setDeleteClickListener(OnItemDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemDeleteClickListener {
        void onDelete(ScheduledItem item);
    }

    public interface OnItemClickListener {
        void onItemClick(ScheduledItem item, View view);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private ImageView rowIcon, deleteIcon;
        private TextView rowTime;
        private TextView rowName;
        private TextView rowUrl;
        private TextView rowDeviceName;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            rowIcon = (ImageView) view.findViewById(R.id.row_icon);
            deleteIcon = (ImageView) view.findViewById(R.id.row_delete);
            rowTime = (TextView) view.findViewById(R.id.row_time);
            rowName = (TextView) view.findViewById(R.id.row_name);
            rowUrl = (TextView) view.findViewById(R.id.row_url);
            rowDeviceName = (TextView) view.findViewById(R.id.row_device_name);
        }
    }
}
