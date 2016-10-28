export class ContextMenuFactory {

  public static getContextMenu(requestType: string): string[] {
    if (requestType === 'CREATE') {
      return ['row_above', 'row_below', '---------', 'remove_row', '---------', 'undo', 'redo']
    } else if (requestType === 'UPDATE') {
      return ['remove_row', '---------', 'undo', 'redo']
    } else if (requestType === 'DELETE') {
      return ['remove_row', '---------', 'undo', 'redo']
    }
  }
}
