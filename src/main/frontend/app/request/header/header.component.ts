import IComponentOptions = angular.IComponentOptions;

export class RequestHeaderComponent implements IComponentOptions {
  public templateUrl: string = '/request/header/header.component.html';
  public controller: Function = RequestHeaderController;
  public bindings: any = {
    request: '='
  };
}

class RequestHeaderController {}
