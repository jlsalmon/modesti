import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {Point} from '../../request/point/point';
import {Table} from '../table';
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

  public schema: Schema;
  public table: Table;

  public constructor() {
    this.schema.categories.forEach(function (category: Category, index: number) {
      category.isCollapsed = index === 0 ? false : true;
    });
  }

  public toggleExpandCategory(category: Category): void {
    category.isCollapsed = !category.isCollapsed;
  }

  public isExpandedCategory(category: Category): boolean {
    return category.isCollapsed === false;
  }

  public toggleCategory(category: Category): void {
    this.table.toggleColumnGroup(category.fields);
  }

  public toggleColumn(field: Field): void {
    this.table.toggleColumn(field);
  }

  public toggleFilter(field: Field): void {
    // FIXME: maintain filter state on the column defs, not the schema
    field.filter = {value: undefined, operation: 'equals'};
  }

  public onFiltersChanged(): void {
    // TODO: send an event
  }
}
