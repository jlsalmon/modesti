import {Category} from './category/category';
import {Field} from './field/field';
import {RowCommentStateDescriptor} from './row-comment-state-descriptor';

export class Schema implements ISerializable<Schema> {
  public id: string;
  public categories: Category[];
  public datasources: Category[];
  public fields: Field[];
  public selectableStates: string[];
  public rowCommentStates: RowCommentStateDescriptor[];

  public getCategory(id: string): Category {
    let result: Category;

    this.categories.concat(this.datasources).forEach((category: Category) => {
      if (category.id === id || category.name === id) {
        result = category;
      }
    });

    return result;
  }

  public getField(id: string): Field {
    let result: Field;

    this.categories.concat(this.datasources).forEach((category: Category) => {
      category.fields.forEach((field: Field) => {
        if (field.id === id) {
          result = field;
        }
      });
    });

    return result;
  }

  public deserialize(json: any): Schema {
    this.id = json.id;
    this.categories = json.categories;
    this.datasources = json.datasources;
    this.fields = json.fields;
    this.selectableStates = json.selectableStates;
    this.rowCommentStates = json.rowCommentStates;
    return this;
  }
}
