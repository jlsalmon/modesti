import {RequestService} from './request.service';
import {SchemaService} from '../schema/schema.service';
import {TaskService} from '../task/task.service';

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
  public static $inject: string[] = ['$stateParams'];

  /** The handsontable instance */
  public table: any = {};

  public request:  any;
  public children: any;
  public schema: any;
  public tasks: any;
  public signals: any;
  public history: any;

  public constructor(private $stateParams: any) {}
}
