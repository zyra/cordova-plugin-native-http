#import <Cordova/CDV.h>
#import <AFNetworking.h>

@interface NativeHttp : CDVPlugin
@property (nonatomic, retain) AFHTTPSessionManager *client;
@property (nonatomic, retain) AFSecurityPolicy *securityPolicy;
- (void) enableSSLPinning: (CDVInvokedUrlCommand *) command;
- (void) get: (CDVInvokedUrlCommand *) command;
- (void) post: (CDVInvokedUrlCommand *) command;
- (void) put: (CDVInvokedUrlCommand *) command;
- (void) delete: (CDVInvokedUrlCommand *) command;
- (void) head: (CDVInvokedUrlCommand *) command;
- (void) patch: (CDVInvokedUrlCommand *) command;
- (void) download: (CDVInvokedUrlCommand *) command;
- (void) upload: (CDVInvokedUrlCommand *) command;
@end

@implementation NativeHttp
@synthesize client;
@synthesize securityPolicy;

- (void)pluginInitialize
{
    NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
    client = [[AFHTTPSessionManager alloc] initWithSessionConfiguration:config];
    client.responseSerializer = [AFHTTPResponseSerializer serializer];
}

- (void)enableSSLPinning: (CDVInvokedUrlCommand *) command
{
    if ([[command.arguments objectAtIndex:0] boolValue]) {
        self.securityPolicy = [AFSecurityPolicy policyWithPinningMode:AFSSLPinningModeCertificate];
    } else {
        self.securityPolicy = [AFSecurityPolicy policyWithPinningMode:AFSSLPinningModeNone];
    }
    
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void (^)(NSURLSessionTask *, id _Nullable)) getSuccessHandler:(CDVInvokedUrlCommand *) command
{
    return ^(NSURLSessionTask *operation, id  _Nullable responseObject) {
        NSHTTPURLResponse *response = (NSHTTPURLResponse *) [operation response];
        NSNumber *statusCode = [NSNumber numberWithInteger:[response statusCode]];
        NSDictionary *res = @{
                              @"status": statusCode,
                              @"headers": [response allHeaderFields],
                              @"body": responseObject
                              };
        
        CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:res];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    };
}

- (void (^)(NSURLSessionTask *, NSError * _Nonnull)) getErrorHandler: (CDVInvokedUrlCommand *) command
{
    return ^(NSURLSessionTask *operation, NSError * _Nonnull error) {
        // on error
        NSHTTPURLResponse *response = (NSHTTPURLResponse *) [operation response];
        NSNumber *statusCode = [NSNumber numberWithInteger:[response statusCode]];
        NSString* ErrorResponse = [[NSString alloc] initWithData:(NSData *)error.userInfo[AFNetworkingOperationFailingURLResponseDataErrorKey] encoding:NSUTF8StringEncoding];
        
        if (ErrorResponse == nil) {
            ErrorResponse = error.localizedDescription;
        }
        
        NSDictionary *res = @{
                              @"status": statusCode,
                              @"headers": [response allHeaderFields],
                              @"body": ErrorResponse
                              };
        
        CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:res];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    };
}

- (void)get:(CDVInvokedUrlCommand *) command
{
    [self.commandDelegate runInBackground:^{
        [client GET:[command.arguments objectAtIndex:0] parameters:nil progress:nil success:[self getSuccessHandler:command] failure:[self getErrorHandler:command]];
    }];
}

- (void)post:(CDVInvokedUrlCommand *) command
{
    [self.commandDelegate runInBackground:^{
        AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
        manager.requestSerializer = [AFJSONRequestSerializer serializer];
        NSDictionary *headers = [command.arguments objectAtIndex:2];
        if (headers != nil) {
            [headers enumerateKeysAndObjectsUsingBlock:^(NSString*  _Nonnull key, NSString*  _Nonnull obj, BOOL * _Nonnull stop) {
                [manager.requestSerializer setValue:obj forHTTPHeaderField:key];
            }];
        }
        
        
        
//        [manager.requestSerializer setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
        
        [manager POST:[command.arguments objectAtIndex:0] parameters:[command.arguments objectAtIndex:1] progress:nil success:[self getSuccessHandler:command] failure:[self getErrorHandler:command]];
    }];
}

- (void)put:(CDVInvokedUrlCommand *) command {}
- (void)head:(CDVInvokedUrlCommand *) command {}
- (void)delete:(CDVInvokedUrlCommand *) command {}
- (void)patch:(CDVInvokedUrlCommand *) command {}
- (void)download:(CDVInvokedUrlCommand *) command {}
- (void)upload:(CDVInvokedUrlCommand *) command {}

@end
