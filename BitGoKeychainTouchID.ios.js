/**
 * @providesModule BitGoKeychainTouchID
 * @flow
 */
'use strict';

import Promise from 'bluebird';
import { NativeModules } from 'react-native';
const NativeBitGoKeychainTouchID = NativeModules.BitGoKeychainTouchID;

/**
 * High-level docs for the BitGoKeychainTouchID iOS API can be written here.
 */

const BitGoKeychainTouchID = {
  hasCredentials(): Promise {
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
  retrieveCredentials(promptString: string): Promise{
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
  storeCredentials(username: string, password: string){
    NativeBitGoKeychainTouchID.storeCredentialsForAccount(username, password);
  },
  deleteCredentials(){
    NativeBitGoKeychainTouchID.deleteCredentials();
  }

};

export default BitGoKeychainTouchID;
