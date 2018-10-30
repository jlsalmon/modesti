import {Schema} from '../../schema/schema';
import {Field} from '../../schema/field/field';
import {SchemaService} from '../../schema/schema.service';
import {Table} from '../table';
import {Filter} from '../filter';
import {IComponentOptions, IRootScopeService, ITimeoutService, IPromise} from 'angular';
import { CacheService } from '../../cache/cache.service';
import IScope = angular.IScope;

export class FilterBuilderComponent implements IComponentOptions {
  public templateUrl: string = '/table/filter-builder/filter-builder.component.html';
  public controller: Function = FilterBuilderController;
  public bindings: any = {
    schema: '=',
    table: '=',
  };
}

class FilterBuilderController {
  public static $inject: string[] = ['$rootScope', '$timeout', 'SchemaService', 'CacheService', '$scope'];

  public schema: Schema;
  public table: Table;
  public filters: Map<string, Filter> = new Map<string, Filter>();
  public popoverIsOpen: boolean = false;
  public showFilters: boolean = true;

  public constructor(private $rootScope: IRootScopeService, private $timeout: ITimeoutService,
    private schemaService: SchemaService, private cacheService: CacheService, private $scope: IScope) {

    $scope.$watch('$ctrl.schema', (previousSchema) => {
      this.loadCachedValues(previousSchema.id);
    });

    $rootScope.$on('modesti:searchDomainChanged', () => {
      this.saveValuesToCache();
    });

    $rootScope.$on('modesti:enableSearchFilters', (event, data) => {
      this.enableFilters(data);
    });
  }

  public addFilter(field: Field): void {
    let filter: Filter = { field: field, operation: undefined, value: undefined, isOpen: false };

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

    this.popoverIsOpen = false;
    this.filters['_' + field.id] = filter;

    this.$timeout(() => filter.isOpen = true);
  }

  public removeFilter(field: Field): void {
    this.filters['_' + field.id] = undefined;
    this.onFiltersChanged();
  }

  public onFiltersChanged(): void {
    this.$rootScope.$emit('modesti:searchFiltersChanged', this.filters);
    this.saveValuesToCache();
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

  private loadCachedValues(id: string): void {
    this.filters = this.cacheService.filtersCache.get(id) || new Map<string, Filter>();
    this.onFiltersChanged();
  }

  private saveValuesToCache(): void {
    this.cacheService.filtersCache.put(this.schema.id, this.filters);
  }

  private enableFilters(show: boolean) : void {
    this.showFilters = show;
  }
}
