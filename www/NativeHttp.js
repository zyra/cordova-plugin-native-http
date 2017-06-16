"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var cordova_1 = require("cordova");
function getHttpRequestHandler(callback) {
    return function (response) {
        callback(new HttpResponse(response));
    };
}
function Cordova(config) {
    return function (target, methodName, descriptor) {
        return {
            value: function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                if (config && config.httpRequest) {
                    if (!args[1])
                        args[1] = {};
                    if (!args[2])
                        args[2] = {};
                    for (var prop in this._defaultHeaders) {
                        if (!args[2][prop]) {
                            args[2][prop] = this._defaultHeaders[prop];
                        }
                    }
                    if (['post', 'put', 'patch'].indexOf(methodName) > -1) {
                        if (typeof args[3] !== 'boolean') {
                            args[3] = true;
                            for (var prop in args[2]) {
                                if (String(prop).toLowerCase() === 'content-type' && String(args[2][prop]).toLowerCase() !== 'application/json') {
                                    args[3] = false;
                                }
                            }
                        }
                    }
                    else {
                        args[3] = false;
                    }
                }
                return new Promise(function (resolve, reject) {
                    if (config && config.httpRequest) {
                        cordova_1.exec(getHttpRequestHandler(resolve), getHttpRequestHandler(reject), SERVICE_NAME, methodName, args);
                    }
                    else {
                        cordova_1.exec(resolve, reject, SERVICE_NAME, methodName, args);
                    }
                });
            },
            enumerable: true,
            configurable: false
        };
    };
}
function merge(defaults, mergeWith) {
    for (var prop in mergeWith) {
        defaults[prop] = mergeWith[prop];
    }
    return defaults;
}
var SERVICE_NAME = 'NativeHttp';
var HttpResponse = (function () {
    function HttpResponse(res) {
        if (typeof res === 'string') {
            this.error = res;
            return;
        }
        this.status = res.status || 0;
        this.headers = res.headers || [];
        !!res.body && (this.body = res.body);
        !!res.error && (this.error = res.error);
    }
    HttpResponse.prototype.json = function () {
        try {
            return JSON.parse(this.body);
        }
        catch (e) {
            return this.body;
        }
    };
    return HttpResponse;
}());
var CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST = {
    httpRequest: true
};
var DEFAULT_UPLOAD_OPTIONS = {
    fileKey: 'file',
    fileName: 'image.jpg',
    mimeType: 'image/jpeg',
    chunkedMode: true,
    httpMethod: 'POST'
};
var NativeHttp = (function () {
    function NativeHttp() {
        this._defaultOptions = {
            params: {},
            headers: {}
        };
        this._defaultHeaders = {};
    }
    NativeHttp.prototype.getDefaultOptions = function () {
        return this._defaultOptions;
    };
    NativeHttp.prototype.setDefaultOptions = function (options) {
        if (options === void 0) { options = {}; }
        this._defaultOptions = options;
    };
    NativeHttp.prototype.setDefaultHeaders = function (headers) {
        if (headers === void 0) { headers = {}; }
        this._defaultHeaders = headers;
    };
    NativeHttp.prototype.setDefaultHeader = function (key, value) {
        this._defaultHeaders[key] = value;
    };
    NativeHttp.prototype.acceptAllCerts = function (accept) { return; };
    NativeHttp.prototype.enableSSLPinning = function (enable) { return; };
    NativeHttp.prototype.validateDomainName = function (validate) { return; };
    NativeHttp.prototype.pinSSL = function (hostname, publicKey) { };
    NativeHttp.prototype.get = function (path, params, headers) { return; };
    NativeHttp.prototype.post = function (path, body, headers, json) {
        if (json === void 0) { json = true; }
        return;
    };
    NativeHttp.prototype.put = function (path, body, headers, json) {
        if (json === void 0) { json = true; }
        return;
    };
    NativeHttp.prototype.patch = function (path, body, headers, json) {
        if (json === void 0) { json = true; }
        return;
    };
    NativeHttp.prototype.head = function (path, params, headers) { return; };
    NativeHttp.prototype.delete = function (path, params, headers) { return; };
    NativeHttp.prototype.download = function (remotePath, localPath, params, headers, onProgress) {
        headers = merge(this._defaultHeaders, headers);
        params = params || {};
        if (!remotePath) {
            return Promise.reject({ error: 'You must provide a remote path.' });
        }
        else if (!localPath) {
            return Promise.reject({ error: 'You must provide a local path.' });
        }
        return new Promise(function (resolve, reject) {
            cordova_1.exec(function (data) {
                if (!!data.total && typeof onProgress === 'function') {
                    onProgress(data);
                }
                else {
                    resolve(data);
                }
            }, reject, SERVICE_NAME, 'download', [remotePath, localPath, params, headers]);
        });
    };
    NativeHttp.prototype.upload = function (remotePath, localPath, options) {
        if (options === void 0) { options = {}; }
        if (!remotePath) {
            return Promise.reject({ error: 'You must provide a remote path.' });
        }
        else if (!localPath) {
            return Promise.reject({ error: 'You must provide a local path.' });
        }
        options = merge(DEFAULT_UPLOAD_OPTIONS, options);
        options.headers = merge(this._defaultHeaders, options.headers);
        options.params = options.params || {};
        return new Promise(function (resolve, reject) {
            cordova_1.exec(resolve, reject, SERVICE_NAME, 'upload', [remotePath, localPath, options]);
        });
    };
    return NativeHttp;
}());
__decorate([
    Cordova(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Boolean]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "acceptAllCerts", null);
__decorate([
    Cordova(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Boolean]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "enableSSLPinning", null);
__decorate([
    Cordova(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Boolean]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "validateDomainName", null);
__decorate([
    Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "get", null);
__decorate([
    Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object, Boolean]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "post", null);
__decorate([
    Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object, Boolean]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "put", null);
__decorate([
    Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object, Boolean]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "patch", null);
__decorate([
    Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "head", null);
__decorate([
    Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "delete", null);
module.exports = new NativeHttp();
