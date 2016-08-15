export class RequestHeaderComponent implements ng.IComponentOptions {
  public templateUrl:string = '/request/header/header.component.html';
  public controller:Function = RequestHeaderController;
  public bindings:any = {
    request: '='
  };
}

function RequestHeaderController() {}
