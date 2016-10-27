import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {SchemaService} from '../../schema/schema.service';
import {Table} from '../table';
import {Filter} from './filter';
import {IComponentOptions, IRootScopeService, IPromise} from 'angular';

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
  public static $inject: string[] = ['$rootScope', 'SchemaService'];

  public schema: Schema;
  public table: Table;
  public filters: Map<string, Filter> = new Map<string, Filter>();

  public constructor(private $rootScope: IRootScopeService, private schemaService: SchemaService) {
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
    let filter: Filter = this.filters[field.id];

    if (filter) {
      this.filters['_' + field.id] = undefined;
      this.onFiltersChanged();
    } else {
      filter = {field: field, operation: undefined, value: undefined};

      switch (field.type) {
        case 'text':
          filter.operation = 'starts-with';
          break;
        case 'numeric':
        case 'options':
        case 'autocomplete':
          filter.operation = 'equals';
          break;
      }

      this.filters['_' + field.id] = filter;
    }
  }

  public onFiltersChanged(): void {
    this.$rootScope.$emit('modesti:searchFiltersChanged', this.filters);
  }

  public getOptionValue(option: any): string {
    return typeof option === 'object' ? option.value : option;
  }

  public getOptionDisplayValue(option: any): string {
    return typeof option === 'object' ? option.value + (option.description ? ': ' + option.description : '') : option;
  }

  public queryFieldValues(field: any, value: string): IPromise<any[]> {
    return this.schemaService.queryFieldValues(field, value, undefined);
  }
}
