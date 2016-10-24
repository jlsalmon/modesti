import {Conditional} from '../conditional';
import {Field} from '../field/field';

export class Category {
  public id: string;
  public name: string;
  public editable: Conditional;
  public fields: Field[];
}