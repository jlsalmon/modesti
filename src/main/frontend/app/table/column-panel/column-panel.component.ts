import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {Point} from '../../request/point/point';
import {IComponentOptions} from 'angular';

export class ColumnPanelComponent implements IComponentOptions {
  public templateUrl: string = '/table/column-panel/column-panel.component.html';
  public controller: Function = ColumnPanelController;
  public bindings: any = {
    schema: '=',
    table: '=',
    enableFilters: '='
  };
}

class ColumnPanelController {
  public static $inject: string[] = [];
}
