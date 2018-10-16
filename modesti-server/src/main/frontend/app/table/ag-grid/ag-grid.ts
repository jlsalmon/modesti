import {Table} from "../table";
import {Point} from "../../request/point/point";
import {Schema} from "../../schema/schema";
import {Category} from "../../schema/category/category";
import {Field} from "../../schema/field/field";
import {SelectedPointsService} from '../../search/selected-points.service';
import {ColumnFactory} from "../column-factory";
import * as agGrid from "ag-grid/main";
import {Grid, GridOptions, Column, ColDef} from "ag-grid/main";
import "lodash";
agGrid.initialiseAgGridWithAngular1(angular);


export class AgGrid extends Table {
  public static $inject: string[] = ['Schema', 'SelectedPointsService']; 
  public grid: Grid;
  public gridOptions: GridOptions;
  public idProperty: string;
  private showSelectedRows: boolean = false;

  public constructor(schema: Schema, data: any[], settings: any, private selectedPointsService: SelectedPointsService) {
    super(schema, data, settings);
    this.idProperty = this.schema.getIdProperty();
    this.clearSelections();
    let columnDefs: ColDef[] = this.getColumnDefs();    
    
    this.gridOptions = {
      angularCompileRows: true,
      enableColResize: true,
      enableServerSideSorting: true,
      enableServerSideFilter: true,
      rowSelection: 'multiple',
      suppressRowClickSelection: true,
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
      maxPagesInCache: 2,
      onRowSelected: this.rowSelected.bind(this),
      onModelUpdated: this.updateSelections.bind(this),

      datasource: {
        rowCount: undefined, // behave as infinite scroll
        getRows: (params: any) => this.settings.getRows(params)
      },
      onGridReady: (event: any) => event.api.sizeColumnsToFit()
    };

    window.onresize = (event: Event) => {
      this.gridOptions.api.sizeColumnsToFit();
    };
  }

  public render(): void {}

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

  public toggleCategory(category: Category): void {
    this.toggleColumnGroup(category.fields);
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

    this.gridOptions.api.sizeColumnsToFit();
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

  public showSelectedRowsOnly(show: boolean) : void {
    this.showSelectedRows = show;
    this.gridOptions.api.purgeVirtualPageCache();
    let sortModel : any = this.gridOptions.api.getSortModel();
    this.gridOptions.api.setSortModel(sortModel);   
  }

  private getColumn(field: Field): Column {
    return this.gridOptions.columnApi.getColumn(field.id);
  }

  private getColumnDefs(): ColDef[] {
    let meta: any = {
      idProperty: this.idProperty,
      cellRenderer: (params: any) => {
        if (params.data != null) {
          return this.gridOptions.api.getValue(params.column, params.node);
        } else {
          return '...';
        }
      },
      checkboxCellRenderer: (params: any) => {
        return '<input type="checkbox" ' +
          (params.node.selected ? 'checked="checked" ' : '') +
          'ng-click="$ctrl.table.selectNodeById(' + params.node.id + ')" style="margin-left: 5px;">';
      }
     };

    return ColumnFactory.getColumnDefinitions('ag-grid', this, meta);
  }

  public selectNodeById(id: number): void {
    if (_.find(this.gridOptions.api.getSelectedNodes(), {id: id.toString()})) {
      this.gridOptions.api.deselectIndex(id);
    } else {
      this.gridOptions.api.selectIndex(id, true, false);
    }
  };

  public getSelectedPoints() {
    return this.selectedPointsService.getSelectedPoints();
  }

  public rowSelected(event) {
    let point: Point = event.node.data;
    let selected: boolean = event.node.isSelected();
    let path: string = this.idProperty;

    if (selected) {
      this.selectedPointsService.addPoint(point, path);
    } else {
      this.selectedPointsService.deletePoint(point, this.idProperty);
    }
  }

  public updateSelections() {
    let path: string = this.idProperty;
    let selectedInGrid = this.gridOptions.api.getSelectedNodes();
    let gridPath = 'data.' + path;

    _.each(selectedInGrid, (node) => {
      if (!_.some(this.selectedPointsService.getSelectedPoints(), [path, _.get(node, gridPath)])) {
        // The following suppressEvents=true flag is ignored for now, but a
        // fixing pull request is waiting at ag-grid GitHub.
        node.setSelected(false);
        // this.gridOptions.api.deselectNode(node, true);
      }
    });

    let selectedIdsInGrid = _.map(selectedInGrid, gridPath);
    let currentlySelectedIds = _.map(this.selectedPointsService.getSelectedPoints(), path);
    let missingIdsInGrid = _.difference(currentlySelectedIds, selectedIdsInGrid);

    if (missingIdsInGrid.length > 0) {
      // We're trying to avoid the following loop, since it seems horrible to
      // have to loop through all the nodes only to select some.  I wish there
      // was a way to select nodes/rows based on an id.
      var i;
      let rowsToRefresh : agGrid.RowNode [] = [];
      this.gridOptions.api.forEachNode((node) => {
        i = _.indexOf(missingIdsInGrid, _.get(node, gridPath));
        if (i >= 0) {
          node.setSelected(true);
          rowsToRefresh.push(node);
          missingIdsInGrid.splice(i, 1);  // Reduce haystack.
          if (!missingIdsInGrid.length) {
            // I'd love for `forEachNode` to support breaking the loop here.
          }
        }
      });
      this.gridOptions.api.refreshRows(rowsToRefresh);
    }
  }

  public clearSelections(): void {
    if(this.gridOptions) {
      let selectedInGrid = this.gridOptions.api.getSelectedNodes();
      this.gridOptions.api.deselectAll();       
      this.selectedPointsService.clear();
      if (this.showSelectedRows) {
        this.showSelectedRowsOnly(false);
      } else {
        this.gridOptions.api.refreshRows(selectedInGrid);
      }
    }
  }

  public selectAll() : void {
    this.gridOptions.api.forEachNode((node: agGrid.RowNode) => {
      node.setSelected(true);
    });
    this.gridOptions.api.refreshView();
  }

  public getActiveDatasources(): Category[] {
    return this.schema.datasources;
  }
}
