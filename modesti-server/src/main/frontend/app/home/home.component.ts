import IComponentOptions = angular.IComponentOptions;
import IHttpService = angular.IHttpService;
import {ServerInfoService} from '../server/server.info';

export class HomeComponent implements IComponentOptions {
  public template: string = '<ng-include src="$ctrl.getHomePage()"/>';
  public controller: Function = HomeController;
}

class HomeController {
  public static $inject: string[] = ['ServerInfoService'];

  constructor(private serverInfoService : ServerInfoService) {}

  public getHomePage(): string {
    return this.serverInfoService.homePage;
  }
}
