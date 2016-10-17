import {Request} from './request';
import {Schema} from '../schema/schema';
import {Category} from '../schema/category/category';
import IScope = angular.IScope;

export class RequestComponent implements ng.IComponentOptions {
  public templateUrl: string = '/request/request.component.html';
  public controller: Function = RequestController;
  public bindings: any = {
    request: '=',
    children: '=',
    schema: '=',
    tasks: '=',
    signals: '=',
    history: '='
  };
}

class RequestController {
  public static $inject: string[] = ['$scope', '$localStorage'];

  public request: Request;
  public schema: Schema;
  public activeCategory: Category;

  public constructor(private $scope: IScope, private $localStorage: any) {
    //this.activeCategory = this.getLastActiveCategory();
    //
    //$scope.$watch(() => this.activeCategory, () => {
    //  this.setLastActiveCategory(this.activeCategory);
    //});
  }

  ///**
  // * @returns {Category} the last active category for the current request
  // * from local storage, or the first category in the schema if local storage
  // * is empty
  // */
  //public getLastActiveCategory(): Category {
  //  this.$localStorage.$default({lastActiveCategory: {}});
  //
  //  let categoryId: string = this.$localStorage.lastActiveCategory[this.request.requestId];
  //  let category: Category;
  //
  //  if (!categoryId) {
  //    console.log('using default category');
  //    category = this.schema.categories[0];
  //  } else {
  //    console.log('using last active category: ' + categoryId);
  //
  //    this.schema.categories.concat(this.schema.datasources).forEach((cat: Category) => {
  //      if (cat.id === categoryId) {
  //        category = cat;
  //      }
  //    });
  //
  //    if (!category) {
  //      category = this.schema.categories[0];
  //    }
  //  }
  //
  //  return category;
  //};
  //
  ///**
  // * Persist the last active category for the current request
  // *
  // * @param category the category to set
  // */
  //public setLastActiveCategory(category: Category): void {
  //  this.$localStorage.lastActiveCategory[this.request.requestId] = category.id;
  //}
}
