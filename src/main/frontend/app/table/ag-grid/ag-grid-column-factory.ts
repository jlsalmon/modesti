import {ColumnFactory} from '../column-factory';
import {Table} from '../table';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {ColDef} from 'ag-grid/main';

export class AgGridColumnFactory {

  protected getColumnDefs(table: Table, meta: any): any[] {
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
      cellRenderer: meta.checkboxCellRenderer
    });

    table.schema.categories.concat(table.schema.datasources).forEach((category: Category) => {
      category.fields.forEach((field: Field) => {
        let columnDef: ColDef = {
          colId: field.id,
          headerName: field.name,
          field: 'properties.' + field.getModelPath()
        };

        //let visibleColumns: string[] = meta.state.visibleColumns;
        //
        //if (visibleColumns.length > 0) {
        //  // If we have list of visible columns, use that
        //  if (visibleColumns.indexOf(field.id) === -1) {
        //    columnDef.hide = true;
        //  }
        //
        //} else {
          // Otherwise, initially show only the first category
          if (table.schema.categories.indexOf(category) !== 0) {
            columnDef.hide = true;
          }
        //}

        if (meta.cellRenderer) {
          columnDef.cellRenderer = meta.cellRenderer;
        }

        columnDefs.push(columnDef);
      });
    });

    return columnDefs;
  }
}
