package com.beastpotato.cast.chromcastscheduler.managers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.beastpotato.cast.chromcastscheduler.Constants;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;

/**
 * Created by Oleksiy on 8/29/2016.
 */
public class CastMediaManager {
    private static final String TAG = "CastMediaManager";
    private static CastMediaManager instance;
    private RemoteMediaPlayer remoteMediaPlayer;
    private GoogleApiManager googleApiManager;
    private boolean isPlaying, videoIsLoaded;
    private boolean waitingForReconnect;
    private GoogleApiClient apiClient;
    private boolean isApplicationStarted;
    private String vidUrl;

    private CastMediaManager() {
        googleApiManager = GoogleApiManager.getInstance();
        remoteMediaPlayer = new RemoteMediaPlayer();
        remoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                MediaStatus mediaStatus = remoteMediaPlayer.getMediaStatus();
                Log.i(TAG,"MEDIA STATUS:"+mediaStatus);
                if (mediaStatus != null)
                    isPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
            }
        });

        remoteMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
            @Override
            public void onMetadataUpdated() {
            }
        });
    }

    public static CastMediaManager getInstance() {
        if(instance ==null)
            instance= new CastMediaManager();
        return instance;
    }

    public void playVideo(final Context context, CastDevice device, String vidUrl) {
        Cast.CastOptions.Builder apiOptionsBuilder = new Cast.CastOptions.Builder(device, new Cast.Listener() {
            @Override
            public void onApplicationDisconnected(int i) {
                super.onApplicationDisconnected(i);
                teardown();
            }
        });
        this.vidUrl = vidUrl;
        apiClient = googleApiManager.connect(context, Cast.API, apiOptionsBuilder.build(), new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (waitingForReconnect) {
                    waitingForReconnect = false;
                    reconnectChannels(bundle);
                } else {
                    try {
                        Cast.CastApi.launchApplication(apiClient, Constants.APP_ID)
                                .setResultCallback(
                                        new ResultCallback<Cast.ApplicationConnectionResult>() {
                                            @Override
                                            public void onResult(
                                                    Cast.ApplicationConnectionResult applicationConnectionResult) {
                                                Status status = applicationConnectionResult.getStatus();
                                                if (status.isSuccess()) {
                                                     isApplicationStarted = true;
                                                    reconnectChannels(null);
                                                }else {
                                                    Toast.makeText(context, "ApplicationConnectionResult :" + status.getStatusCode(), Toast.LENGTH_SHORT).show();
                                                    Log.e(TAG,"ApplicationConnectionResult :"+status.getStatusCode());
                                                }
                                            }
                                        }
                                );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                waitingForReconnect = true;
            }
        }, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                teardown();
            }
        });
    }

    private void startVideo(final GoogleApiClient apiClient, String vidUrl) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, "Title");

        MediaInfo mediaInfo = new MediaInfo.Builder(vidUrl)
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
        try {
            remoteMediaPlayer.load(apiClient, mediaInfo, true)
                    .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
                            if (mediaChannelResult.getStatus().isSuccess()) {
                                videoIsLoaded = true;
                                remoteMediaPlayer.play(apiClient);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void teardown() {
        if(isApplicationStarted) {
            try {
                Cast.CastApi.stopApplication(apiClient);
                if (remoteMediaPlayer != null) {
                    Cast.CastApi.removeMessageReceivedCallbacks(apiClient, remoteMediaPlayer.getNamespace());
                    remoteMediaPlayer = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            apiClient = null;
            isApplicationStarted = false;
            isPlaying = false;
            videoIsLoaded = false;
            waitingForReconnect =false;
            apiClient = null;
            vidUrl = null;
        }
    }

    private void reconnectChannels(Bundle hint) {
        if ((hint != null) && hint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
            Log.e( TAG, "App is no longer running" );
            teardown();
        } else {
            try {
                Cast.CastApi.setMessageReceivedCallbacks(apiClient, remoteMediaPlayer.getNamespace(), remoteMediaPlayer);
                startVideo(apiClient,vidUrl);
            } catch (IOException e) {
                //Log.e( TAG, "Exception while creating media channel ", e );
            } catch (NullPointerException e) {
                //Log.e( TAG, "Something wasn't reinitialized for reconnectChannels" );
            }
        }
    }
}
