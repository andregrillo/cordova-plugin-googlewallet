var exec = require('cordova/exec');

exports.init = function(clientId, clientSecret, redirectUri, success, error) {
    exec(success, error, 'GoogleWalletPlugin', 'init', [clientId, clientSecret, redirectUri]);
};

exports.authenticate = function(success, error) {
    exec(success, error, 'GoogleWalletPlugin', 'authenticate', []);
};

exports.insertTicket = function(ticketInfo, success, error) {
    exec(success, error, 'GoogleWalletPlugin', 'insertTicket', [ticketInfo]);
};
