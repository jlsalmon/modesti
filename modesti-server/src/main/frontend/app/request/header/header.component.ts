import IComponentOptions = angular.IComponentOptions;
import {RequestService} from '../request.service';
import {Request} from '../request';
import {AuthService} from '../../auth/auth.service';

export class RequestHeaderComponent implements IComponentOptions {
  public templateUrl: string = '/request/header/header.component.html';
  public controller: Function = RequestHeaderController;
  public bindings: any = {
    request: '='
  };
}

class RequestHeaderController {
  public static $inject: string[] = ['RequestService', 'AuthService'];

  public request: Request;
  public hover: boolean = false;

  public constructor(private requestService: RequestService, private authService: AuthService) {}

  public validateDescription(data: string): string {
    if (!(data.toString().length > 0)) {
      return ' ';
    }
  }

  public saveRequest(): void {
    this.requestService.saveRequest(this.request).then((request: Request) => {
      this.request = request;
    });
  }

  public assignCreator(): void {
    this.requestService.assignCreator(this.request).then((newRequest: Request) => {
      this.request.creator = newRequest.creator;
    });
  }
  
  public isCurrentUserOwner(): boolean {
    return this.requestService.isCurrentUserOwner(this.request);
  }
  
  public getRequestCreator(): string {
    return this.request.creator;
  }
}
