import IHttpService = angular.IHttpService;

export class ServerInfoService {
    public static $inject: string[] = ['$http'];
    public title: string;
    public version: string;
    public homePage: string; 
  
    constructor(private $http: IHttpService) {
      $http.get('/api/plugins').then((response: any) => {
        this.title = response.data.name === undefined ? "modesti" : response.data.name;
        let version: string = response.data.version;
        this.version = version === 'dev' ? version : 'v' + version;
        let homePage = response.data.homePage === undefined ? '/home/home.component.html' : response.data.homePage;
        this.homePage = homePage;
      });
    }
  }
  