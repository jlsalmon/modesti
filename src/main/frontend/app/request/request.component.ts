import {Request} from './request';
import {Schema} from '../schema/schema';
import {Category} from '../schema/category/category';
import IScope = angular.IScope;

export class RequestComponent implements ng.IComponentOptions {
  public templateUrl: string = '/request/request.component.html';
  public controller: Function = RequestController;
  public bindings: any = {
    request: '=',
    children: '=',
    schema: '=',
    tasks: '=',
    signals: '=',
    history: '='
  };
}

class RequestController {
  public static $inject: string[] = ['$scope', '$localStorage'];

  public request: Request;
  public schema: Schema;

  public constructor(private $scope: IScope, private $localStorage: any) {}
}
