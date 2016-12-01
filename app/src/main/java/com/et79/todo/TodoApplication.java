package com.et79.todo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by eisuke on 2016/12/01.
 */

public class TodoApplication extends Application {
    private static final String TAG = "TodoApplication";

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
    }

    @Override
    public void onTerminate() {
        Log.d(TAG,"onTerminate");
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public FirebaseAuth getFirebaseAuth() {
        return mFirebaseAuth;
    }

    public void setFirebaseAuth(FirebaseAuth mFirebaseAuth) {
        this.mFirebaseAuth = mFirebaseAuth;
    }
}
