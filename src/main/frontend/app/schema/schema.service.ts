import {Request} from '../request/request';
import {Point} from '../request/point/point';
import {Schema} from './schema';
import {Field} from './field/field';
import {AutocompleteField} from './field/autocomplete-field';
import IQService = angular.IQService;
import IHttpService = angular.IHttpService;
import IPromise = angular.IPromise;
import IDeferred = angular.IDeferred;

export class SchemaService {
  public static $inject: string[] = ['$q', '$http'];

  public constructor(private $q: IQService, private $http: IHttpService) {}

  public getSchema(request: Request): IPromise<Schema> {
    console.log('fetching schema');
    let q: IDeferred<Schema> = this.$q.defer();

    let url: string = request._links.schema.href;

    this.$http.get(url).then((response: any) => {
      let schema: Schema = new Schema().deserialize(response.data);
      console.log('fetched schema: ' + schema.id);
      q.resolve(schema);
    },

    (error: any) => {
      console.log('error fetching schema: ' + error.statusText);
      q.reject();
    });

    return q.promise;
  }

  public getSchemas(): IPromise<Schema[]> {
    console.log('fetching schemas');
    let q: IDeferred<Schema[]> = this.$q.defer();

    this.$http.get('/api/schemas').then((response: any) => {
      let schemas: Schema[] = [];

      response.data._embedded.schemas.forEach((schema: Schema) => {
        schemas.push(new Schema().deserialize(schema));
      });

      console.log('fetched ' + schemas.length + ' schemas');
      q.resolve(schemas);
    },

    (error: any) => {
      console.log('error fetching schemas: ' + error.statusText);
      q.reject();
    });

    return q.promise;
  }

  public queryFieldValues(field: Field, query: string, point: Point): IPromise<any[]> {
    console.log('querying values for field ' + field.id + ' with query string "' + query + '"');
    let q: IDeferred<any[]> = this.$q.defer();

    // Don't make a call if the query is less than the minimum length (or is undefined)
    if (!query || (field.minLength && query.length < field.minLength)) {
      q.resolve([]);
      return q.promise;
    }

    // Figure out the query parameters we need to put in the URI.
    let params: any = {};

    if (field.params == null) {
      // If no params are specified, then by default the query string will be
      // mapped to a parameter called 'query'.
      params.query = query;
    } else if (field.params instanceof Array) {

      // Otherwise, an array of params will have been given.
      if (field.params.length === 1) {
        let param: string = field.params[0];

        if (point && point.properties[param]) {
          params[param] = point.properties[param];
        } else {
          params[param] = query;
        }
      } else if (field.params.length > 1) {

        // If we've been given a point object, then we can query stuff based on other properties
        // of that point. So we take all parameters into account.
        if (point) {
          field.params.forEach((param: string) => {

            // Conventionally the first parameter will be called 'query'.
            if (param === 'query') {
              params.query = query;
            }

            // If the specified parameter name matches a property of the point, then we will use the
            // value of that property as the corresponding parameter value. Otherwise, it will be
            // mapped to the query string.
            if (point.properties[param]) {
              params[param] = point.properties[param];
            } else {
              params[param] = query;
            }

            // The parameter might be a sub-property of another property (i.e. contains a dot). In
            // that case, find the property of the point and add it as a search parameter. This
            // acts like a filter for a search, based on another property.
            // TODO: add "filter" parameter to schema instead of this?
            if (param.indexOf('.') > -1) {
              let parts: string[] = param.split('.');
              let prop: string = parts[0];
              let subProp: string = parts[1];

              if (point.properties[prop] && point.properties[prop].hasOwnProperty(subProp)
                  && point.properties[prop][subProp]) {
                params[subProp] = point.properties[prop][subProp];
              } else {
                params[subProp] = '';
              }
            }
          });
        } else {
          // If we haven't been given a point, then we can't do point-contextual queries. In that
          // case, we ignore all params except the first one and bind the query string to it.
          params[field.params[0]] = query;
          params[field.params[1]] = '';
        }
      }
    }

    // Call the endpoint asynchronously and resolve the promise when we're done.
    this.$http.get('/api/' + field.url, {
      params: params,
      cache: true
    }).then((response: any) => {
      let values: any[] = [];

      if (response.data.hasOwnProperty('_embedded')) {
        // Relies on the fact that the property name inside the JSON response is the same
        // as the first part of the URL, before the first forward slash
        let returnPropertyName: string = field.url.split('/')[0];
        values = response.data._embedded[returnPropertyName];
      } else if (response.data instanceof Array) {
        values = response.data;
      }

      console.log('found ' + values.length + ' values');
      q.resolve(values);
    },

    (error: any) => {
      console.log('error querying values: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public evaluateConditional(point: Point, conditional: any, status: string): boolean {
    // Simple boolean
    if (conditional === false || conditional === true) {
      return conditional;
    }

    let results: boolean[] = [];

    if (conditional.or) {
      // Chained OR condition
      conditional.or.forEach((subConditional: any) => {
        results.push(this.evaluateConditional(point, subConditional, status));
      });

      return results.indexOf(true) > -1;
    } else if (conditional.and) {
      // Chained AND condition
      conditional.and.forEach((subConditional: any) => {
        results.push(this.evaluateConditional(point, subConditional, status));
      });

      return results.reduce((a: boolean, b: boolean) => { return (a === b) ? a : false; }) === true;
    }

    let statusResult: boolean, valueResult: boolean;

    // Conditional based on the status of the request.
    if (conditional.status) {
      if (conditional.status instanceof Array) {
        statusResult = conditional.status.indexOf(status) > -1;
      } else if (typeof conditional.status === 'string') {
        statusResult = status === conditional.status;
      }
    }

    // Conditional based on the value of another property of the point, used in conjunction with the status conditional
    if (conditional.condition) {
      valueResult = this.evaluateConditional(point, conditional.condition, status);
    }

    // Simple value conditional without status conditional
    if (conditional.field) {
      valueResult = this.evaluateValueCondition(point, conditional);
    }

    if (valueResult != null && statusResult != null) {
      return statusResult && valueResult;
    } else if (valueResult == null && statusResult != null) {
      return statusResult;
    } else if (valueResult != null && statusResult == null) {
      return valueResult;
    } else {
      return false;
    }
  }

  public evaluateValueCondition(point: Point, condition: any): boolean {
    let value: any = point.properties[condition.field];
    let result: boolean = false;

    if (condition.operation === 'equals' && value === condition.value) {
      result = true;
    } else if (condition.operation === 'notEquals' && value !== condition.value) {
      result = true;
    } else if (condition.operation === 'contains' && (value && value.toString().indexOf(condition.value) > -1)) {
      result = true;
    } else if (condition.operation === 'notNull' && (value != null && value !== '')) {
      result = true;
    } else if (condition.operation === 'isNull' && (value == null || value === '')) {
      result = true;
    } else if (condition.operation === 'in' && (condition.value.indexOf(value) > -1)) {
      result = true;
    }

    return result;
  }
}
