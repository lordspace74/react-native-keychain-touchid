/**
 * @providesModule BitGoKeychainTouchID
 * @flow
 */
'use strict';

var Promise = require('bluebird');
var NativeBitGoKeychainTouchID = require('NativeModules').BitGoKeychainTouchID;

/**
 * High-level docs for the BitGoKeychainTouchID iOS API can be written here.
 */

var BitGoKeychainTouchID = {
  hasCredentials(){
    return new Promise(function(resolve, reject) {
      NativeBitGoKeychainTouchID.hasCredentialsWithCallback(
      function(err, hasCredentials) {
        if (err) {
          reject(new Error(err.message));
        }
        resolve(hasCredentials);
      });
    });
  },
  retrieveCredentials(promptString){
    return new Promise(function(resolve, reject) {
      NativeBitGoKeychainTouchID.retrieveCredentialsWithTouchIDPrompt(
      promptString,
      function(retrievalError, details) {
        if (retrievalError) {
          reject(new Error(retrievalError.message));
        }
        resolve(details);
      });
    });
  },
  storeCredentials(username, password){
    NativeBitGoKeychainTouchID.storeCredentialsForAccount(username, password);
  },
  deleteCredentials(){
    NativeBitGoKeychainTouchID.deleteCredentials();
  }

};

module.exports = BitGoKeychainTouchID;
