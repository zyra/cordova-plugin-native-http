import { exec } from 'cordova';

interface RequestOptions {
    params?: { [key: string]: any };
    headers?: { [key: string]: any };
    body?: any;
}

function Cordova() {
    return function(target: NativeHttp, methodName: string, descriptor: TypedPropertyDescriptor<any>): TypedPropertyDescriptor<any> {
      return {
          value: function(...args: any[]) {
              return new Promise<any>((resolve, reject) => {
                  exec(resolve.bind(resolve), reject.bind(reject), SERVICE_NAME, methodName, args);
              });
          }
      };
    };
}

const SERVICE_NAME: string = 'NativeHttp';

class NativeHttp {

    private _defaultOptions: RequestOptions = {
        params: {},
        headers: {}
    };

    getDefaultOptions(): RequestOptions {
        return this._defaultOptions;
    }

    setDefaultOptions(options: RequestOptions = {}) {
        this._defaultOptions = options;
    }

    setDefaultHeaders(headers: any = {}) {
        this._defaultOptions.headers = headers;
    }

    @Cordova()
    get(path: string, params?: any, headers?: any): Promise<any> { return; }

    @Cordova()
    post(path: string, body?: any, headers?: any): Promise<any> { return; }

}

export = new NativeHttp();
