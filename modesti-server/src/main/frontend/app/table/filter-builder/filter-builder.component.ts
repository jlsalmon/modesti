import {Schema} from '../../schema/schema';
import {Field} from '../../schema/field/field';
import {SchemaService} from '../../schema/schema.service';
import {Table} from '../table';
import {Filter} from '../filter';
import {IComponentOptions, IRootScopeService, ITimeoutService, IPromise} from 'angular';
import { CacheService } from '../../cache/cache.service';

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
  private $scope: any;
  
  public constructor(private $rootScope: IRootScopeService, private $timeout: ITimeoutService,
    private schemaService: SchemaService, private cacheService: CacheService, $scope: any) {

      $scope.$watch("$ctrl.schema", (value) => {
        let id = value.id;
        if(typeof this.cacheService.filtersCache.get(id) === "undefined"){
          alert(id + " doesnt exist");
          this.filters = new Map<string, Filter>();
          return;
        }
        alert(id + " exists   " + JSON.stringify(this.cacheService.filtersCache.get(id)));
        console.clear();
        this.filters = this.cacheService.filtersCache.get(id);
        this.onFiltersChanged();
      // let x = this.cacheService.bookCache.get('cachedFilters')[id];
      //           alert("load" + JSON.stringify(x));

    });

    //   $rootScope.$watch('schema', function(newValue, oldValue) {
    //     if(newValue!==oldValue) {
    //       alert("obserwator " + oldValue + " - > " + newValue);
    //     } 
    //  });
    //  alert("ab");

    $rootScope.$on('modesti:searchDomainChanged', () => {

      // let x = {
      //   "_tagname": {
      //     "field":
      //     { "id": "tagname", "type": "text", "name": "Tagname", "help": "Automatically generated TIM tagname. Interrogation marks (?) indicate that a column which makes up the tagname has not yet been filled.", "unique": true, "editable": false, "fixed": true },
      //     "operation": "starts-with", "value": "FW", "isOpen": false
      //   }
      // }
      // alert("leaving " + this.schema.id);
      this.cacheFilters();
    });




  }

  // public auToaddFilter(x: any): void {
  //   // if(typeof this.cacheService.bookCache.get('cachedFilters') === 'undefined' || typeof this.cacheService.bookCache.get('cachedFilters').TIM === 'undefined' ){
  //   //   alert('not found');
  //   //   return;
  //   // }
  //   //     let x = this.cacheService.bookCache.get('cachedFilters').TIM;

  //   let filter: Filter = { 'field': Object.assign(new Field(), x._tagname.field), operation: x._tagname.operation, value: x._tagname.value, isOpen: false };
  //   this.filters['_' + x._tagname.field.id] = filter;
  //   this.onFiltersChanged();
  // }

  // private loadLastFiltersConfig(): void{
  //   let cacheFilter = {
  //     '_tagname': {
  //       'field': {
  //           'id': 'tagname',
  //           'type': 'text',
  //           'name': 'Tagname',
  //           'help': 'Automatically generated TIM tagname. Interrogation marks (?) indicate that a column which makes up the tagname has not yet been filled.',
  //           'unique': true,
  //           'editable': false,
  //           'fixed': true,
  //           '$$hashKey': 'object:225'
  //        },
  //        'operation': 'starts-with',
  //        'value': 'a',
  //        'isOpen': false
  //     },
  //     "_pointDatatype":{"field":{"id":"pointDatatype","type":"options","name":"Data Type","help":"Select the data type of the point from the list","required":true,"options":["Boolean","Double","Float","Integer","Long","String"]},"operation":"equals","value":"Double","isOpen":false}
  //  };
  // //  this.filters = cacheFilter;
  // // this.filters['_tagname'] = cacheFilter._tagname;
  // this.filters['_pointDatatype'] = cacheFilter._pointDatatype;
  // // setTimeout(()=>{
  // //   this.$rootScope.$emit('modesti:searchFiltersChanged', this.filters);

  // // }, 10000);

  // }



  public addFilter(field: Field): void {
    console.clear();
    console.log(field);
    console.log(JSON.stringify(field));
    console.log('---------------------------------------------------------------------------');
    console.log(JSON.stringify(this.cacheService.filtersCache));

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
    this.cacheFilters();
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

  private cacheFilters(): void {
    let key = this.schema.id;
    this.cacheService.filtersCache.put(key, this.filters);
  }
}
