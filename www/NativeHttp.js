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
        callback(new Response(response));
    };
}
function Cordova() {
    return function (target, methodName, descriptor) {
        return {
            value: function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i] = arguments[_i];
                }
                return new Promise(function (resolve, reject) {
                    cordova_1.exec(resolve.bind(resolve), reject.bind(reject), SERVICE_NAME, methodName, args);
                });
            }
        };
    };
}
var SERVICE_NAME = 'NativeHttp';
var Response = (function () {
    function Response(_response) {
        this._response = _response;
    }
    Object.defineProperty(Response.prototype, "status", {
        get: function () {
            return this._response.status;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(Response.prototype, "headers", {
        get: function () {
            return this._response.headers;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(Response.prototype, "body", {
        get: function () {
            return this._response.body;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(Response.prototype, "error", {
        get: function () {
            return this._response.error;
        },
        enumerable: true,
        configurable: true
    });
    Response.prototype.json = function () {
        try {
            this._response.body = JSON.parse(this._response.body);
        }
        catch (e) { }
        return this;
    };
    return Response;
}());
var NativeHttp = (function () {
    function NativeHttp() {
        this._defaultOptions = {
            params: {},
            headers: {}
        };
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
        this._defaultOptions.headers = headers;
    };
    NativeHttp.prototype.get = function (path, params, headers) { return; };
    NativeHttp.prototype.post = function (path, body, headers) { return; };
    return NativeHttp;
}());
__decorate([
    Cordova(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "get", null);
__decorate([
    Cordova(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, Object, Object]),
    __metadata("design:returntype", Promise)
], NativeHttp.prototype, "post", null);
module.exports = new NativeHttp();
