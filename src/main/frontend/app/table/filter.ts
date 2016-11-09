import {Field} from '../../schema/field/field';

export class Filter {
  public field: Field;
  public operation: string;
  public value: string;
}
