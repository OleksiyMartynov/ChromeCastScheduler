package com.beastpotato.cast.chromcastscheduler.managers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.media.MediaControlIntent;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.CastDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleksiy on 8/25/2016.
 */
//todo follow https://github.com/googlecast/MediaRouter-Cast-Button-android/blob/master/src/com/example/mediarouter/MediaRouterDiscoveryActivity.java
//todo follow https://www.binpress.com/tutorial/building-an-android-google-cast-sender-app/161
//todo follow https://github.com/dbaelz/ChromecastDemoStreaming/blob/master/ChromecastDemoStreaming/src/main/java/de/inovex/chromecast/demostreaming/MainActivity.java
public class CastManager {
    private static CastManager instance;
    private MediaRouter mediaRouter;
    private MediaRouter.RouteInfo selectedRoute;
    private List<MediaRouter.RouteInfo> deviceRoutes;
    private List<OnDeviceListUpdateListener> deviceListChangeListeners;

    private CastMediaManager mediaManager;


    private CastManager(final Context context) {

        deviceListChangeListeners = new ArrayList<>();
        deviceRoutes = new ArrayList<>();
        mediaManager = CastMediaManager.getInstance();

        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                mediaRouter = MediaRouter.getInstance(context);
                init(context);
            }
        });
    }

    public static CastManager getInstance(Context context) {
        if (instance == null)
            instance = new CastManager(context);
        return instance;
    }

    private void init(Context context) {
        mediaRouter = MediaRouter.getInstance(context);
        MediaRouteSelector routeFilter = new MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .build();
        mediaRouter.addCallback(routeFilter, new RouterCallBack(), MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    public void playVideo(Context context, final String url, String deviceId) throws Exception {
        CastDevice selectedDevice = null;
        for (MediaRouter.RouteInfo device : deviceRoutes) {
            if (device.getId().equals(deviceId)) {
                selectedDevice = CastDevice.getFromBundle(device.getExtras());
            }
        }
        if (selectedDevice != null) {
            mediaManager.playVideo(context, selectedDevice, url);
        } else
            throw new Exception("Cant find device");
    }



    public void addOnDeviceListUpdateListener(OnDeviceListUpdateListener listener) {
        if (!deviceListChangeListeners.contains(listener)) {
            deviceListChangeListeners.add(listener);
        }
    }

    public void removeOnDeviceListChangeListener(OnDeviceListUpdateListener listener) {
        deviceListChangeListeners.remove(listener);
    }

    public List<MediaRouter.RouteInfo> getDeviceRoutes() {
        return deviceRoutes;
    }

    private void notifyOnDeviceListUpdateListener(MediaRouter.RouteInfo route) {
        for (OnDeviceListUpdateListener deviceListUpdateListener : deviceListChangeListeners) {
            deviceListUpdateListener.onDeviceListUpdate(deviceRoutes, route);
        }
    }

    public interface OnDeviceListUpdateListener {
        void onDeviceListUpdate(List<MediaRouter.RouteInfo> list, MediaRouter.RouteInfo deviceDelta);
    }

    private class RouterCallBack extends MediaRouter.Callback {
        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteAdded(router, route);
            synchronized (this) {
                deviceRoutes.add(route);
                notifyOnDeviceListUpdateListener(route);
            }
        }

        @Override
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteRemoved(router, route);
            synchronized (this) {
                deviceRoutes.remove(route);
            }
        }

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteSelected(router, route);
            selectedRoute = route;
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteUnselected(router, route);
            selectedRoute = null;
        }

        @Override
        public void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteChanged(router, route);
            synchronized (this) {
                if (!deviceRoutes.contains(route)) {
                    deviceRoutes.add(route);
                } else {
                    deviceRoutes.remove(route);
                    deviceRoutes.add(route);
                }
            }
        }
    }


}
