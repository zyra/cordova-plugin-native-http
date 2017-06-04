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
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                if (config && config.httpRequest) {
                    for (var prop in this._defaultHeaders) {
                        if (this._defaultHeaders.hasOwnProperty(prop) && !args[2].hasOwnProperty(prop)) {
                            args[2][prop] = this._defaultHeaders[prop];
                        }
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
    NativeHttp.prototype.acceptAllCerts = function (accept) { return; };
    NativeHttp.prototype.enableSSLPinning = function (enable) { return; };
    NativeHttp.prototype.validateDomainName = function (validate) { return; };
    NativeHttp.prototype.get = function (path, params, headers) { return; };
    NativeHttp.prototype.post = function (path, body, headers) { return; };
    NativeHttp.prototype.put = function (path, body, headers) { return; };
    NativeHttp.prototype.patch = function (path, body, headers) { return; };
    NativeHttp.prototype.head = function (path, body, headers) { return; };
    NativeHttp.prototype.delete = function (path, body, headers) { return; };
    NativeHttp.prototype.download = function (path, body, headers) { return; };
    NativeHttp.prototype.upload = function (path, body, headers) { return; };
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
    __metadata("design:paramtypes", [String, Object, Object]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "post", null);
__decorate([
    Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "put", null);
__decorate([
    Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object]),
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
__decorate([
    Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "download", null);
__decorate([
    Cordova(CORDOVA_DECORATOR_OPTIONS_HTTP_REQUEST),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "upload", null);
module.exports = new NativeHttp();
