import IComponentOptions = angular.IComponentOptions;
import {RequestService} from '../request.service';
import {Request} from '../request';

export class RequestHeaderComponent implements IComponentOptions {
  public templateUrl: string = '/request/header/header.component.html';
  public controller: Function = RequestHeaderController;
  public bindings: any = {
    request: '='
  };
}

class RequestHeaderController {
  public static $inject: string[] = ['RequestService'];

  public request: Request;
  public hover: boolean = false;

  public constructor(private requestService: RequestService) {}

  public validateDescription(data): String {
    if (!(data.toString().length > 0)) {
      return ' ';
    }
  }

  public saveRequest(data): void {
    this.requestService.saveRequest(this.request).then((request: Request) => {
      this.request = request;
    });
  }
}
