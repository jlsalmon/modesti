import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {Table} from '../table';
import {Filter} from './filter';
import {IComponentOptions, IRootScopeService} from 'angular';

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
  public static $inject: string[] = ['$rootScope'];

  public schema: Schema;
  public table: Table;
  public filters: Map<string, Filter> = new Map<string, Filter>();

  public constructor(private $rootScope: IRootScopeService) {
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
    this.filters[field.id] = {field: field, value: undefined, operation: 'starts-with'};
  }
  public onFiltersChanged(): void {
    this.$rootScope.$emit('modesti:searchFiltersChanged', this.filters);
  }
}
