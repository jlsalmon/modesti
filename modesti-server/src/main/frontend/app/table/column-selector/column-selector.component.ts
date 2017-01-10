import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {Table} from '../table';
import {IComponentOptions} from 'angular';

export class ColumnSelectorComponent implements IComponentOptions {
  public templateUrl: string = '/table/column-selector/column-selector.component.html';
  public controller: Function = ColumnSelectorController;
  public bindings: any = {
    schema: '=',
    table: '=',
  };
}

class ColumnSelectorController {
  public static $inject: string[] = [];

  public schema: Schema;
  public table: Table;
  public popoverIsOpen: boolean = false;

  public constructor() {}

  public toggleCategory(category: Category): void {
    this.table.toggleColumnGroup(category.fields);
  }

  public toggleColumn(field: Field): void {
    this.table.toggleColumn(field);
  }
}
