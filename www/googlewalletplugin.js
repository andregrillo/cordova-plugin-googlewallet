var exec = require('cordova/exec');

var GoogleWalletPlugin = {
    saveToGooglePay: function(successCallback, errorCallback, newObjectJson) {
        exec(successCallback, errorCallback, "GoogleWalletPlugin", "saveToGooglePay", newObjectJson);
    },

    saveSignedJwtToGooglePay: function(successCallback, errorCallback, newObjectJson) {
        exec(successCallback, errorCallback, "GoogleWalletPlugin", "saveSignedJwtToGooglePay", newObjectJson);
    },

    canAddPassesToGoogleWallet: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "GoogleWalletPlugin", "canAddPassesToGoogleWallet");
    }
};

module.exports = GoogleWalletPlugin;
