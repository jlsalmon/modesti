import {Category} from './category/category';
import {Field} from './field/field';
import {RowCommentStateDescriptor} from './row-comment-state-descriptor';

export class Schema implements ISerializable<Schema> {
  public id: string;
  public description: string;
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

  public getAllCategories(): Category[] {
    return this.categories.concat(this.datasources);
  }

  public getField(id: string): Field {
    let field: Field;

    if (id.indexOf('properties.') !== -1) {
      id = id.substring(11);
    }

    if (id.indexOf('.') !== -1) {
      id = id.split('.')[0];
    }

    this.categories.concat(this.datasources).forEach((category: Category) => {
      category.fields.forEach((f: Field) => {
        if (f.id === id) {
          field = f;
        }
      });
    });

    return field;
  }

  public getAllFields(): Field[] {
    let fields: Field[] = [];

    this.categories.concat(this.datasources).forEach((category: any) => {
      category.fields.forEach((field: any) => {
        field.category = category.name;
        fields.push(field);
      });
    });

    return fields;
  }

  public hasRowSelectColumn(requestStatus: string): boolean {
    let selectableStates: string[] = this.selectableStates;
    return selectableStates && selectableStates.indexOf(requestStatus) > -1;
  }

  public hasRowCommentColumn(requestStatus: string): boolean {
    let rowCommentStates: RowCommentStateDescriptor[] = this.rowCommentStates;
    if (!rowCommentStates) {
      return false;
    }

    let has: boolean = false;
    rowCommentStates.forEach((rowCommentState: any) => {
      if (rowCommentState.status === requestStatus) {
        has = true;
      }
    });

    return has;
  }

  public deserialize(json: any): Schema {
    this.id = json.id;
    this.description = json.description;
    this.categories = json.categories;
    this.datasources = json.datasources;
    this.fields = json.fields;
    this.selectableStates = json.selectableStates;
    this.rowCommentStates = json.rowCommentStates;
    return this;
  }
}
