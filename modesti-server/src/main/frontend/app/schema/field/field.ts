import {Conditional} from '../conditional';

export class Field {
  public id: string;
  public type: string;
  public name: string;
  public help: string;
  public helpUrl: string;
  public required: boolean;
  public unique: boolean;
  public editable: any;
  public template: string;
  public fixed: boolean;
  public default: any;
  public model: string;
  public minLength: number;
  public maxLength: number;
  public uppercase: boolean;
  public options: any[];
  public url: string;
  public params: string[];

  /**
   * Return the full model path of the field. This means the field id plus
   * the model attribute, separated by a space e.g. "person.name"
   *
   * For fields that are objects but have no 'model' attribute defined, assume
   * that the object has only a single property called 'value'.
   *
   * @returns {string}
   */
  public getModelPath(): string {
    let modelAttribute: string;

    if (this.type === 'autocomplete') {
      modelAttribute = this.id + '.' + (this.model ? this.model : 'value');
    } else {
      modelAttribute = this.id;
    }

    return modelAttribute;
  }

  public deserialize(field: Field): Field {
    this.id = field.id;
    this.type = field.type;
    this.name = field.name;
    this.help = field.help;
    this.helpUrl = field.helpUrl;
    this.required = field.required;
    this.unique = field.unique;
    this.editable = field.editable;
    this.template = field.template;
    this.fixed = field.fixed;
    this.default = field.default;
    this.model = field.model;
    this.minLength = field.minLength;
    this.maxLength = field.maxLength;
    this.uppercase = field.uppercase;
    this.options = field.options;
    this.url = field.url;
    this.params = field.params;
    return this;
  }
}
