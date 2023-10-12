var exec = require('cordova/exec');

var GoogleWalletPlugin = {
    saveToGooglePay: function(newObjectJson, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "GoogleWalletPlugin", "saveToGooglePay", [newObjectJson]);
    }
};

module.exports = GoogleWalletPlugin;
