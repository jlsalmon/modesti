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
    let category: Category;

    this.categories.concat(this.datasources).forEach((c: Category) => {
      if (c.id === id || c.name === id) {
        category = c;
      }
    });

    return category;
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

  public getCategoryForField(field: Field): Category {
    let category: Category;

    this.getAllCategories().forEach((c: Category) => {
      let fieldIds: string[] = c.fields.map((f: Field) => f.id);

      if (fieldIds.indexOf(field.id) !== -1) {
        category = c;
      }
    });

    return category;
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

  public determineIdProperty(): string {
    let property: string;

    // Choose the first field marked as "unique"
    // TODO: this would be better served by having a "primary" flag to avoid ambiguity
    this.getAllFields().forEach((field: Field) => {
      if (field.unique) {
        property = field.id;
        return;
      }
    });

    return 'properties.' + property;
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

  public deserialize(schema: Schema): Schema {
    this.id = schema.id;
    this.description = schema.description;
    this.selectableStates = schema.selectableStates;
    this.rowCommentStates = schema.rowCommentStates;

    if (schema.categories) {
      this.categories = [];
      schema.categories.forEach((category: Category) => {
        this.categories.push(new Category().deserialize(category));
      });
    }

    if (schema.datasources) {
      this.datasources = [];
      schema.datasources.forEach((category: Category) => {
        this.datasources.push(new Category().deserialize(category));
      });
    }

    if (schema.fields) {
      this.fields = [];
      schema.fields.forEach((field: Field) => {
        this.fields.push(new Field().deserialize(field));
      });
    }

    return this;
  }
}
