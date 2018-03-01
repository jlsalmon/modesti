import {AuthService} from '../../auth/auth.service';
import {SchemaService} from '../../schema/schema.service';
import {Request} from '../../request/request';
import {Point} from '../../request/point/point';
import {Schema} from '../../schema/schema';
import {Field} from '../../schema/field/field';
import IPromise = angular.IPromise;
import {IPromise, IRootScopeService} from 'angular';

export class UpdatePointsModalController {
  public static $inject: string[] = ['$uibModalInstance', '$rootScope', 'points', 'schema', 'message', 'header', 'AuthService', 'SchemaService'];

  public request: Request;
  public fieldValues: any[] = [];

  constructor(private $modalInstance: any, private $rootScope: IRootScopeService, private points: Point[], private schema: Schema, private message: string,
  			  private header: string, private authService: AuthService, private schemaService: SchemaService) {
    this.request = new Request();
    this.request.type = 'UPDATE';
    this.request.description = '';
    this.request.creator = authService.getCurrentUser().username;
    this.request.assignee = this.request.creator;
    this.request.domain = schema.id;
    this.request.points = points;
    this.updateMessage = message;
    this.updateHeader = header;
  }

  public removeDuplicates(arr: any[], model: string): string[] {
	if(!model){	model = 'value'; }
	let duplicatesArr = arr.map(function(item){ return item[model] });
	let duplicatesFreeArr: string[] = [];
	duplicatesArr.some(function(item, idx){
	  if (duplicatesArr.indexOf(item) == idx ){
		  duplicatesFreeArr.push(arr[idx]);
	  }
	});
	return duplicatesFreeArr;
  }

  public ok(): void {
    this.$modalInstance.close(this.request);
  }

  public cancel(): void {
    this.$modalInstance.dismiss('cancel');
  }

  public queryFieldValues(field: Field, query: string): IPromise<void> {
    return this.schemaService.queryFieldValues(field, query, undefined).then((values: any[]) => {
      this.fieldValues = values;
	});
  }
}