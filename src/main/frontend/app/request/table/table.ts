import {Request} from '../request';
import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category';
import {Field} from '../../schema/field';

export class Table {

  /** the handsontable instance */
  public hot: any;
  public activeCategory: Category;

  public constructor(private $localStorage: any, private request: Request, private schema: Schema, hot: any) {
    this.hot = hot;
    $localStorage.$default({
      lastActiveCategory: {}
    });
  }

  public activateDefaultCategory = () => {
    let categoryId: string = this.$localStorage.lastActiveCategory[this.request.requestId];
    let category: Category;

    if (!categoryId) {
      console.log('activating default category');
      category = this.schema.categories[0];
    } else {
      console.log('activating last active category: ' + categoryId);

      this.schema.categories.concat(this.schema.datasources).forEach((cat: Category) => {
        if (cat.id === categoryId) {
          category = cat;
        }
      });

      if (!category) {
        category = this.schema.categories[0];
      }
    }

    this.activateCategory(category);
  };

  public activateCategory = (category: Category) => {
    console.log('activating category "' + category.id + '"');
    this.activeCategory = category;
    this.$localStorage.lastActiveCategory[this.request.requestId] = category.id;
    this.refreshColumns(category);
  };

  public getSelectedLineNumbers = () => {
    if ($.isEmptyObject(this.hot)) {
      return [];
    }

    let checkboxes: any[] = this.hot.getDataAtProp('selected');
    let lineNumbers: number[] = [];

    for (let i: number = 0, len: number = checkboxes.length; i < len; i++) {
      if (checkboxes[i]) {
        // Line numbers are 1-based
        lineNumbers.push(this.request.points[i].lineNo);
      }
    }

    return lineNumbers;
  };

  /**
   * Navigate somewhere to focus on a particular field.
   *
   * @param categoryName
   * @param fieldId
   */
  public navigateToField = (categoryName: string, fieldId: string) => {

    // Find the category which contains the field.
    let category: Category;

    if (fieldId.indexOf('.') !== -1) {
      fieldId = fieldId.split('.')[0];
    }

    this.schema.categories.concat(this.schema.datasources).forEach((cat: Category) => {
      if (cat.name === categoryName || cat.id === categoryName) {
        cat.fields.forEach((field: Field) => {
          if (field.id === fieldId || cat.name === fieldId || cat.id === fieldId) {
            category = cat;
          }
        });
      }
    });

    if (category) {
      this.activateCategory(category);
    }
  };
}
