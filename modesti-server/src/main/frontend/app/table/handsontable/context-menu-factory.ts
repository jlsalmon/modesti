export class ContextMenuFactory {

  public static getContextMenu(requestType: string, requestStatus: string): any {

    // TODO: take "schema.editableStates" into account

    if (requestType === 'CREATE') {
      if (requestStatus === 'IN_PROGRESS' || requestStatus === 'FOR_CORRECTION') {
        return ['row_above', 'row_below', '---------', 'remove_row', '---------', 'undo', 'redo'];
      } else {
        return false;
      }
    } else if (requestType === 'UPDATE') {
      if (requestStatus === 'IN_PROGRESS' || requestStatus === 'FOR_CORRECTION') {
        return ['remove_row', '---------', 'undo', 'redo'];
      } else {
        return false;
      }
    } else if (requestType === 'DELETE') {
      if (requestStatus === 'IN_PROGRESS' || requestStatus === 'FOR_CORRECTION') {
        return ['remove_row', '---------', 'undo', 'redo'];
      } else {
        return false;
      }
    }
  }
}
