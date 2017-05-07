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
        [client GET:[command.arguments objectAtIndex:0] parameters:nil progress:nil success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
            NSDictionary *res = @{
                                  @"status": @200,
                                  @"headers": @{},
                                  @"body": responseObject
                                  };
            
            CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:res];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
            
            NSDictionary *res = @{ @"error": [error localizedDescription] };
            CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:res];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }];
    }];
    
}

@end
