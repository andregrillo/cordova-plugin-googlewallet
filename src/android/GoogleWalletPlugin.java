package com.outsystems.experts.googlewallet;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GoogleWalletPlugin extends CordovaPlugin {

    private CallbackContext authCallbackContext;
    private static String CLIENT_ID;// = "CLIENT_ID";
    private static String REDIRECT_URI;// = "REDIRECT_URI";
    private static String AUTH_URL; // = "https://accounts.google.com/o/oauth2/auth?"
//        + "client_id=" + CLIENT_ID
//        + "&redirect_uri=" + REDIRECT_URI
//        + "&response_type=code"
//        + "&scope=https://www.googleapis.com/auth/wallet_object.issuer";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("init".equals(action)) {
            if(args.length() >= 3 && !args.isNull(0)) {
                String clientId = args.getString(0);
                // Check if the obtained strings are not empty
                if(clientId != null && !clientId.isEmpty()) {
                    init(clientId, callbackContext);
                    return true;
                } else {
                    callbackContext.error("One or more parameters are empty");
                    return false;
                }
            } else {
                callbackContext.error("Not enough parameters or null parameter provided");
                return false;
            }
        } else if ("authenticate".equals(action)) {
            authenticate(callbackContext);
            return true;
        } else if ("insertTicket".equals(action)) {
            insertTicket(args, callbackContext);
            return true;
        }
        return false;  
    }

    private void init(String clientId, CallbackContext callbackContext){
        String appIdentifier = cordova.getContext().getPackageName();

        CLIENT_ID = clientId;
        REDIRECT_URI = appIdentifier + "://";
        AUTH_URL = "https://accounts.google.com/o/oauth2/auth?"
                + "client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=code"
                + "&scope=https://www.googleapis.com/auth/wallet_object.issuer";
        callbackContext.success();
    }

    private void authenticate(final CallbackContext callbackContext) {
        this.authCallbackContext = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Open an InAppBrowser with the authentication URL
                String script = "var browser = cordova.InAppBrowser.open('" + AUTH_URL + "', '_blank', 'location=yes');"
                        + "browser.addEventListener('loadstart', function(event) {"
                        + "    var code = /code=([^&]+)/.exec(event.url);"
                        + "    if (code) {"
                        + "        browser.close();"
                        + "        cordova.fireDocumentEvent('authCodeReceived', { 'code': code[1] });"
                        + "    }"
                        + "});";
                webView.loadUrl("javascript:" + script);

                // Keep the callback active
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
    }

    @Override
    public Object onMessage(String id, Object data) {
        if ("authCodeReceived".equals(id)) {
            try {
                String authCode = ((JSONObject) data).getString("code");
                exchangeAuthCodeForToken(authCode, this.authCallbackContext);
            } catch (JSONException e) {
                e.printStackTrace();
                this.authCallbackContext.error("Failed to parse authorization code: " + e.getMessage());
            }
        }
        return null;
    }

    private void exchangeAuthCodeForToken(String authCode, CallbackContext callbackContext) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String tokenEndpoint = "https://oauth2.googleapis.com/token";
                    URL url = new URL(tokenEndpoint);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);

                    String payload = "code=" + authCode
                            + "&client_id=" + CLIENT_ID
                            + "&redirect_uri=" + REDIRECT_URI
                            + "&grant_type=authorization_code";

                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = payload.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String response;
                        try (Scanner scanner = new Scanner(connection.getInputStream())) {
                            response = scanner.useDelimiter("\\A").next();
                        }
                        callbackContext.success(response);
                    } else {
                        callbackContext.error("Failed to retrieve tokens: " + connection.getResponseMessage());
                    }
                } catch (Exception e) {
                    callbackContext.error("Exception: " + e.getMessage());
                }
            }
        }).start();
    }

    private void insertTicket(JSONArray args, CallbackContext callbackContext) {
        try {
            JSONObject ticketInfo = args.getJSONObject(0);
            String ticketClass = ticketInfo.getString("ticketClass");
            String ticketObject = ticketInfo.getString("ticketObject");
            String accessToken = ticketInfo.getString("accessToken");
            String refreshToken = ticketInfo.getString("refreshToken");
            String apiUrl = "YOUR_GOOGLE_WALLET_API_ENDPOINT"; // Replace with the actual API endpoint

            handleApiRequest(accessToken, refreshToken, apiUrl, callbackContext);
        } catch (JSONException e) {
            callbackContext.error("❌ Failed to parse arguments: " + e.getMessage());
        } catch (Exception e) {
            callbackContext.error("❌ Failed to insert ticket: " + e.getMessage());
        }
    }

    private void handleApiRequest(String accessToken, String refreshToken, String apiUrl, CallbackContext callbackContext) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = makeApiRequest(accessToken, apiUrl);
                    callbackContext.success(response);
                } catch (IOException e) {
                    if (e.getMessage().contains("Unauthorized")) {
                        try {
                            //String newAccessToken = refreshAccessToken(refreshToken);
                            String newAccessToken = refreshAccessToken(refreshToken, CLIENT_ID);
                            // TODO: Store the new access token securely
                            String response = makeApiRequest(newAccessToken, apiUrl);
                            callbackContext.success(response);
                        } catch (Exception refreshEx) {
                            callbackContext.error("Failed to refresh access token: " + refreshEx.getMessage());
                        }
                    } else {
                        callbackContext.error("API request failed: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    private String makeApiRequest(String accessToken, String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // TODO: Add API request payload here
            String payload = "YOUR_API_REQUEST_PAYLOAD";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    return scanner.useDelimiter("\\A").next();
                }
            } else {
                throw new IOException("HTTP error code: " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }

    private String refreshAccessToken(String refreshToken, String clientId) throws IOException {
        String tokenEndpoint = "https://oauth2.googleapis.com/token";
        URL url = new URL(tokenEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            // Define the payload data without client secret
            String payload = "refresh_token=" + refreshToken
                    + "&client_id=" + clientId
                    + "&grant_type=refresh_token";

            // Write payload to output stream
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get HTTP response code
            int responseCode = connection.getResponseCode();

            // Check if request was successful
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                String response;
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    response = scanner.useDelimiter("\\A").next();
                }

                // Extract and return the access token from the response JSON
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    return jsonResponse.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new IOException("Failed to parse JSON response: " + e.getMessage());
                }

            } else {
                throw new IOException("Failed to refresh access token: " + connection.getResponseMessage());
            }
        } finally {
            connection.disconnect();
        }
    }
}
