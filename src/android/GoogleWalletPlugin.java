package com.outsystems.experts.googlewallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.android.gms.pay.Pay;
import com.google.android.gms.pay.PayApiAvailabilityStatus;
import com.google.android.gms.pay.PayClient;
import java.util.UUID;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleWalletPlugin extends CordovaPlugin {

    private static final int ADD_TO_GOOGLE_WALLET_REQUEST_CODE = 1000;
    private PayClient walletClient;
    private CallbackContext callbackContext;

    //TODO: Check if the Google Wallet API is available!!!

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
        else if ("canAddPassesToGoogleWallet".equals(action)){
            canAddPassesToGoogleWallet(callbackContext);
        }
        return false;
    }

    private void saveToGooglePay(String jwtFromOutSystems) {
        Activity activity = cordova.getActivity();
        walletClient = Pay.getClient(activity);

        cordova.setActivityResultCallback(this);

        try {
            // Attempt to parse the JWT string as a JSON object for validation
            new JSONObject(jwtFromOutSystems);

            walletClient.getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
                    .addOnSuccessListener(status -> {
                        if (status == PayApiAvailabilityStatus.AVAILABLE) {
                            walletClient.savePasses(jwtFromOutSystems, activity, ADD_TO_GOOGLE_WALLET_REQUEST_CODE);
                        } else {
                            callbackContext.error("Google Wallet API is not available.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.v("BENFICA", e.getMessage());
                        callbackContext.error("Failed to check Google Wallet API availability: " + e.getMessage());
                    });
        } catch (JSONException e) {
            callbackContext.error("Provided string is not a valid JSON object (JWT): " + e.getMessage());
        }
    }

    private void saveToGooglePayJWT(String jwtFromOutSystems) {

        String teste = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0ZXN0ZW91dHN5c3RlbXNAYmVuZmljYS1hbmRyb2lkLXdhbGxldC5pYW0uZ3NlcnZpY2VhY2NvdW50LmNvbSIsImF1ZCI6Imdvb2dsZSIsIm9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDozMDAwIl0sInR5cCI6InNhdmV0b3dhbGxldCIsInBheWxvYWQiOnsiZ2VuZXJpY09iamVjdHMiOlt7ImlkIjoiMzM4ODAwMDAwMDAyMjI4OTA0OC5jb2RlbGFiX29iamVjdCIsImNsYXNzSWQiOiIzMzg4MDAwMDAwMDIyMjg5MDQ4LmNvZGVsYWJfY2xhc3MiLCJnZW5lcmljVHlwZSI6IkdFTkVSSUNfVFlQRV9VTlNQRUNJRklFRCIsImhleEJhY2tncm91bmRDb2xvciI6IiM0Mjg1ZjQiLCJsb2dvIjp7InNvdXJjZVVyaSI6eyJ1cmkiOiJodHRwczovL3N0b3JhZ2UuZ29vZ2xlYXBpcy5jb20vd2FsbGV0LWxhYi10b29scy1jb2RlbGFiLWFydGlmYWN0cy1wdWJsaWMvcGFzc19nb29nbGVfbG9nby5qcGcifX0sImNhcmRUaXRsZSI6eyJkZWZhdWx0VmFsdWUiOnsibGFuZ3VhZ2UiOiJlbi1VUyIsInZhbHVlIjoiR29vZ2xlIEkvTyAnMjIifX0sInN1YmhlYWRlciI6eyJkZWZhdWx0VmFsdWUiOnsibGFuZ3VhZ2UiOiJlbi1VUyIsInZhbHVlIjoiQXR0ZW5kZWUifX0sImhlYWRlciI6eyJkZWZhdWx0VmFsdWUiOnsibGFuZ3VhZ2UiOiJlbi1VUyIsInZhbHVlIjoiQWxleCBNY0phY29icyJ9fSwiYmFyY29kZSI6eyJ0eXBlIjoiUVJfQ09ERSIsInZhbHVlIjoiMzM4ODAwMDAwMDAyMjI4OTA0OC5jb2RlbGFiX29iamVjdCJ9LCJoZXJvSW1hZ2UiOnsic291cmNlVXJpIjp7InVyaSI6Imh0dHBzOi8vc3RvcmFnZS5nb29nbGVhcGlzLmNvbS93YWxsZXQtbGFiLXRvb2xzLWNvZGVsYWItYXJ0aWZhY3RzLXB1YmxpYy9nb29nbGUtaW8taGVyby1kZW1vLW9ubHkuanBnIn19LCJ0ZXh0TW9kdWxlc0RhdGEiOlt7ImhlYWRlciI6IlBPSU5UUyIsImJvZHkiOiIxMjM0IiwiaWQiOiJwb2ludHMifSx7ImhlYWRlciI6IkNPTlRBQ1RTIiwiYm9keSI6IjIwIiwiaWQiOiJjb250YWN0cyJ9XX1dfSwiaWF0IjoxNjk3MTk5NDU5fQ.tyK2mhQrxjQJc-vj4y_n-Ou9LCfil3nfMlSjvqGhYP4ibjZnOi4Iz-zb7-yTixueWAvXSp6u9R2JsycpEeUx9fXYB_JuvIBHikLDj6yqjH600RL-bhdyt9Q7nBrITxQp3GQ2XKpGPdhpPv3h3Y1iVs2udVznavHliNDBrn6GXrnMuarye2Y43Epenw8awQJr7l2nqY445YU-JK7AjRsG63gUTW9EVtFts-VAOlbog-ZFFdHKpu3rP8t65YmIi3dJ_cZ0ebESoBupQj6VWgo9ehR-gSSStcQFTm9pP5vZefP9MBx_GDktaHClH9qW0BdCkN-1fhfpa53Y7kPvh460qg";
        Activity activity = cordova.getActivity();
        walletClient = Pay.getClient(activity);

        cordova.setActivityResultCallback(this);

        walletClient.getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
                .addOnSuccessListener(status -> {
                    if (status == PayApiAvailabilityStatus.AVAILABLE) {
                        walletClient.savePassesJwt(teste, activity, ADD_TO_GOOGLE_WALLET_REQUEST_CODE);
                    } else {
                        callbackContext.error("Google Wallet API is not available.");
                    }
                })
                .addOnFailureListener(e ->
                {
                    Log.v("BENFICA", e.getMessage());
                    callbackContext.error("Failed to check Google Wallet API availability: " + e.getMessage());
                });
    }
    void canAddPassesToGoogleWallet(CallbackContext callbackContext) {
        walletClient
                .getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
                .addOnSuccessListener(status -> {
                    if (status == PayApiAvailabilityStatus.AVAILABLE) {
                        callbackContext.success();
                    } else {
                        callbackContext.error("The user or device is not eligible for using the Pay API");
                    };
                })
                .addOnFailureListener(exception -> {
                    callbackContext.error("Google Play Services is too old, or API availability not verified");
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
