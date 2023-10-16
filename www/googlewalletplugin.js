var exec = require('cordova/exec');

var GoogleWalletPlugin = {
    saveToGooglePay: function(newObjectJson, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "GoogleWalletPlugin", "saveToGooglePay", [newObjectJson]);
    },

    canAddPassesToGoogleWallet: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "GoogleWalletPlugin", "canAddPassesToGoogleWallet");
    }
};

module.exports = GoogleWalletPlugin;
