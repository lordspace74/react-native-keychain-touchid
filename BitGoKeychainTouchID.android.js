/**
 * Stub of BitGoKeychainTouchID for Android.
 *
 * @providesModule BitGoKeychainTouchID
 * @flow
 */
'use strict';

var Promise = require('bluebird');
var NativeAES = require('NativeModules').BitGoAES;
var NativeHash = require('NativeModules').BitGoHash;

console.log(NativeAES);
console.log(NativeHash);

console.log("\n\n\nARIK WAS HERE!\n\n\n");

var warning = require('warning');

var BitGoKeychainTouchID = {
  test: function() {
    warning('Not yet implemented for Android.');
  },
  hasCredentials(){
    return new Promise(function(resolve, reject){
      resolve(false);
    });
  },
  retrieveCredentials(){
    return new Promise(function(resolve, reject){
      resolve({});
    });
  },
  storeCredentials(){
  },
  deleteCredentials(){
  },
  androidFlag(){
    return true;
  }

};

module.exports = BitGoKeychainTouchID;
