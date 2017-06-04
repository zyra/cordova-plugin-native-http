/********* CordovaHttp.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

#import <AFNetworking.h>

@interface NativeHttp : CDVPlugin
@property (nonatomic, retain) AFHTTPSessionManager *client;
@end

@implementation NativeHttp
@synthesize client;

- (void)pluginInitialize
{
    NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
    client = [[AFHTTPSessionManager alloc] initWithSessionConfiguration:config];

}

- (void)get:(CDVInvokedUrlCommand *) command
{
    
    [self.commandDelegate runInBackground:^{
        [client GET:[command.arguments objectAtIndex:0] parameters:nil progress:nil success:^(NSURLSessionTask *operation, id  _Nullable responseObject) {
            
            NSHTTPURLResponse *response = (NSHTTPURLResponse *) [operation response];
            NSNumber *statusCode = [NSNumber numberWithInteger:[response statusCode]];
            
            
            
            NSDictionary *res = @{
                                  @"status": statusCode,
                                  @"headers": [response allHeaderFields],
                                  @"body": responseObject
                                  };
            
            CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:res];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        } failure:^(NSURLSessionTask *operation, NSError * _Nonnull error) {
            
            NSDictionary *res = @{ @"error": [error localizedDescription] };
            CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:res];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }];
    }];
    
}

- (void)post:(CDVInvokedUrlCommand *) command
{

    [self.commandDelegate runInBackground:^{
       [client POST:[command.arguments objectAtIndex:0] parameters:nil progress:^(NSProgress * _Nonnull uploadProgress) {
           
           // noop
           
       } success:^(NSURLSessionTask *operation, id  _Nullable responseObject) {
           
           // on success
           NSDictionary *res = @{
                                 @"status": @200,
                                 @"headers": @{},
                                 @"body": responseObject
                                 };
           
           CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:res];
           [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
           
       } failure:^(NSURLSessionTask *operation, NSError * _Nonnull error) {
           // on error
           
           NSHTTPURLResponse *response = (NSHTTPURLResponse *) [operation response];
           NSInteger statusCode = [response statusCode];
           NSLog(@"RESPONSE CODE: %i", statusCode);
           
           NSString* ErrorResponse = [[NSString alloc] initWithData:(NSData *)error.userInfo[AFNetworkingOperationFailingURLResponseDataErrorKey] encoding:NSUTF8StringEncoding];
           NSLog(@"%@",ErrorResponse);
           NSDictionary *res = @{ @"error": [error localizedDescription] };
           CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:res];
           [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
       }];
    }];
    
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
    
}

@end
