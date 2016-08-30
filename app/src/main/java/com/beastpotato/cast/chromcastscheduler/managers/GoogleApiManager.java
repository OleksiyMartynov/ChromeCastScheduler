package com.beastpotato.cast.chromcastscheduler.managers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Oleksiy on 8/29/2016.
 */

public class GoogleApiManager {
    private static final String TAG = "GoogleApiManager";
    private static GoogleApiManager instance;
    private GoogleApiClient apiClient;

    private GoogleApiManager() {
    }

    public static GoogleApiManager getInstance() {
        if (instance == null)
            instance = new GoogleApiManager();
        return instance;
    }

    public <O extends Api.ApiOptions.HasOptions> GoogleApiClient connect(Context context, Api<O> options, O builder, GoogleApiClient.ConnectionCallbacks successListener, final GoogleApiClient.OnConnectionFailedListener failureListener) {
        cleanUp();
        apiClient = new GoogleApiClient.Builder(context)
                .addApi(options, builder)
                .addConnectionCallbacks(successListener)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        failureListener.onConnectionFailed(connectionResult);
                        cleanUp();
                    }
                })
                .build();

        apiClient.connect();
        return apiClient;
    }

    private void cleanUp() {
        if (apiClient != null && apiClient.isConnected())
            apiClient.disconnect();
        apiClient = null;
    }


}
