import {Filter} from '../table/column-panel/filter';
import {Field} from '../schema/field/field';

export class QueryParser {

  public static parse(filters: Map<string, Filter>): string {
    let expressions: string[] = [];

    for (let fieldId in filters) {
      let filter: Filter = filters[fieldId];

      if (filter.value != null && filter.value !== '') {

        let property: string;
        let field: Field = filter.field;

        if (field.type === 'autocomplete') {
          let modelAttribute: string = filter.field.model ? field.model : 'value';
          property = field.id + '.' + modelAttribute;
        } else {
          property = field.id;
        }

        let operation: string = this.parseOperation(filter.operation);
        let expression: string = property + ' ' + operation + ' "' + filter.value + '"';

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
    } else {
      console.warn('not supported!');
      return ' == ';
    }
  }
}
