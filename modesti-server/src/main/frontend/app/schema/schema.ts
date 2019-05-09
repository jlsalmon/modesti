import {Category} from './category/category';
import {Field} from './field/field';
import {Configuration} from './configuration/configuration';
import {RowCommentStateDescriptor} from './row-comment-state-descriptor';
import {StatusFilter} from './filter/status-filter'

export class Schema implements ISerializable<Schema> {
  public id: string;
  public description: string;
  public categories: Category[];
  public datasources: Category[];
  public fields: Field[];
  public allFields: Field[];
  public configuration: Configuration;
  public primary: string;
  public primaryField: Field;
  public alarm: string;
  public alarmField: Field;
  public command: string;
  public commandField: Field;
  public selectableStates: string[];
  public rowCommentStates: RowCommentStateDescriptor[];
  private statusFilters: { [status: string]: StatusFilter; } = {};

  public getCategory(id: string): Category {
    let category: Category;

    this.categories.concat(this.datasources).forEach((c: Category) => {
      if (c.id === id || c.name === id) {
        category = c;
        return;
      }
    });

    return category;
  }

  public getAllCategories(): Category[] {
    return this.categories.concat(this.datasources);
  }

  public getField(id: string, categoryName: string): Field {
    let field: Field;

    if (id.indexOf('properties.') !== -1) {
      id = id.substring(11);
    }

    if (id.indexOf('.') !== -1) {
      id = id.split('.')[0];
    }

    this.categories.concat(this.datasources).forEach((category: Category) => {
      category.fields.forEach((f: Field) => {
        if ((f.id === id && categoryName === null) || (f.id === id && category.name === categoryName)) {
          field = f;
          return;
        }
      });
    });

    return field;
  }

  public getCategoryForField(field: any): Category {
    let category: Category;

    this.getAllCategories().forEach((c: Category) => {
      let fieldIds: string[] = c.fields.map((f: Field) => f.id);

      if (fieldIds.indexOf(field.id) !== -1 && field.category === c.name) {
        category = c;
        return;
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

  public getIdProperty(): string {
    return 'properties.' + this.primary;
  }

  public getPrimaryField(): Field {
    return this.primaryField;
  }

  public getAlarmField(): Field {
    return this.alarmField;
  }

  public getCommandField(): Field {
    return this.commandField;
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

  private addToStatusFilter(field: Field) : void {
    if (field.filters === undefined) {
      return;
    }

    if (typeof field.filters === 'string') {
      this.addToFilter(field.filters, field);
    } else if (field.filters instanceof Array) {
      field.filters.forEach((filter: string) => {
        this.addToFilter(filter, field);
      });
    }
  }

  private addToFilter(status: string, field: Field) : void {
    if (this.statusFilters[status] === undefined) {
      this.statusFilters[status] = new StatusFilter(status);
    }

    let filter : StatusFilter = this.statusFilters[status];
    filter.addField(field);
  }

  public getStatusFilter(status: string) : StatusFilter {
    return this.statusFilters[status];
  }

  public deserialize(schema: Schema): Schema {
    this.id = schema.id;
    this.description = schema.description;
    this.primary = schema.primary;
    this.alarm = schema.alarm;
    this.command = schema.command;
    this.selectableStates = schema.selectableStates;
    this.rowCommentStates = schema.rowCommentStates;
    this.configuration = schema.configuration;
    this.allFields = [];

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

    this.categories.concat(this.datasources).forEach((category: any) => {
      category.fields.forEach((field: any) => {
        field.category = category.name;
        this.allFields.push(field);
        this.addToStatusFilter(field);
        if (field.id === schema.primary) {
          this.primaryField = field;
        } else if (field.id === schema.alarm) {
          this.alarmField = field;
        } else if (field.id === schema.command) {
          this.commandField = field;
        }

      });
    });

    return this;
  }
}
