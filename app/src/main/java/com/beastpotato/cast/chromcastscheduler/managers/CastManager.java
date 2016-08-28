package com.beastpotato.cast.chromcastscheduler.managers;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleksiy on 8/25/2016.
 */
//todo follow https://github.com/googlecast/MediaRouter-Cast-Button-android/blob/master/src/com/example/mediarouter/MediaRouterDiscoveryActivity.java
//todo follow https://www.binpress.com/tutorial/building-an-android-google-cast-sender-app/161
public class CastManager {
    private static CastManager instance;
    private static final String APP_ID = "D93BF532";
    private MediaRouter mediaRouter;
    private MediaRouter.RouteInfo selectedRoute;
    private List<String> deviceNames;
    private List<MediaRouter.RouteInfo> deviceRoutes;
    private List<OnDeviceListUpdateListener> deviceListChangeListeners;
    private boolean mWaitingForReconnect;
    private GoogleApiClient mApiClient;
    private boolean mApplicationStarted;
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private boolean mIsPlaying;
    private boolean mVideoIsLoaded;
    private Runnable completionBlock;

    private CastManager(Context context) {
        deviceNames = new ArrayList<>();
        deviceRoutes = new ArrayList<>();
        deviceListChangeListeners = new ArrayList<>();
        mediaRouter = MediaRouter.getInstance(context);
        init(context);
    }

    public static CastManager getInstance(Context context) {
        if (instance == null)
            instance = new CastManager(context);
        return instance;
    }

    private void init(Context context) {
        mediaRouter = MediaRouter.getInstance(context);
        MediaRouteSelector routeFilter = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID))
                .build();
        mediaRouter.addCallback(routeFilter, new RouterCallBack(), MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    public void playVideo(Context context, final String url, String deviceId) throws Exception {
        CastDevice selectedDevice = null;
        for (MediaRouter.RouteInfo device : deviceRoutes) {
            if (device.getId().equals(deviceId)) {
                selectedDevice = CastDevice.getFromBundle(device.getExtras());
            }
        }
        if (selectedDevice != null) {
            launchReceiver(context, selectedDevice);
            completionBlock = new Runnable() {
                @Override
                public void run() {

                    startVideo(url);//wait for completion?
                }

            };
        } else
            throw new Exception("Cant find device");
    }

    private void launchReceiver(Context context, CastDevice device) {

        initRemoteMediaPlayer();
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder(device, new Cast.Listener() {
                    @Override
                    public void onApplicationDisconnected(int i) {
                        super.onApplicationDisconnected(i);
                        teardown();
                    }
                });

        ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks();
        ConnectionFailedListener mConnectionFailedListener = new ConnectionFailedListener();
        mApiClient = new GoogleApiClient.Builder(context)
                .addApi(Cast.API, apiOptionsBuilder.build())
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();

        mApiClient.connect();
    }

    private void initRemoteMediaPlayer() {
        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
                if (mediaStatus != null)
                    mIsPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
            }
        });

        mRemoteMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
            @Override
            public void onMetadataUpdated() {
            }
        });
    }

    private void reconnectChannels(Bundle hint) {
        if ((hint != null) && hint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
            //Log.e( TAG, "App is no longer running" );
            teardown();
        } else {
            try {
                Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
                if (completionBlock != null) {
                    completionBlock.run();
                    completionBlock = null;
                }
            } catch (IOException e) {
                //Log.e( TAG, "Exception while creating media channel ", e );
            } catch (NullPointerException e) {
                //Log.e( TAG, "Something wasn't reinitialized for reconnectChannels" );
            }
        }
    }

    private void startVideo(String vidUrl) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, "Title");

        MediaInfo mediaInfo = new MediaInfo.Builder(vidUrl)
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
        try {
            mRemoteMediaPlayer.load(mApiClient, mediaInfo, true)
                    .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
                            if (mediaChannelResult.getStatus().isSuccess()) {
                                mVideoIsLoaded = true;
                            }
                        }
                    });
        } catch (Exception e) {
        }
    }

    private void teardown() {
        if (mApiClient != null) {
            if (mApplicationStarted) {
                try {
                    Cast.CastApi.stopApplication(mApiClient);
                    if (mRemoteMediaPlayer != null) {
                        Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace());
                        mRemoteMediaPlayer = null;
                    }
                } catch (IOException e) {
                    //Log.e( TAG, "Exception while removing application " + e );
                }
                mApplicationStarted = false;
            }
            if (mApiClient.isConnected())
                mApiClient.disconnect();
            mApiClient = null;
        }
        //mSelectedDevice = null;
        mVideoIsLoaded = false;
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

    private class RouterCallBack extends MediaRouter.Callback {
        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteAdded(router, route);
            synchronized (this) {
                deviceNames.add(route.getId());
                deviceRoutes.add(route);
                notifyOnDeviceListUpdateListener(route);
            }
        }

        @Override
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteRemoved(router, route);
            synchronized (this) {
                deviceNames.remove(route.getId());
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
    }

    private void notifyOnDeviceListUpdateListener(MediaRouter.RouteInfo route) {
        for (OnDeviceListUpdateListener deviceListUpdateListener : deviceListChangeListeners) {
            deviceListUpdateListener.onDeviceListUpdate(deviceRoutes, route);
        }
    }

    public interface OnDeviceListUpdateListener {
        void onDeviceListUpdate(List<MediaRouter.RouteInfo> list, MediaRouter.RouteInfo deviceDelta);
    }

    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle hint) {
            if (mWaitingForReconnect) {
                mWaitingForReconnect = false;
                reconnectChannels(hint);
            } else {
                try {
                    Cast.CastApi.launchApplication(mApiClient, APP_ID, false)
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(
                                                Cast.ApplicationConnectionResult applicationConnectionResult) {
                                            Status status = applicationConnectionResult.getStatus();
                                            if (status.isSuccess()) {
                                                //Values that can be useful for storing/logic
                                                ApplicationMetadata applicationMetadata =
                                                        applicationConnectionResult.getApplicationMetadata();
                                                String sessionId =
                                                        applicationConnectionResult.getSessionId();
                                                String applicationStatus =
                                                        applicationConnectionResult.getApplicationStatus();
                                                boolean wasLaunched =
                                                        applicationConnectionResult.getWasLaunched();

                                                mApplicationStarted = true;
                                                reconnectChannels(null);
                                            }
                                        }
                                    }
                            );
                } catch (Exception e) {

                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            mWaitingForReconnect = true;
        }
    }

    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            teardown();
        }
    }
}
