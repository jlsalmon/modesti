import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {Table} from '../table';
import {Request} from '../../request/request';
import {StatusFilter} from '../../schema/filter/status-filter';
import {IComponentOptions} from 'angular';

export class ColumnSelectorComponent implements IComponentOptions {
  public templateUrl: string = '/table/column-selector/column-selector.component.html';
  public controller: Function = ColumnSelectorController;
  public bindings: any = {
    schema: '=',
    table: '=',
    request: '=',
  };
}

class ColumnSelectorController {
  public static $inject: string[] = [];

  public schema: Schema;
  public table: Table;
  public request: Request;
  public popoverIsOpen: boolean = false;

  public constructor() {}

  public toggleCategory(category: Category): void {
    this.table.toggleCategory(category);
  }

  public toggleColumn(field: Field): void {
    this.table.toggleColumn(field);
  }

  public hasDefaultFilter() : boolean {
    return this.request!==undefined && this.schema.getStatusFilter(this.request.status) !== undefined;
  }

  public applyDefaultFilter() : void {
    this.table.applyDefaultFilter(this.request.status);
  }
}
