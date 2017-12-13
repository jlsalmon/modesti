import {Point} from '../request/point/point';
import IHttpService = angular.IHttpService;
import IQService = angular.IQService;
import IPromise = angular.IPromise;
import IDeferred = angular.IDeferred;
import { AlertService } from '../alert/alert.service';
import {RequestService} from '../request/request.service';
import {Table} from '../table/table';
import {TableFactory} from '../table/table-factory';
import {Schema} from '../schema/schema';
import {QueryParser} from './query-parser';
import {Filter} from '../table/filter';
import {IComponentOptions, IRootScopeService, IAngularEvent} from 'angular';
import { SearchService } from './search.service';

export class TableService {
  public schema: Schema;
  public schemas: Schema[];
  public table: Table;
  public filters: Map<string, Filter>;
  public query: string;
  public page: any = {number: 0, size: 100};
  public sort: string;
  public loading: string;
  public error: string;
  public submitting: string;

  public static $inject: string[] = ['$http', '$q', '$uibModal', 'AlertService', 'SearchService', 'RequestService'];

  constructor(private $http: IHttpService, private $q: IQService, 
    private $modal: any, private alertService: AlertService, private searchService: SearchService,
  private requestService: RequestService) {}

  public sayHello(){
    // alert("Hello in the end");
  }

  public buildTable(schema: Schema, settings: any){
    this.schema = schema;
    this.table = TableFactory.createTable('ag-grid', schema, [], settings);
    return this.table;

  }

  public updatePoints(): void {
    alert("update from update");
    let modalInstance: any = this.$modal.open({
      animation: false,
      templateUrl: '/search/update/update-points.modal.html',
      controller: 'UpdatePointsModalController as ctrl',
      size: 'lg',
      resolve: this.resolvePoints()
    });

    modalInstance.result.then((request: any) => {
      console.log('creating update request');

      this.submitting = 'started';

      // Post form to server to create new request.
      this.requestService.createRequest(request).then((location: string) => {
        // Strip request ID from location.
        let id: string = location.substring(location.lastIndexOf('/') + 1);
        // Redirect to point entry page.
        this.$state.go('request', {id: id}).then(() => {
          this.submitting = 'success';

          this.alertService.add('success', 'Update request #' + id + ' has been created.');
        });
      },

      (error: any) => {
        this.submitting = 'error';
        console.log("Error submiting request: " + error.data.message);
        this.alertService.add('danger', "Error submiting request: " + error.data.message);
      });
    });
  }

  public deletePoints(): void {
    let modalInstance: any = this.$modal.open({
      animation: false,
      templateUrl: '/search/delete/delete-points.modal.html',
      controller: 'DeletePointsModalController as ctrl',
      size: 'lg',
      resolve: this.resolvePoints()
    });

    modalInstance.result.then((request: any) => {
      console.log('creating delete request');

      this.submitting = 'started';

      // Post form to server to create new request.
      this.requestService.createRequest(request).then((location: string) => {
        // Strip request ID from location.
        let id: string = location.substring(location.lastIndexOf('/') + 1);
        // Redirect to point entry page.
        this.$state.go('request', {id: id}).then(() => {
          this.submitting = 'success';

          this.alertService.add('success', 'Delete request #' + id + ' has been created.');
        });
      },

      (error: any) => {
        this.submitting = 'error';
        console.log("Error submiting request: " + error.data.message);
        this.alertService.add('danger', "Error submiting request: " + error.data.message);
      });
    });
  }

  private resolvePoints() {
    return {
      points: (): any => {
        let selectedPoints: number[] = this.table.getSelectedPoints();
        alert(JSON.stringify(selectedPoints));
        // If the user selected some specific points, just use those
        if (selectedPoints.length !== 0) {
          return selectedPoints;
        }

        // Otherwise, update all the points for the current filters
        else {
          let query: string = QueryParser.parse(this.filters);
          let page: any = {number: 0, size: this.page.totalElements};

          return this.searchService.getPoints(this.schema.id, this.schema.primary, query, page, this.sort)
          .then((response: any) => {
            let points: Point[] = [];

            if (response.hasOwnProperty('_embedded')) {
              points = response._embedded.points;
            }

            return points;
          });
        }
      },
      schema: () => this.schema
    }
  }
}