import {Schema} from '../schema/schema';
import {Category} from '../schema/category/category';
import {Field} from '../schema/field/field';
import {Point} from "../request/point/point";

export abstract class Table {

  public schema: Schema;
  public data: any[];
  public settings: any;

  public constructor(schema: Schema, data: any[], settings: any) {
    this.schema = schema;
    this.data = data;
    this.settings = settings;
  }

  public abstract refreshData(): void;

  public abstract updateSelections(): void;

  public abstract refreshColumnDefs(): void;

  public abstract render(): void;

  public abstract showColumn(field: Field): void;

  public abstract hideColumn(field: Field): void;

  public abstract toggleColumn(field: Field): void;

  public abstract toggleCategory(category: Category): void;

  /**
   * Called to check if a category is selectable (for the column selector)
   * Categories can be hidden in Handsontable if all fields are flagged as 'searchFieldOnly'
   * @param category The category to verify
   * @returns TRUE if and only if the category is selectable to show/hide columns
   */
  public abstract isSelectableCategory(category: Category): boolean;

  public abstract applyDefaultFilter(status: string) : void;

  public abstract isVisibleColumn(field: Field): boolean;

  public abstract toggleColumnGroup(fields: Field[]): void;

  public abstract isVisibleColumnGroup(fields: Field[]): boolean;

  public abstract getActiveDatasources(): Category[];

  public abstract getSelectedPoints(): Point[];

  public abstract clearSelections(): void;

  public abstract showSelectedRowsOnly(value: boolean): void;

  public abstract getSelectedLineNumbers() : number[];
}
