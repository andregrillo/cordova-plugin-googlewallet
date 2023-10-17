package com.outsystems.experts.googlewallet;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.pay.Pay;
import com.google.android.gms.pay.PayApiAvailabilityStatus;
import com.google.android.gms.pay.PayClient;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class GoogleWalletPlugin extends CordovaPlugin {

    private static final int ADD_TO_GOOGLE_WALLET_REQUEST_CODE = 1000;
    private PayClient walletClient;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if ("saveToGooglePay".equals(action)) {
            this.callbackContext = callbackContext;
            final String jwtFromOutSystems = args.getString(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    saveToGooglePay(jwtFromOutSystems);
                }
            });
            return true;
        }
        else if ("saveSignedJwtToGooglePay".equals(action)) {
            this.callbackContext = callbackContext;
            final String jwtFromOutSystems = args.getString(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    saveToGooglePayJWT(jwtFromOutSystems);
                }
            });
            return true;
        }
        else if ("canAddPassesToGoogleWallet".equals(action)){
            canAddPassesToGoogleWallet(callbackContext);
            return true;
        }
        return false;
    }

    private void saveToGooglePay(String jwtFromOutSystems) {
        Activity activity = cordova.getActivity();
        if (walletClient == null) {
            walletClient = Pay.getClient(cordova.getActivity().getApplication());
            //walletClient = Pay.getClient(activity);
        }

        cordova.setActivityResultCallback(this);
        
        walletClient.getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
                .addOnSuccessListener(status -> {
                    if (status == PayApiAvailabilityStatus.AVAILABLE) {
                        walletClient.savePasses(jwtFromOutSystems, activity, ADD_TO_GOOGLE_WALLET_REQUEST_CODE);
                    } else {
                        callbackContext.error("Google Wallet API is not available.");
                    }
                })
                .addOnFailureListener(e -> {
                    callbackContext.error("Failed to check Google Wallet API availability: " + e.getMessage());
                });
    }

    private void saveToGooglePayJWT(String jwtFromOutSystems) {
        Activity activity = cordova.getActivity();
        if (walletClient == null) {
            walletClient = Pay.getClient(activity);
        }

        cordova.setActivityResultCallback(this);

        walletClient.getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
                .addOnSuccessListener(status -> {
                    if (status == PayApiAvailabilityStatus.AVAILABLE) {
                        walletClient.savePassesJwt(jwtFromOutSystems, activity, ADD_TO_GOOGLE_WALLET_REQUEST_CODE);
                    } else {
                        callbackContext.error("Google Wallet API is not available.");
                    }
                })
                .addOnFailureListener(e ->
                {
                    callbackContext.error("Failed to check Google Wallet API availability: " + e.getMessage());
                });
    }
    void canAddPassesToGoogleWallet(CallbackContext callbackContext) {
        if (walletClient == null) {
            walletClient = Pay.getClient(cordova.getActivity().getApplication());
        }

        walletClient.getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int status = task.getResult();
                        if (status == PayApiAvailabilityStatus.AVAILABLE) {
                            callbackContext.success();
                        } else {
                            callbackContext.error("The user or device is not eligible for using the Pay API");
                        }
                    } else {
                        Exception exception = task.getException();
                        callbackContext.error("Failed to check Google Wallet API availability: " + (exception != null ? exception.getMessage() : "Unknown error"));
                    }
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_TO_GOOGLE_WALLET_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    callbackContext.success("Pass saved successfully.");
                    break;
                case Activity.RESULT_CANCELED:
                    callbackContext.error("Save canceled.");
                    break;
                case PayClient.SavePassesResult.SAVE_ERROR:
                    String errorMessage = data.getStringExtra(PayClient.EXTRA_API_ERROR_MESSAGE);
                    callbackContext.error("Error saving pass: " + errorMessage);
                    break;
                default:
                    callbackContext.error("Unexpected error.");
            }
        }
    }
}
