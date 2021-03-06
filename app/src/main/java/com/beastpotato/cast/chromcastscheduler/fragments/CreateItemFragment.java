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
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.beastpotato.cast.chromcastscheduler.R;
import com.beastpotato.cast.chromcastscheduler.adapters.LayoutDeviceRowAdapter;
import com.beastpotato.cast.chromcastscheduler.managers.CastManager;
import com.beastpotato.cast.chromcastscheduler.models.ScheduledItem;

import java.util.List;

/**
 * Created by Oleksiy on 8/25/2016.
 */

public class CreateItemFragment extends DialogFragment implements TextView.OnEditorActionListener, CastManager.OnDeviceListUpdateListener, LayoutDeviceRowAdapter.OnDeviceSelectedListener {
    public static final String EXTRA_SCHEDULED_ITEM = "extra_scheduled_item";
    private EditText itemName, itemUrl;
    private TimePicker itemDate;
    private NumberPicker itemRepeat;
    private RecyclerView deviceListView;
    private MediaRouter.RouteInfo selectedDevice;
    private ScheduledItem item;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_add_item, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        itemRepeat = (NumberPicker) view.findViewById(R.id.item_repeat);
        itemRepeat.setMinValue(1);
        itemRepeat.setMaxValue(24);
        itemRepeat.setValue(24);
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
        if (getArguments() != null) {
            ScheduledItem item = getArguments().getParcelable(EXTRA_SCHEDULED_ITEM);
            if (item != null) {
                this.item = item;
                itemRepeat.setValue(item.repeatHour);
                itemName.setText(item.name);
                itemUrl.setText(item.url);
                itemDate.setHour(item.hour);
                itemDate.setMinute(item.minute);
            } else {
                this.item = new ScheduledItem();
            }
        } else {
            this.item = new ScheduledItem();
        }
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
        } else if (itemUrl.getText() == null || itemUrl.getText().length() < 1) {
            itemUrl.setError("Please enter URL with MP4 video");
            return null;
        } else if (selectedDevice == null) {
            Toast.makeText(getContext(), "Select device", Toast.LENGTH_LONG).show();
            return null;
        } else {
            name = itemName.getText().toString();
            url = itemUrl.getText().toString();
            item.name = name;
            item.deviceId = selectedDevice.getId();
            item.deciveName = selectedDevice.getName();
            item.url = url;
            item.hour = itemDate.getHour();
            item.minute = itemDate.getMinute();
            item.repeatHour = itemRepeat.getValue();
            return item;
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
