import {ColumnFactory} from '../column-factory';
import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {Grid, GridOptions, Column, ColDef} from 'ag-grid/main';

export class AgGridColumnFactory {

  protected getColumnDefs(schema: Schema, meta: any): any[] {
    let columnDefs: ColDef[] = [];

    schema.categories.concat(schema.datasources).forEach((category: Category) => {
      category.fields.forEach((field: Field) => {
        let columnDef: ColDef = {
          headerName: field.name,
          field: ColumnFactory.getModel(field)
        };

        // Initially show all fields from the first category
        if (schema.categories.indexOf(category) !== 0) {
          columnDef.hide = true;
        }

        if (meta.cellRenderer) {
          columnDef.cellRenderer = meta.cellRenderer;
        }

        columnDefs.push(columnDef);
      });
    });

    return columnDefs;
  }
}