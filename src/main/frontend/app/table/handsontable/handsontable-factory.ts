import {TableFactory} from '../table-factory';
import {Table} from '../../request/table/table';
import {Schema} from '../../schema/schema';
import {HandsonTable} from './handsontable';

// FIXME: make the inheritance work...
export class HandsontableFactory /*extends TableFactory*/ {

  public createTable(schema: Schema, data: any[], settings: any): Table {
    return new HandsonTable(schema, data, settings);
  }
}