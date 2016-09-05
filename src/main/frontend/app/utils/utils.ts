import {Point} from "../request/point/point";
import {Schema} from "../schema/schema";
import {Category} from "../schema/category";
import {Field} from "../schema/field";

export class Utils {

  /**
   * Check if a point is empty. A point is considered to be empty if it contains no properties, or if the values of all
   * its properties are either null, undefined or empty strings.
   *
   * @param point the point to check
   * @returns {boolean} true if the point is empty, false otherwise
   */
  public isEmptyPoint(point: Point): boolean {
    if (Object.keys(point.properties).length === 0) {
      return true;
    }

    let property: any;
    for (let key in point.properties) {
      if (point.properties.hasOwnProperty(key)) {
        property = point.properties[key];

        if (typeof property === 'object') {
          for (let subproperty in property) {
            if (property.hasOwnProperty(subproperty)) {
              if (property[subproperty] !== undefined && property[subproperty] !== '') {
                return false;
              }
            }
          }
        } else if (property !== undefined && property !== '') {
          return false;
        }
      }
    }

    return true;
  }

  public getCategory(schema: Schema, id: string): Category {
    let result: Category;

    schema.categories.concat(schema.datasources).forEach((category: Category) => {
      if (category.id === id || category.name === id) {
        result = category;
      }
    });

    return result;
  }

  /**
   * Retrieve a field object matching the given id from the given schema.
   *
   * @param schema
   * @param id
   * @returns {*}
   */
  public getField(schema: Schema, id: string): Field {
    let result: Field;

    schema.categories.forEach((category: Category) => {
      category.fields.forEach((field: Field) => {
        if (field.id === id) {
          result = field;
        }
      });
    });

    schema.datasources.forEach((datasource: Category) => {
      datasource.fields.forEach((field: Field) => {
        if (field.id === id) {
          result = field;
        }
      });
    });

    return result;
  }
}
