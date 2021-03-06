export class Point implements ISerializable<Point> {
  public lineNo: number;
  public valid: boolean;
  public dirty: boolean;
  public selected: boolean;
  public errors: any[];
  public properties: any;

  public getProperty(property: string): any {
    if (property.indexOf('properties') !== -1) {
      property = property.split('.')[1];
    }

    return this.properties[property];
  }

  public setProperty(property: string, value: any): void {
    if (property.indexOf('properties') !== -1) {
      property = property.split('.')[1];
    }

    this.properties[property] = value;
  }

  public getPropertyAsString(property: string) : string {
    let prop : any = this.getProperty(property);
    let value : string = '';

    if (prop === null || prop === undefined) {
      return value;
    }

    if (typeof prop === 'object') {
      if (property === 'responsiblePerson') {
        value = prop.name;
      } else {
        value = prop.value;
      }
    } else {
      value = prop;
    }

    if (value === undefined || value == null) {
      value = '';
    }

    return value;
  }

  /**
   * Check if a point is empty. A point is considered to be empty if it
   * contains no properties, or if the values of all its properties are either
   * null, undefined, empty strings or Boolean values.
   *
   * @returns {boolean} true if the point is empty, false otherwise
   */
  public isEmpty(): boolean {
    if (Object.keys(this.properties).length === 0) {
      return true;
    }

    let property: any;
    for (let key in this.properties) {
      if (this.properties.hasOwnProperty(key)) {
        property = this.properties[key];

        if (typeof property === 'object') {
          for (let subproperty in property) {
            if (property.hasOwnProperty(subproperty)) {
              if (property[subproperty] != null && property[subproperty] !== '') {
                return false;
              }
            }
          }
        } else if (property != null && property !== '' && property != true && property != false) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Set an error message on a single field of a point.
   *
   * @param propertyName the name of the property for which to set the error
   * @param message the error message
   */
  public setErrorMessage(propertyName: string, message: string): void {
    let exists: boolean = false;

    this.errors.forEach((error: any) => {
      if (error.property === propertyName) {
        exists = true;
        if (error.errors.indexOf(message) > -1) {
          error.errors.push(message);
        }
      }
    });

    if (!exists) {
      this.errors.push({property: propertyName, errors: [message]});
    }
  }

  public deserialize(json: any): Point {
    this.lineNo = json.lineNo;
    this.valid = json.valid;
    this.dirty = json.dirty;
    this.selected = json.selected;
    this.errors = json.errors;
    this.properties = json.properties;
    return this;
  }
}
