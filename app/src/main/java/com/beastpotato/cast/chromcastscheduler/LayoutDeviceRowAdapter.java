package com.beastpotato.cast.chromcastscheduler;

import android.support.v7.media.MediaRouter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LayoutDeviceRowAdapter extends RecyclerView.Adapter<LayoutDeviceRowAdapter.ViewHolder> {

    private List<MediaRouter.RouteInfo> objects = new ArrayList<>();
    private MediaRouter.RouteInfo selectedRoute;
    private OnDeviceSelectedListener deviceSelectedListener;
    private boolean ignoreCheckChange = false;

    public LayoutDeviceRowAdapter(List<MediaRouter.RouteInfo> items) {
        objects = items;
    }

    public void setDeviceSelectedListener(OnDeviceSelectedListener deviceSelectedListener) {
        this.deviceSelectedListener = deviceSelectedListener;
    }

    private void initializeViews(final MediaRouter.RouteInfo object, ViewHolder holder) {
        holder.deviceName.setText(object.getName());
        ignoreCheckChange = true;
        holder.deviceCheckBox.setChecked(object == selectedRoute);
        ignoreCheckChange = false;
        holder.deviceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!ignoreCheckChange) {
                    if (deviceSelectedListener != null)
                        deviceSelectedListener.onDeviceSelected(object);
                    selectedRoute = object;
                    notifyDataSetChanged();
                }
            }
        });
    }

    public MediaRouter.RouteInfo getSelectedRoute() {
        return selectedRoute;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_device_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        initializeViews(objects.get(position), holder);
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView deviceIcon;
        private TextView deviceName;
        private CheckBox deviceCheckBox;

        public ViewHolder(View view) {
            super(view);
            deviceIcon = (ImageView) view.findViewById(R.id.device_icon);
            deviceName = (TextView) view.findViewById(R.id.device_name);
            deviceCheckBox = (CheckBox) view.findViewById(R.id.device_check);
        }
    }

    public interface OnDeviceSelectedListener {
        void onDeviceSelected(MediaRouter.RouteInfo deviceInfo);
    }
}
