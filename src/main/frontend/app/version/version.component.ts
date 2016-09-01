export class VersionComponent implements ng.IComponentOptions {
  public template: string = '{{$ctrl.version}}';
  public controller: Function = VersionController;
}

class VersionController {
  public static $inject: string[] = ['$http'];

  public version: string;

  constructor(private $http: any) {
    $http.get('/api/plugins').then((response: any) => {
      let version: string = response.data.version;
      this.version = version === 'dev' ? version : 'v' + version;
      console.log('modesti ' + this.version);
    });
  }
}
