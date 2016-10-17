import {Schema} from '../schema/schema';
import {Table} from './table';
import {TableState} from './table-state';
import {AgGridFactory} from './ag-grid/ag-grid-factory';
import {HandsontableFactory} from './handsontable/handsontable-factory';

export abstract class TableFactory {

  public static createTable(type: string, schema: Schema, data: any[], state: TableState, settings: any): Table {
    if (type === 'handsontable') {
      return new HandsontableFactory().createTable(schema, data, state, settings);
    } else if (type === 'ag-grid') {
      return new AgGridFactory().createTable(schema, data, state, settings);
    }

    throw new Error('Unsupported table type' + type);
  }

  public abstract createTable(schema: Schema, data: any[], state: TableState, settings: any): Table;
}
