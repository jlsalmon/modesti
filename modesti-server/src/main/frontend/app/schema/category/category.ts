import {Conditional} from '../conditional';
import {Field} from '../field/field';

export class Category {
  public id: string;
  public name: string;
  public editable: Conditional;
  public fields: Field[];

  public deserialize(category: Category): Category {
    this.id = category.id;
    this.name = category.name;
    this.editable = category.editable;

    if (category.fields) {
      this.fields = [];
      category.fields.forEach((field: Field) => {
        this.fields.push(new Field().deserialize(field));
      });
    }

    return this;
  }
}
