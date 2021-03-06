import {Table} from './table';
import {Schema} from '../schema/schema';
import {Field} from '../schema/field/field';
import {AgGridColumnFactory} from './ag-grid/ag-grid-column-factory';
import {HandsontableColumnFactory} from './handsontable/handsontable-column-factory';

export abstract class ColumnFactory {

  public static getColumnDefinitions(type: string, table: Table, meta: any): any[] {
    if (type === 'handsontable') {
      return new HandsontableColumnFactory().getColumnDefs(table, meta);
    } else if (type === 'ag-grid') {
      return new AgGridColumnFactory().getColumnDefs(table, meta);
    }

    throw new Error('Unsupported table type' + type);
  }

  protected abstract getColumnDefs(table: Table, meta: any): any[];
}
