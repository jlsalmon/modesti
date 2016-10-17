import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {Point} from '../../request/point/point';
import {Table} from '../table';
import {TableStateService} from '../table-state.service';
import {TableState} from '../table-state';
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
  public static $inject: string[] = ['TableStateService'];

  public schema: Schema;
  public table: Table;
  public state: TableState;

  public constructor(private tableStateService: TableStateService) {
    this.state = this.tableStateService.getTableState(this.schema);
  }

  public toggleExpandCategory(category: Category): void {
    let index: number = this.state.expandedCategories.indexOf(category.id);

    if (index === -1) {
      this.state.expandedCategories.push(category.id);
    } else {
      this.state.expandedCategories.splice(index, 1);
    }

    persistState();
    this.tableStateService.persist(state);
  }

  public isExpandedCategory(category: Category): boolean {
    return this.state.expandedCategories.indexOf(category.id) !== -1;
  }

  public toggleCategory(category: Category): void {
    this.table.toggleColumnGroup(category.fields);
  }

  public toggleColumn(field: Field): void {
    this.table.toggleColumn(field);
    let index: number = this.state.visibleColumns.indexOf(field.id);

    if (index === -1) {
      this.state.visibleColumns.push(field.id);
    } else {
      this.state.visibleColumns.splice(index, 1);
    }
  }

  public toggleFilter(field: Field): void {
    // FIXME: maintain filter state on the column defs, not the schema
    field.filter = {value: undefined, operation: 'equals'};
  }

  public onFiltersChanged(): void {
    // TODO: send an event
  }

  private persistState() {
    $ctrl.table.settings.requestStatus
  }

  // public getColumnDisplayState(): Category {
  //  this.$localStorage.$default({lastActiveCategory: {}});
  //
  //  let categoryId: string = this.$localStorage.lastActiveCategory[this.request.requestId];
  //  let category: Category;
  //
  //  if (!categoryId) {
  //    console.log('using default category');
  //    category = this.schema.categories[0];
  //  } else {
  //    console.log('using last active category: ' + categoryId);
  //
  //    this.schema.categories.concat(this.schema.datasources).forEach((cat: Category) => {
  //      if (cat.id === categoryId) {
  //        category = cat;
  //      }
  //    });
  //
  //    if (!category) {
  //      category = this.schema.categories[0];
  //    }
  //  }
  //
  //  return category;
  // };
  //
  // public setLastActiveCategory(category: Category): void {
  //  this.$localStorage.lastActiveCategory[this.request.requestId] = category.id;
  // }
}
