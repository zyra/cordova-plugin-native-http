#import <Cordova/CDV.h>
#import <AFNetworking.h>

// taken from https://gist.github.com/swizzlr/8478966
@interface LMLPlaintextResponseSerializer : AFHTTPResponseSerializer

@end

@implementation LMLPlaintextResponseSerializer

- (id)responseObjectForResponse:(NSURLResponse *)response data:(NSData *)data error:(NSError *__autoreleasing *)error {
    [super responseObjectForResponse:response data:data error:error]; //BAD SIDE EFFECTS BAD BUT NECESSARY TO CATCH 500s ETC
    NSStringEncoding encoding = CFStringConvertEncodingToNSStringEncoding(CFStringConvertIANACharSetNameToEncoding((__bridge CFStringRef)([response textEncodingName] ?: @"utf-8")));
    return [[NSString alloc] initWithData:data encoding:encoding];
}

@end

@interface NativeHttp : CDVPlugin
//@property (nonatomic, retain) AFHTTPSessionManager *client;
@property (nonatomic, retain) AFSecurityPolicy *securityPolicy;
- (void) enableSSLPinning: (CDVInvokedUrlCommand *) command;
- (void) acceptAllCerts: (CDVInvokedUrlCommand *) command;
- (void) validateDomainName: (CDVInvokedUrlCommand *) command;
- (void) get: (CDVInvokedUrlCommand *) command;
- (void) post: (CDVInvokedUrlCommand *) command;
- (void) put: (CDVInvokedUrlCommand *) command;
- (void) delete: (CDVInvokedUrlCommand *) command;
- (void) head: (CDVInvokedUrlCommand *) command;
- (void) patch: (CDVInvokedUrlCommand *) command;
- (void) download: (CDVInvokedUrlCommand *) command;
- (void) upload: (CDVInvokedUrlCommand *) command;
@end

// influenced by https://github.com/wymsee/cordova-HTTP/blob/master/src/ios/CordovaHttpPlugin.m
@implementation NativeHttp
//@synthesize client;
@synthesize securityPolicy;

- (void) pluginInitialize
{
   // NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
    //client = [[AFHTTPSessionManager alloc] initWithSessionConfiguration:config];
    self.securityPolicy = [AFSecurityPolicy policyWithPinningMode:AFSSLPinningModeNone];
}

- (void) enableSSLPinning:(CDVInvokedUrlCommand *) command
{
    if ([[command.arguments objectAtIndex:0] boolValue]) {
        self.securityPolicy = [AFSecurityPolicy policyWithPinningMode:AFSSLPinningModeCertificate];
    } else {
        self.securityPolicy = [AFSecurityPolicy policyWithPinningMode:AFSSLPinningModeNone];
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void) acceptAllCerts:(CDVInvokedUrlCommand *) command {
    securityPolicy.allowInvalidCertificates = [[command.arguments objectAtIndex:0] boolValue];
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void) validateDomainName:(CDVInvokedUrlCommand*)command {
    securityPolicy.validatesDomainName = [[command.arguments objectAtIndex:0] boolValue];
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (NSDictionary *) getResponseObject: (NSURLSessionTask *) operation withResponseObject: (id _Nullable) body
{
    NSHTTPURLResponse *response = (NSHTTPURLResponse *) [operation response];
    NSNumber *statusCode = [NSNumber numberWithInteger:[response statusCode]];
    NSDictionary *res = @{
                          @"status": statusCode,
                          @"headers": [response allHeaderFields],
                          @"body": body
                          };
    
    return res;
}

- (void (^)(NSURLSessionTask *, id _Nullable)) getSuccessHandler:(CDVInvokedUrlCommand *) command
{
    return ^(NSURLSessionTask *operation, id  _Nullable responseObject) {
        CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self getResponseObject:operation withResponseObject:responseObject]];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    };
}

- (void (^)(NSURLSessionTask *, NSError * _Nonnull)) getErrorHandler: (CDVInvokedUrlCommand *) command
{
    return ^(NSURLSessionTask *operation, NSError * _Nonnull error) {
        // on error
        NSHTTPURLResponse *response = (NSHTTPURLResponse *) operation.response;
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

- (void) setHeaders: (NSDictionary *) headers forManager: (AFHTTPSessionManager *) manager
{
    if (headers != nil) {
        [headers enumerateKeysAndObjectsUsingBlock:^(NSString*  _Nonnull key, NSString*  _Nonnull obj, BOOL * _Nonnull stop) {
            [manager.requestSerializer setValue:obj forHTTPHeaderField:key];
        }];
    }
}

- (AFHTTPSessionManager *) getManager: (CDVInvokedUrlCommand *) command
{
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    manager.securityPolicy = self.securityPolicy;
    if ([[command.arguments objectAtIndex:3] boolValue] == TRUE) {
        manager.requestSerializer = [AFJSONRequestSerializer serializer];
    }
    
    [self setHeaders:[command.arguments objectAtIndex:2] forManager:manager];
    
    return manager;
}

- (void) get:(CDVInvokedUrlCommand *) command
{
    [self.commandDelegate runInBackground:^{
        [[self getManager:command] GET:[command.arguments objectAtIndex:0] parameters:[command.arguments objectAtIndex:1] progress:nil success:[self getSuccessHandler:command] failure:[self getErrorHandler:command]];
    }];
}

- (void) post:(CDVInvokedUrlCommand *) command
{
    [self.commandDelegate runInBackground:^{
        [[self getManager:command] POST:[command.arguments objectAtIndex:0] parameters:[command.arguments objectAtIndex:1] progress:nil success:[self getSuccessHandler:command] failure:[self getErrorHandler:command]];
    }];
}

- (void) put:(CDVInvokedUrlCommand *) command {
    [self.commandDelegate runInBackground:^{
        [[self getManager:command] PUT:[command.arguments objectAtIndex:0] parameters:[command.arguments objectAtIndex:1] success:[self getSuccessHandler:command] failure:[self getErrorHandler:command]];
    }];
}

- (void) head:(CDVInvokedUrlCommand *) command {
    [self.commandDelegate runInBackground:^{
        [[self getManager:command] HEAD:[command.arguments objectAtIndex:0] parameters:[command.arguments objectAtIndex:1] success:^(NSURLSessionTask *operation) {
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self getResponseObject:operation withResponseObject:nil]];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        } failure:[self getErrorHandler:command]];
        
    }];
}

- (void) delete:(CDVInvokedUrlCommand *) command {
    [self.commandDelegate runInBackground:^{
        [[self getManager:command] DELETE:[command.arguments objectAtIndex:0] parameters:[command.arguments objectAtIndex:1] success:[self getSuccessHandler:command] failure:[self getErrorHandler:command]];
    }];
}

- (void) patch:(CDVInvokedUrlCommand *) command {
    [self.commandDelegate runInBackground:^{
        [[self getManager:command] PATCH:[command.arguments objectAtIndex:0] parameters:[command.arguments objectAtIndex:1] success:[self getSuccessHandler:command] failure:[self getErrorHandler:command]];
    }];
}

- (void) download:(CDVInvokedUrlCommand *) command {
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    manager.securityPolicy = self.securityPolicy;
    [self setHeaders:[command.arguments objectAtIndex:2] forManager:manager];

    
    
}

- (void) upload:(CDVInvokedUrlCommand *) command {
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    manager.securityPolicy = self.securityPolicy;
    [self setHeaders:[command.arguments objectAtIndex:2] forManager:manager];

    NSURLRequest *uploadRequest = [[AFHTTPRequestSerializer serializer] multipartFormRequestWithMethod:@"POST" URLString:[command.arguments objectAtIndex:0] parameters:nil constructingBodyWithBlock:^(id<AFMultipartFormData>  _Nonnull formData) {
        
    } error:nil];
    
    NSURLSessionUploadTask *uploadTask = [manager uploadTaskWithStreamedRequest:uploadRequest progress:^(NSProgress * _Nonnull uploadProgress) {
        
    } completionHandler:^(NSURLResponse * _Nonnull response, id  _Nullable responseObject, NSError * _Nullable error) {
        
    }];
    
    [uploadTask resume];
    
}

@end
