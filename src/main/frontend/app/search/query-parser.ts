import {Filter} from '../table/column-panel/filter';
import {Field} from '../schema/field/field';

export class QueryParser {

  public static parse(filters: Map<string, Filter>): string {
    let expressions: string[] = [];

    for (let key in filters) {
      if (filters.hasOwnProperty(key)) {
        let filter: Filter = filters[key];

        if (filter && filter.value != null && filter.value !== '') {
          let property: string = filter.field.getModelPath();
          let operation: string = this.parseOperation(filter.operation);
          let value: string = filter.value;

          if (filter.operation === 'starts-with') {
            value += '*';
          } else if (filter.operation === 'ends-with') {
            value = '*' + value;
          } else if (filter.operation === 'contains') {
            value = '*' + value + '*';
          }

          let expression: string = property + ' ' + operation + ' "' + value + '"';

          if (expressions.indexOf(expression) === -1) {
            expressions.push(expression);
          }
        }
      }
    }

    let query: string = expressions.join(' and ');
    console.log('parsed query: ' + query);
    return query;
  }

  private static parseOperation(operation: string): string {
    if (operation === 'equals') {
      return ' == ';
    } else if (operation === 'not-equals') {
      return ' != ';
    } else if (operation === 'less-than') {
      return ' < ';
    } else if (operation === 'greater-than') {
      return ' > ';
    } else {
      return ' == ';
    }
  }
}
