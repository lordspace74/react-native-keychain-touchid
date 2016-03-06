/**
 * Stub of BitGoKeychainTouchID for Android.
 *
 * @providesModule BitGoKeychainTouchID
 * @flow
 */

var Promise = require('bluebird');
var NativeModules = require('react-native').NativeModules;
var NativeAES = NativeModules.BitGoAES;
var NativeHash = NativeModules.BitGoHash;
var BitGoPincodeModule = NativeModules.BitGoPincodeModule;
var React = require('react-native');

var BitGoKeychainTouchID = {
  hasCredentials: function() {
    return new Promise(function(resolve, reject) {
      BitGoPincodeModule.hasCredentials(resolve, resolve);
    });
  },
  retrieveCredentials: function() {
    return new Promise(function(resolve, reject) {
      BitGoPincodeModule.retrieveCredentials(function(result) {
        console.log('retrieveCredentials', result);
        resolve({token: result});
      },
      function(result) {
        if (typeof result === 'string') {
          result = new Error(result);
        }
        reject(result);
      });
    });
  },
  storeCredentials: function(username, password) {
    console.log('storeCredentials', password);
    BitGoPincodeModule.storeCredentials(password);
  },
  deleteCredentials: function() {
    BitGoPincodeModule.deleteCredentials();
  }
};

module.exports = BitGoKeychainTouchID;
