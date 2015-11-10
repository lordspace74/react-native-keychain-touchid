/**
 * Stub of BitGoKeychainTouchID for Android.
 *
 * @providesModule BitGoKeychainTouchID
 * @flow
 */
'use strict';

var warning = require('warning');

var BitGoKeychainTouchID = {
  test: function() {
    warning('Not yet implemented for Android.');
  },
  hasCredentials(){
    return false;
  },
  retrieveCredentials(){
    return new Promise(function(resolve, reject){
      resolve({});
    });
  },
  storeCredentials(){
  },
  deleteCredentials(){
  }
};

module.exports = BitGoKeychainTouchID;
