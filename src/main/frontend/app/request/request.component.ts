import {RequestService} from './request.service';
import {SchemaService} from '../schema/schema.service';
import {TaskService} from '../task/task.service';

export class RequestComponent implements ng.IComponentOptions {
  public templateUrl:string = '/request/request.component.html';
  public controller:Function = RequestController;
  public bindings:any = {
    request: '=',
    children: '=',
    schema: '=',
    tasks: '=',
    signals: '=',
    history: '=',
  };
}

class RequestController {
  public static $inject:string[] = ['$stateParams'];

  /** The handsontable instance */
  public table:any = {};

  public request:any;
  public children:any;
  public schema:any;
  public tasks:any;
  public signals:any;
  public history:any;
  
  public constructor(private $stateParams: any) {}

  /**
   * Return true if the given category is "invalid", i.e. there are points in
   * the current request that have errors that relate to the category.
   *
   * @param category
   */
  public isInvalidCategory(category) {
    var fieldIds = category.fields.map((field) => field.id);
    var invalid = false;

    this.request.points.forEach((point) => {
      if (point.errors && point.errors.length > 0) {
        point.errors.forEach((error) => {
          if (!error.category) {
            var property = error.property.split('.')[0];

            if (fieldIds.indexOf(property) !== -1) {
              invalid = true;
            }
          }

          else if (error.category === category.name || error.category === category.id) {
            invalid = true;
          }
        });
      }
    });

    return invalid;
  }
}
