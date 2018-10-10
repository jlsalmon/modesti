import IComponentOptions = angular.IComponentOptions;
import IHttpService = angular.IHttpService;
import {ServerInfoService} from '../server/server.info';

export class LogoComponent implements IComponentOptions {
  public template: string = '<a ui-sref="home" class="logo"> {{$ctrl.getTitle()}}  <small style="font-size: 50%">{{$ctrl.getVersion()}}</small></a>';
  public controller: Function = LogoController;
}


class LogoController {
  public static $inject: string[] = ['ServerInfoService'];

  constructor(private serverInfoService : ServerInfoService) { }

  public getTitle(): string {
    return this.serverInfoService.title;
  }

  public getVersion(): string {
    return this.serverInfoService.version;
  }
}
