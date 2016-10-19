import {Schema} from '../schema/schema';
import {Field} from '../schema/field/field';
import {TableState} from './table-state';

export abstract class Table {

  public schema: Schema;
  public data: any[];
  public state: TableState;
  public settings: any;

  public constructor(schema: Schema, data: any[], state: TableState, settings: any) {
    this.schema = schema;
    this.data = data;
    this.state = state;
    this.settings = settings;
  }

  public abstract refreshData(): void;

  public abstract refreshColumnDefs(): void;

  public abstract render(): void;

  public abstract showColumn(field: Field): void;

  public abstract hideColumn(field: Field): void;

  public abstract toggleColumn(field: Field): void;

  public abstract isVisibleColumn(field: Field): boolean;

  public abstract toggleColumnGroup(fields: Field[]): void;

  public abstract isVisibleColumnGroup(fields: Field[]): boolean;

  public getModel(field: Field): string {
    let model: string = 'properties.' + field.id;
    if (field.type === 'autocomplete') {
      if (field.model) {
        model += '.' + field.model;
      } else {
        model += '.value';
      }
    }
    return model;
  }
}
