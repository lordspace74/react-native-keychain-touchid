/**
 * Stub of BitGoKeychainTouchID for Android.
 *
 * @providesModule BitGoKeychainTouchID
 * @flow
 */

var Promise = require('bluebird');
var NativeAES = require('NativeModules').BitGoAES;
var NativeHash = require('NativeModules').BitGoHash;
var React = require('react-native');
var {
  AsyncStorage
} = React;

var TOKEN_KEY = "bitgo-keychain-touchid-credentials-token";
var VECTOR_KEY = "bitgo-keychain-touchid-credentials-vector";

var BitGoKeychainTouchID = {
  hasCredentials(){
    return new Promise(function(resolve, reject){
      AsyncStorage.getItem(TOKEN_KEY)
      .then(function(credentials) {
        console.log(credentials.token);
        if (!credentials) {
          resolve(false);
        } else {
          resolve(true);
        }
      })
      .catch(function(e) {
        reject(false);
      });
    });
  },
  retrieveCredentials(promptString, passcode){
    var aesKey, credentials, vector;
    // hashedCode = sha256(passcode)
    // aesKey = hashedCode.substr(0, 32)
    // retrieve username, initVector, encryptedPassword
    // password = NativeAES.decrypt(encryptedPassword, aesKey, initVector)
    // credentials = JSON.parse(password)
    // extensionKey = tokenDetails.extensionKey // perivate key used for extending the token
    return new Promise(function(resolve, reject){
      NativeHash.sha256(
        passcode,
        function(hash) {
          aesKey = hash.substr(0, 32);
          AsyncStorage.getItem(VECTOR_KEY)
          .then(function(value) {
            if (!value) {
              reject("Credentials not found");
            } else {
              vector = value;
              return AsyncStorage.getItem(TOKEN_KEY);
            }
          })
          .then(function(hashedToken) {
            if (!hashedToken) {
              reject("Credentials not found");
            } else {
              NativeAES.decrypt(hashedToken, aesKey, vector, function(value) {
                credentials = value;
                resolve({ token: credentials });
              }, function(errorMessage) {
                reject(errorMessage);
              });
            }
          })
          .catch(function(e) {
            reject("Credentials not found");
          });
        },
        function(errorMessage) {
          reject(errorMessage);
        }
      );
    });
  },
  storeCredentials(username, password, passcode){
    var vector, aesKey, credentials;
    // store username
    // ask user for 6-digit-code -> passcode
    // hashedCode = sha256(passcode)
    // aesKey = hashedCode.substr(0, 32)
    // initVector = NativeAES.generateInitializationVector()
    // encryptedPassword = NativeAES.encrypt(password, esKey, initVector)
    // store username, encryptedPassword, initVector
    NativeHash.sha256(
      passcode,
      function(hash) {
        aesKey = hash.substr(0, 32);
        NativeAES.generateInitializationVector(function(value) {
          if (!value) {
            return;
          }
          vector = value;
          NativeAES.encrypt(password, aesKey, vector, function(result) {
            AsyncStorage.setItem(VECTOR_KEY, vector);
            AsyncStorage.setItem(TOKEN_KEY, result);
          }, function(errorMessage) {});
        }, function(errorMessage) {});
      }, function(errorMessage) {});
  },
  deleteCredentials(){
    AsyncStorage.removeItem(VECTOR_KEY);
    AsyncStorage.removeItem(TOKEN_KEY);
  }
};

module.exports = BitGoKeychainTouchID;
