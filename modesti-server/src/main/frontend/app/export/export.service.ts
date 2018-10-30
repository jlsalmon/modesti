import {Category} from "../schema/category/category";
import {Field} from "../schema/field/field";
import {Schema} from "../schema/schema";
import {Point} from "../request/point/point";
import {Table} from "../table/table";
import {IPromise} from 'angular';

export class ExportService {

  public static $inject: string[] = ['$uibModal'];

  constructor(private $modal: any) {}

  public showModal(numPoints: number) : IPromise<any> {
    let modalInstance : any = this.$modal.open({
      animation: false,
      templateUrl: '/export/export-request.modal.html',
      controller: 'ExportRequestModalController as ctrl',
      resolve: {
        points: () => numPoints
      }
    });

    return modalInstance.result;
  }

  public exportPoints(table: Table, schema: Schema, points: Point[], exportVisibleColumnsOnly: boolean) : void {
    let columnIds : string [] = [];
    let columnNames: string [] = [];
    let buffer : string = "";
    let categories : Category[] = schema.categories.concat(table.getActiveDatasources());

    categories.forEach((category: Category) => {
        category.fields.forEach((field: Field) => {
        let exportField : boolean = !field.searchFieldOnly && (!exportVisibleColumnsOnly || 
            (exportVisibleColumnsOnly && table.isVisibleColumn(field)));
        if (exportField) {
            columnIds.push(field.id);
            columnNames.push(field.name);
        }
        });
    });
    buffer = columnNames.join(',') + '\r\n';

    points.forEach((point: Point) => {
        columnIds.forEach((colId : string) =>  {
        buffer += '"' + point.getPropertyAsString(colId) + '"';
        if (colId != columnIds[columnIds.length-1]) {
            buffer += ',';
        } else {
            buffer += '\r\n';
        }
        });
    });

    let csvData = new Blob([buffer], {type: 'text/plain;charset=utf-8;'});
    let csvUrl = window.URL.createObjectURL(csvData);
    let link = document.createElement('a');
    link.href = csvUrl;    
    link.setAttribute('download', 'export.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}