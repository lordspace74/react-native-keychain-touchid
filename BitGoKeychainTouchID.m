#import "BitGoKeychainTouchID.h"
#import <React/RCTUtils.h>

@implementation BitGoKeychainTouchID

RCT_EXPORT_MODULE()

- (NSString *)serviceName{
    return @"com.bitgo.ios";
}
- (NSString *)userDefaultsStorageIndicatorKey{
    return @"stored-credentials";
}

RCT_EXPORT_METHOD(storeCredentialsForAccount:(NSString *)email withToken:(NSString *)token){

    CFErrorRef error = NULL;
    SecAccessControlRef sacObject = SecAccessControlCreateWithFlags(
      kCFAllocatorDefault,
      kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
      kSecAccessControlTouchIDCurrentSet, // Restrict keychain item access to TouchID, and invalidate access when TouchID fingerprint set is changed
      &error
    );

    // store a token by saving it using the keychain with only touch ID accessibility
    NSDictionary *dict = @{
                           (__bridge id)(kSecClass): (__bridge id)kSecClassGenericPassword,
                           (__bridge id)kSecAttrService: [self serviceName],
                           (__bridge id)kSecAttrAccessControl: (__bridge_transfer id)sacObject,
                           (__bridge id)kSecAttrAccount: email,
                           (__bridge id)kSecValueData: [token dataUsingEncoding:NSUTF8StringEncoding]
                           };

    OSStatus err = SecItemAdd((__bridge CFDictionaryRef) dict, NULL);
    NSLog(@"Store Credentials For Account result: %@", [self keychainErrorToString:err]);

    if(err == errSecSuccess || err == errSecDuplicateItem){
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        [defaults setObject:@YES forKey:[self userDefaultsStorageIndicatorKey]];
        [defaults synchronize];
    }

}

RCT_EXPORT_METHOD(hasCredentialsWithCallback:(RCTResponseSenderBlock)callback){

    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSNumber *isStored = [defaults objectForKey:[self userDefaultsStorageIndicatorKey]];
    if(isStored && isStored.boolValue == YES){
        callback(@[[NSNull null], @YES]);
        return;
    }
    callback(@[[NSNull null], @NO]);

}

RCT_EXPORT_METHOD(retrieveCredentialsWithTouchIDPrompt:(NSString *)promptString andCallback:(RCTResponseSenderBlock)callback){

    NSDictionary *dict = @{
                           (__bridge id)(kSecClass): (__bridge id)kSecClassGenericPassword,
                           (__bridge id)kSecAttrService: [self serviceName],
                           (__bridge id)kSecReturnAttributes: (__bridge id)kCFBooleanTrue,
                           (__bridge id)kSecUseOperationPrompt: promptString,
                           (__bridge id)kSecReturnData: (__bridge id)kCFBooleanTrue,
                           };


    NSDictionary* found = nil;
    CFTypeRef foundCF = NULL;
    OSStatus err = SecItemCopyMatching((__bridge CFDictionaryRef) dict, (CFTypeRef*)&foundCF);
    NSLog(@"Retrieve Credentials For Account result: %@", [self keychainErrorToString:err]);

    found = (__bridge NSDictionary*)(foundCF);
    if (!found){
        NSError *error = [NSError errorWithDomain:[self serviceName] code:err userInfo:nil];
        id jsError = RCTMakeError([self keychainErrorToString:err], error, nil);
        callback(@[jsError]);
        return;
    }

    // Found
    NSString* username = (NSString*) [found objectForKey:(__bridge id)(kSecAttrAccount)];
    NSString* token = [[NSString alloc] initWithData:[found objectForKey:(__bridge id)(kSecValueData)] encoding:NSUTF8StringEncoding];

    NSDictionary *response = @{
                               @"username": username,
                               @"token": token
                               };
    callback(@[[NSNull null], response]);

}

RCT_EXPORT_METHOD(deleteCredentials){

    NSDictionary *dict = @{
                           (__bridge id)(kSecClass): (__bridge id)kSecClassGenericPassword,
                           (__bridge id)kSecAttrService: [self serviceName]
                           };
    OSStatus status = SecItemDelete((__bridge CFDictionaryRef)dict);
    NSLog(@"Delete Credentials For Account result: %@", [self keychainErrorToString:status]);

    if(status == errSecSuccess){
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        [defaults setObject:@NO forKey:[self userDefaultsStorageIndicatorKey]];
        [defaults synchronize];
    }

}

- (NSString *)keychainErrorToString:(OSStatus)error {
    NSString *message = [NSString stringWithFormat:@"%ld", (long)error];

    switch (error) {
        case errSecSuccess:
            message = @"success";
            break;

        case errSecDuplicateItem:
            message = @"error item already exists";
            break;

        case errSecItemNotFound :
            message = @"error item not found";
            break;

        case errSecAuthFailed:
            message = @"error item authentication failed";
            break;

        default:
            break;
    }

    return message;
}


RCT_EXPORT_METHOD(test)
{
  // Your implementation here
}

@end
