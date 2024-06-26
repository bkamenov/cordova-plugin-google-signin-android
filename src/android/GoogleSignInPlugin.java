package com.cordova.plugin.googlesignin;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleSignInPlugin extends CordovaPlugin {

    private static final int RC_SIGN_IN = 9001;
    private CallbackContext callbackContext;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("login")) {
            String clientId = args.getString(0);
            this.login(clientId);
            return true;
        } else if (action.equals("logout")) {
            this.logout();
            return true;
        }

        return false;
    }

    private void login(String clientId) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(clientId)
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this.cordova.getActivity(), gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        this.cordova.setActivityResultCallback(this);
        this.cordova.getActivity().startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void logout() {
        if (mGoogleSignInClient == null) {
            mGoogleSignInClient = GoogleSignIn.getClient(this.cordova.getActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);
        }

        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this.cordova.getActivity(), new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                }
            });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            JSONObject result = new JSONObject();
            result.put("email", account.getEmail());
            result.put("displayName", account.getDisplayName());
            result.put("id", account.getId());
            result.put("idToken", account.getIdToken());

            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
        } catch (ApiException | JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }
}
