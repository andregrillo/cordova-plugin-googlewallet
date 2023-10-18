# Google Wallet Cordova Plugin

**Note**: This plugin is designed specifically for Android. Ensure you're working within an Android environment when using this plugin.
This Cordova plugin provides an interface to Google Wallet's API, allowing you to save signed and unsigned JWTs to Google Pay, and check if passes can be added to Google Wallet.

## Installation

```
cordova plugin add https://github.com/andregrillo/cordova-plugin-googlewallet.git
```

## Usage

### 1. Save unsigned JWT Pass to Google Wallet

```
GoogleWalletPlugin.saveToGooglePay(successCallback, errorCallback, newObjectJson);
```

- `successCallback`: Function to be called upon successful execution.
- `errorCallback`: Function to be called upon encountering an error.
- `newObjectJson`: JSON object to be saved to Google Pay.

### 2. Save Signed JWT Pass to Google Wallet

```
GoogleWalletPlugin.saveSignedJwtToGooglePay(successCallback, errorCallback, newObjectJson);
```

- `successCallback`: Function to be called upon successful execution.
- `errorCallback`: Function to be called upon encountering an error.
- `newObjectJson`: Signed JWT object to be saved to Google Pay.

### 3. Check if Passes Can Be Added to Google Wallet

```
GoogleWalletPlugin.canAddPassesToGoogleWallet(successCallback, errorCallback);
```

- `successCallback`: Function to be called upon successful execution.
- `errorCallback`: Function to be called upon encountering an error.

## Native Implementation

The native Android implementation is done in the `GoogleWalletPlugin` class. This class provides methods to interact with the Google Wallet API, such as:

- `saveToGooglePay`: Save a pass to Google Pay.
- `saveToGooglePayJWT`: Save a signed JWT to Google Pay.
- `canAddPassesToGoogleWallet`: Check if passes can be added to Google Wallet.

## JavaScript Interface

The JavaScript interface is provided by the `GoogleWalletPlugin` object. This object exposes methods that can be called from your Cordova application to interact with the native Android implementation.

## Author

- [André Grillo](https://github.com/andregrillo)

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT](https://choosealicense.com/licenses/mit/)
