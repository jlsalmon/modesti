export class VersionComponent implements ng.IComponentOptions {
  public template: string = 'v{{$ctrl.version}}';
  public controller: Function = VersionController;
}

class VersionController {
  public static $inject: string[] = ['$http'];

  public version: string;

  constructor(private $http: any) {
    $http.get('/api/plugins').then((response: any) => {
      this.version = response.data.version;
      console.log('modesti v' + this.version);
    });
  }
}
