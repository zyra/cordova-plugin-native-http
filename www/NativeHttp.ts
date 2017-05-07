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

function getHttpRequestHandler(callback: Function) {
    return (response: IHttpResponse) => {
        callback(new HttpResponse(response));
    };
}

function Cordova() {
    return function(target: NativeHttp, methodName: string, descriptor: TypedPropertyDescriptor<any>): TypedPropertyDescriptor<any> {
      return {
          value: function(...args: any[]) {
              return new Promise<any>((resolve, reject) => {
                  exec(getHttpRequestHandler(resolve), getHttpRequestHandler(reject), SERVICE_NAME, methodName, args);
              });
          }
      };
    };
}

const SERVICE_NAME: string = 'NativeHttp';

class HttpResponse {

    status: number;
    headers: any[];
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

    json(): HttpResponse {
        try {
            this.body = JSON.parse(this.body);
        } catch (e) {}
        return this;
    }

}

class NativeHttp {

    private _defaultOptions: IRequestOptions = {
        params: {},
        headers: {}
    };

    getDefaultOptions(): IRequestOptions {
        return this._defaultOptions;
    }

    setDefaultOptions(options: IRequestOptions = {}) {
        this._defaultOptions = options;
    }

    setDefaultHeaders(headers: any = {}) {
        this._defaultOptions.headers = headers;
    }

    @Cordova()
    get(path: string, params?: any, headers?: any): Promise<HttpResponse> { return; }

    @Cordova()
    post(path: string, body?: any, headers?: any): Promise<HttpResponse> { return; }

}

export = new NativeHttp();
