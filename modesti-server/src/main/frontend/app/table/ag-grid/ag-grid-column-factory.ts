import {ColumnFactory} from '../column-factory';
import {Table} from '../table';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {ColDef} from 'ag-grid/main';
import {TestFilter} from './test-filter';

export class AgGridColumnFactory {

  public getColumnDefs(table: Table, meta: any): any[] {
    let columnDefs: ColDef[] = [];

    columnDefs.push({
      field: meta.idProperty,
      headerName: '',
      width: 30,
      suppressSorting: true,
      suppressMenu: true,
      suppressResize: true,
      suppressSizeToFit: true,
      pinned: true,
      cellRenderer: meta.selectPointCheckboxCellRenderer
    });

    table.schema.categories.concat(table.schema.datasources).forEach((category: Category) => {
      category.fields.forEach((field: Field) => {
        let columnDef: ColDef = {
          colId: field.id,
          headerName: field.name,
          field: 'properties.' + field.getModelPath()
        };

        // initially show only the first category
        if (table.schema.categories.indexOf(category) !== 0 || field.visibleOnStatus !== undefined) {
          columnDef.hide = true;
        }

        if (field.type === 'checkbox') {
          columnDef.cellRenderer = meta.checkboxCellRenderer;
          columnDef.cellStyle= {'text-align': 'center'}
        } else if (meta.cellRenderer) {
          columnDef.cellRenderer = meta.cellRenderer;
        }

        if (field.id === 'tagname') {
          columnDef.cellClassRules = meta.cellClassRules;
        }

        columnDefs.push(columnDef);
      });
    });

    return columnDefs;
  }
}
