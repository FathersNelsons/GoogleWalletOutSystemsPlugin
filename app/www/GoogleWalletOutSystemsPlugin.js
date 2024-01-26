var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'GoogleWallet', 'coolMethod', [arg0]);
};

exports.canAddToWallet = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'GoogleWallet', 'canAddToWallet', []);
}

exports.addToWallet = function (successCallback, errorCallback, memberCardJson) {
    exec(successCallback, errorCallback, 'GoogleWallet', 'addToWallet', [ memberCardJson ]);
}