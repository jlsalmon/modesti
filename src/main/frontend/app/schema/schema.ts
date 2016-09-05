import {Category} from './category';
import {Field} from './field';

export class Schema {
  public id: string;
  public categories: Category[];
  public datasources: Category[];
  public fields: Field[];
}
