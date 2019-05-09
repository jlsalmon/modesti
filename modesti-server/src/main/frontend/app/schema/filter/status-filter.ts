import {Field} from '../field/field';

/**
 * Predefined visualization filter for a workflow status
 */
export class StatusFilter {
  public status: string;
  public fields: Field[] = [];

  public constructor(status: string) {
    this.status = status;
  }

  public addField(field: Field) {
    this.fields.push(field);
  }
}