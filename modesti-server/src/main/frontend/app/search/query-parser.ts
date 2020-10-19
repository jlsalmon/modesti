import {Filter} from '../table/filter';
import {Field} from '../schema/field/field';

export class QueryParser {

  public static parse(filters: Filter[]): string {
    let expressions: string[] = [];

    for (let i=0; i<filters.length; i++) {
      let filter: Filter = filters[i];
      if (filter && ((filter.value != null && filter.value !== '') || filter.operation === 'is-empty') )  {
        let property: string = filter.field.getModelPath();
        let operation: string = this.parseOperation(filter.operation);
        let value: string = filter.value;

        if (filter.operation === 'starts-with') {
          value += '*';
        } else if (filter.operation === 'ends-with') {
          value = '*' + value;
        } else if (filter.operation === 'contains') {
          value = '*' + value + '*';
        } else if (filter.operation === 'is-empty') {
          value = null;
        }

        let expression: string = property + ' ' + operation + ' "' + value + '"';

        if (expressions.indexOf(expression) === -1) {
          expressions.push(expression);
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
    } else if (operation === 'in') {
      return ' =in= ';
    } else {
      return ' == ';
    }
  }
}
