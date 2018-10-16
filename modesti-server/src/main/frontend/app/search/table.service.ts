import {Point} from '../request/point/point';
import IHttpService = angular.IHttpService;
import IQService = angular.IQService;
import IPromise = angular.IPromise;
import IDeferred = angular.IDeferred;
import {AlertService} from '../alert/alert.service';
import {RequestService} from '../request/request.service';
import {Table} from '../table/table';
import {TableFactory} from '../table/table-factory';
import {Schema} from '../schema/schema';
import {QueryParser} from './query-parser';
import {Filter} from '../table/filter';
import {IComponentOptions, IRootScopeService, IAngularEvent} from 'angular';
import {SearchService} from './search.service';
import {SelectedPointsService} from '../search/selected-points.service';
import {IStateService} from 'angular-ui-router';

export class TableService {
  public table: Table;
  public filters: Map<string, Filter>;
  public query: string;
  public page: any = {number: 0, size: 100};
  public sort: string;
  public loading: string;
  public error: string;
  public submitting: string; 
  public updateHeader: string;
  public updateMessage: string;
  public static $inject: string[] = ['$http', '$rootScope', '$q', '$state', '$uibModal', 'AlertService', 'SearchService', 'RequestService', 'SelectedPointsService'];

  constructor(private $http: IHttpService, private $rootScope: IRootScopeService, private $q: IQService, private $state: IStateService,
    private $modal: any, private alertService: AlertService, private searchService: SearchService,
    private requestService: RequestService, private selectedPointsService: SelectedPointsService) {}

  public buildTable(schema: Schema, settings: any){
    this.table = TableFactory.createTable('ag-grid', schema, [], settings, this.selectedPointsService);
    return this.table;
  }

  public getDefaultUpdateHeader() {
    return 'Modify points';
  }

  public getDefaultUpdateMessage() {
    return 'You are about to create a new MODESTI request to update <b>' + this.table.getSelectedPoints().length + '</b> points.';
  }

  public updatePoints(header: string = '', message: string = ''): void {
    if (typeof(header) == undefined || header == '') {
      this.updateHeader = this.getDefaultUpdateHeader();
    } else {
      this.updateHeader = header
    }
    if (typeof(message) == undefined || message == '') {
      this.updateMessage = this.getDefaultUpdateMessage();
    } else {
      this.updateMessage = message
    }

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

  public clearSelections() : void {
    if (this.table) {
      this.table.clearSelections();
    }
  }

  private resolvePoints() {
    return {
      /*
      points: (): any => {
        let selectedPoints: Point[] = this.table.getSelectedPoints();
        // If the user selected some specific points, just use those
        if (selectedPoints.length !== 0) {
          return selectedPoints;
        }

        // Otherwise, update all the points for the current filters
        else {
          let query: string = QueryParser.parse(this.filters);
          let page: any = {number: 0, size: this.page.totalElements};

          return this.searchService.getPoints(this.table.schema.id, this.table.schema.primary, query, page, this.sort)
          .then((response: any) => {
            let points: Point[] = [];

            if (response.hasOwnProperty('_embedded')) {
              points = response._embedded.points;
            }

            return points;
          });
        }
      },
      */
      points: () => this.table.getSelectedPoints(),
      schema: () => this.table.schema,
      message: () => this.updateMessage,
      header: () => this.updateHeader
    }
  }
}
