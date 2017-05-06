# cordova-plugin-native-http
Native HTTP Client for Cordova Apps

Work in progress

---

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

}

```