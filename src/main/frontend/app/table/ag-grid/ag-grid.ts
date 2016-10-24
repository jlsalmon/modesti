import {Table} from '../table';
import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {ColumnFactory} from '../column-factory';
import {Grid, GridOptions, Column, ColDef} from 'ag-grid/main';

export class AgGrid extends Table {

  public grid: Grid;
  public gridOptions: GridOptions;

  public constructor(schema: Schema, data: any[], settings: any) {
    super(schema, data, settings);
    let columnDefs: ColDef[] = this.getColumnDefs();

    this.gridOptions = {
      enableColResize: true,
      enableServerSideSorting: true,
      enableServerSideFilter: true,
      debug: true,
      rowHeight: 24,
      rowDeselection: true,
      columnDefs: columnDefs,
      suppressMovableColumns: true,
      rowModelType: 'virtual',
      paginationPageSize: 100,
      paginationOverflowSize: 2,
      maxConcurrentDatasourceRequests: 2,
      paginationInitialRowCount: 50,
      maxPagesInCache: 10,
      datasource: {
        rowCount: undefined, // behave as infinite scroll
        getRows: (params: any) => this.settings.getRows(params)
      },
      onGridReady: (event: any) => event.api.sizeColumnsToFit()
    };

    this.grid = new Grid(document.getElementById('table'), this.gridOptions);

    window.onresize = (event: Event) => {
      this.gridOptions.api.sizeColumnsToFit();
    };
  }

  public refreshData(): void {
    if (this.gridOptions) {
      this.gridOptions.api.purgeVirtualPageCache();
      this.gridOptions.api.setDatasource(this.gridOptions.datasource);
    }
  }

  public refreshColumnDefs(): void {
    if (this.gridOptions) {
      this.gridOptions.api.setColumnDefs(this.getColumnDefs());
      this.gridOptions.api.sizeColumnsToFit();
    }
  }

  public showColumn(field: Field): void {
    let column: Column = this.getColumn(field);
    this.gridOptions.columnApi.setColumnVisible(column, true);
    this.gridOptions.api.sizeColumnsToFit();
  }

  public hideColumn(field: Field): void {
    let column: Column = this.getColumn(field);
    this.gridOptions.columnApi.setColumnVisible(column, false);
    this.gridOptions.api.sizeColumnsToFit();
  }

  public toggleColumn(field: Field): void {
    let column: Column = this.getColumn(field);
    if (column.isVisible()) {
      this.gridOptions.columnApi.setColumnVisible(column, false);
    } else {
      this.gridOptions.columnApi.setColumnVisible(column, true);
    }
    this.gridOptions.api.sizeColumnsToFit();
  }

  public isVisibleColumn(field: Field): boolean {
    let column: Column = this.getColumn(field);
    return column.isVisible();
  }

  public toggleColumnGroup(fields: Field[]): void {
    let fieldIds: string[] = fields.map((field: Field) => field.id);

    if (this.isVisibleColumnGroup(fields)) {
      this.gridOptions.columnApi.setColumnsVisible(fieldIds, false);
    } else {
      this.gridOptions.columnApi.setColumnsVisible(fieldIds, true);
    }
  }

  public isVisibleColumnGroup(fields: Field[]): boolean {
    let visible: boolean = true;

    fields.forEach((field: Field) => {
      let column: Column = this.getColumn(field);
      if (!column.isVisible()) {
        visible = false;
        return;
      }
    });

    return visible;
  }

  private getColumn(field: Field): Column {
    return this.gridOptions.columnApi.getColumn(field.id);
  }

  private getColumnDefs(): ColDef[] {
    let meta: any = {
      cellRenderer: (params: any) => {
        if (params.data != null) {
          return this.gridOptions.api.getValue(params.column, params.node);
        } else {
          return '...';
        }
      },
      state: this.state
    };

    return ColumnFactory.getColumnDefinitions('ag-grid', this, meta);
  }
}
