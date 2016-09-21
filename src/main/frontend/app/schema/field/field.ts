import {Conditional} from '../conditional';

export class Field {
  public id: string;
  public type: string;
  public name: string;
  public help: string;
  public required: boolean;
  public unique: boolean;
  public editable: any;
  public fixed: boolean;
  public default: any;
  public model: string;
  public minLength: number;
  public maxLength: number;
  public uppercase: boolean;
  public url: string;
  public params: string[];
}
