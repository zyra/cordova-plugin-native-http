import { exec } from 'cordova';

interface IRequestOptions {
    params?: { [key: string]: any };
    headers?: { [key: string]: any };
    body?: any;
}

interface IHttpResponse {
    status: number;
    headers: any;
    body: any;
    error?: string;
}

interface ICordovaDecoratorConfig {
    defaultArgs?: any[];
    mergeDefaults?: boolean[];
    httpRequest?: boolean;
}

interface IFileUploadOptions {
    fileKey?: string;
    fileName?: string;
    httpMethod?: 'PUT' | 'POST';
    mimeType?: string;
    params?: any;
    chunkedMode?: boolean;
    headers?: any;
}

function getHttpRequestHandler(callback: Function) {
    return (response: IHttpResponse) => {
        callback(new HttpResponse(response));
    };
}

function Cordova(config?: ICordovaDecoratorConfig) {
    return function(target: NativeHttp, methodName: string, descriptor: TypedPropertyDescriptor<any>): TypedPropertyDescriptor<any> {
        return {
            value: function(...args: any[]) {

                // if (config && config.defaultArgs) {
                //     config.defaultArgs.forEach((value: any, index: number) => {
                //        if (config.mergeDefaults && config.mergeDefaults[index] === true) {
                //            for (let prop in value) {
                //                if (!args[index][prop]) {
                //                    args[index][prop] = value;
                //                }
                //            }
                //        } else if (!args[index]) {
                //            args[index] = value;
                //        }
                //     });
                // }

                if (config && config.httpRequest) {

                    // do not send blank params/headers to native code
                    if (!args[1]) args[1] = {};
                    if (!args[2]) args[2] = {};

                    // apply default headers
                    for (let prop in this._defaultHeaders) {
                        if (!args[2][prop]) {
                            args[2][prop] = this._defaultHeaders[prop];
                        }
                    }

                    if (['post', 'put', 'patch'].indexOf(methodName) > -1) {
                        if (typeof args[3] !== 'boolean') {

                            // default to json body
                            args[3] = true;

                            // check for headers to see if there's a content type
                            for (let prop in args[2]) {
                                if (String(prop).toLowerCase() === 'content-type' && String(args[2][prop]).toLowerCase() !== 'application/json') {
                                    // use x-www-url-encoded instead
                                    args[3] = false;
                                }
                            }

                        }
                    } else {
                        args[3] = false;
                    }

                }

                return new Promise<any>((resolve, reject) => {
                    if (config && config.httpRequest) {
                        exec(getHttpRequestHandler(resolve), getHttpRequestHandler(reject), SERVICE_NAME, methodName, args);
                    } else {
                        exec(resolve, reject, SERVICE_NAME, methodName, args);
                    }
                });
            },
            enumerable: true,
            configurable: false
        };
    };
}

const SERVICE_NAME: string = 'NativeHttp';

class HttpResponse {

    status: number;
    headers: any;
    body: any;
    error: any;

    constructor(res: IHttpResponse) {
        if (typeof res === 'string') {
            this.error = res;
            return;
        }
        this.status = res.status || 0;
        this.headers = res.headers || [];
        !!res.body && (this.body = res.body);
        !!res.error && (this.error = res.error);
    }

    json(): any {
        try {
            return JSON.parse(this.body);
        } catch (e) {
            return this.body;
        }
    }

}

const CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST: ICordovaDecoratorConfig = {
    httpRequest: true
};

const DEFAULT_UPLOAD_OPTIONS: IFileUploadOptions = {
    fileKey: 'file',
    fileName: 'image.jpg',
    mimeType: 'image/jpeg',
    chunkedMode: true,
    httpMethod: 'POST'
};

class NativeHttp {

    private _defaultOptions: IRequestOptions = {
        params: {},
        headers: {}
    };

    private _defaultHeaders: any = {};

    getDefaultOptions(): IRequestOptions {
        return this._defaultOptions;
    }

    setDefaultOptions(options: IRequestOptions = {}) {
        this._defaultOptions = options;
    }

    setDefaultHeaders(headers: any = {}) {
        this._defaultHeaders = headers;
    }

    @Cordova()
    acceptAllCerts(accept: boolean): Promise<void> { return; }

    @Cordova()
    enableSSLPinning(enable: boolean): Promise<void> { return; }

    @Cordova()
    validateDomainName(validate: boolean): Promise<void> { return; }

    @Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST)
    get(path: string, params?: any, headers?: any): Promise<HttpResponse> { return; }

    @Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST)
    post(path: string, body?: any, headers?: any, json: boolean = true): Promise<HttpResponse> { return; }

    @Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST)
    put(path: string, body?: any, headers?: any, json: boolean = true): Promise<HttpResponse> { return; }

    @Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST)
    patch(path: string, body?: any, headers?: any, json: boolean = true): Promise<HttpResponse> { return; }

    @Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST)
    head(path: string, params?: any, headers?: any): Promise<HttpResponse> { return; }

    @Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST)
    delete(path: string, params?: any, headers?: any): Promise<HttpResponse> { return; }

    @Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST)
    download(remotePath: string, localPath: string, params?: any, headers?: any): Promise<HttpResponse> { return; }

    upload(remotePath: string, localPath: string, options: IFileUploadOptions = {}): Promise<HttpResponse> {
        if (!remotePath) {
            return Promise.reject({ error: 'You must provide a remote path.' });
        } else if(!localPath) {
            return Promise.reject({ error: 'You must provide a local path.' });
        }

        for (let prop in DEFAULT_UPLOAD_OPTIONS) {
            if (!options[prop]) {
                options[prop] = DEFAULT_UPLOAD_OPTIONS[prop];
            }
        }

        return new Promise<HttpResponse>((resolve, reject) => {
            exec(resolve, reject, SERVICE_NAME, 'upload', [remotePath, localPath, options]);
        });
    }

}

export = new NativeHttp();
