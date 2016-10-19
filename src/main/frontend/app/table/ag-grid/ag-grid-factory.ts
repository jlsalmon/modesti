///<reference path='../table-factory.ts' />

import {TableFactory} from '../table-factory';
import {Table} from '..//table';
import {Schema} from '../../schema/schema';
import {AgGrid} from './ag-grid';

// FIXME: make the inheritance work... currently this throws an error:
// TypeError: Cannot read property 'prototype' of undefined
export class AgGridFactory /*extends TableFactory*/ {

  public createTable(schema: Schema, data: any[], settings: any): Table {
    return new AgGrid(schema, data, state, settings);
  }
}
