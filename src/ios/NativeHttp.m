/********* CordovaHttp.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

#import <AFNetworking.h>

@interface NativeHttp : CDVPlugin
@property (nonatomic, retain) AFHTTPSessionManager *client;
- (void)coolMethod:(CDVInvokedUrlCommand*)command;
@end

@implementation NativeHttp
@synthesize client;

- (void)pluginInitialize
{
    NSLog(@"~~~~~~~~~~NATIVE HTTP RUNNING~~~~~~~~~~~~~~~~~");
    NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
    client = [[AFHTTPSessionManager alloc] initWithSessionConfiguration:config];

}

- (void)get:(CDVInvokedUrlCommand *) command
{
    
    
    NSLog(@"~~~~~~~~~~GET REQUEST HTTP RUNNING~~~~~~~~~~~~~~~~~");

    [client GET:[command.arguments objectAtIndex:0] parameters:nil progress:nil success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        NSDictionary *res = @{
                              @"status": @200,
                              @"headers": @{},
                              @"body": responseObject
                              };
        
        CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:res];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        
        
    }];
}

- (void)coolMethod:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];

    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
