package com.beastpotato.cast.chromcastscheduler.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.beastpotato.cast.chromcastscheduler.R;
import com.beastpotato.cast.chromcastscheduler.managers.CastManager;
import com.beastpotato.cast.chromcastscheduler.models.ScheduledItem;

import java.util.List;

import adapters.LayoutDeviceRowAdapter;

/**
 * Created by Oleksiy on 8/25/2016.
 */

public class CreateItemFragment extends DialogFragment implements TextView.OnEditorActionListener, CastManager.OnDeviceListUpdateListener, LayoutDeviceRowAdapter.OnDeviceSelectedListener {
    private EditText itemName, itemUrl;
    private TimePicker itemDate;
    private RecyclerView deviceListView;
    private MediaRouter.RouteInfo selectedDevice;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_add_item, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        itemName = (EditText) view.findViewById(R.id.item_name);
        itemUrl = (EditText) view.findViewById(R.id.item_url);
        itemUrl.setOnEditorActionListener(this);
        itemDate = (TimePicker) view.findViewById(R.id.item_date);
        itemDate.setIs24HourView(true);
        deviceListView = (RecyclerView) view.findViewById(R.id.devices_list);
        LayoutDeviceRowAdapter deviceRowAdapter = new LayoutDeviceRowAdapter(CastManager.getInstance(getContext()).getDeviceRoutes());
        deviceRowAdapter.setDeviceSelectedListener(this);
        deviceListView.setAdapter(deviceRowAdapter);
        deviceListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        getDialog().setTitle("Add item");
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (EditorInfo.IME_ACTION_DONE == i) {
            ScheduledItem item = validate();
            if (item != null) {
                OnAddItemDialogDone activity = (OnAddItemDialogDone) getActivity();
                activity.onDone(item);
                this.dismiss();
            }
            return true;
        }
        return false;
    }

    private ScheduledItem validate() {
        String name, url;
        if (itemName.getText() == null || itemName.getText().length() < 1) {
            itemName.setError("Please enter name");
            return null;
        } else if (itemUrl.getText() == null || itemUrl.getText().length() < 1 || (!itemUrl.getText().toString().endsWith(".mp4") && !itemUrl.getText().toString().endsWith(".MP4"))) {
            itemUrl.setError("Please enter URL ending in .MP4");
            return null;
        } else if (selectedDevice == null) {
            Toast.makeText(getContext(), "Select device", Toast.LENGTH_LONG).show();
            return null;
        } else {
            name = itemName.getText().toString();
            url = itemUrl.getText().toString();
            return new ScheduledItem(name, selectedDevice.getId(), selectedDevice.getName(), url, itemDate.getHour(), itemDate.getMinute());
        }
    }

    @Override
    public void onDeviceSelected(MediaRouter.RouteInfo deviceInfo) {
        this.selectedDevice = deviceInfo;
    }

    @Override
    public void onDeviceListUpdate(List<MediaRouter.RouteInfo> list, MediaRouter.RouteInfo deviceDelta) {
        deviceListView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        CastManager.getInstance(getContext()).removeOnDeviceListChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        CastManager.getInstance(getContext()).addOnDeviceListUpdateListener(this);
    }

    public interface OnAddItemDialogDone {
        void onDone(ScheduledItem item);
    }
}
