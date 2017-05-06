# cordova-plugin-native-http
Native HTTP Client for Cordova Apps

Work in progress

---

## Usage will be like:
```js
const http = cordova.plugins.NativeHttp;

http.get('https://path/to/something')
  .then(res => {
    // nice utility to convert res.body to json
    res = res.json();
    
    console.log(res.body);
  })
  .catch(e => {
    // nice utility to convert e.body to json
    e = e.json();
      
    console.log(e.body, e.error);
  });

```

## Goals:

- Build a good native HTTP client for Cordova apps in general 
- Ability to integrate with Ionic 2 apps, by overriding provider or by conditional use. (this functionality might be added through another library, i.e. Ionic Native)
```ts
@NgModule({
  providers: [
    { provide: Http, useClass: NativeHttp }
  ]
})

// OR

@Injectable()
export class APIService {

  private http: any;
  
  constructor(
    http: Http,
    platform: Platform
  ) {
   
    this.http = http;
    
    platform.ready().then(() => {
    
      if (nativePluginIsAvailalbe) {
        
        this.http = NativeHttp
      
      }
    
    
    });
  
  }
  
  
  get(url: string, params?: any, headers?: any): Promise<any> {
  
    return this.http.get(url, params, headers).map(res => res.json());
  
  }

}

```