import {Point} from '../../request/point/point';
import 'lodash';

export class AgGridSelectionService {

  public selected: Point[] = [];

  public constructor() {}

  public add(item, path) {
    if (!_.some(this.selected, path, _.get(item, path))) {
      this.selected.push(item);
    }
  }

  public remove(item, path) {
    _.remove(this.selected, path, _.get(item, path));
  }

  public updateInGridSelections(gridApi, path) {
    let selectedInGrid = gridApi.getSelectedNodes();
    let gridPath = 'data.' + path;

    _.each(selectedInGrid, (node) => {
      if (!_.some(this.selected, path, _.get(node, gridPath))) {
        // The following suppressEvents=true flag is ignored for now, but a
        // fixing pull request is waiting at ag-grid GitHub.
        gridApi.deselectNode(node, true);
      }
    });

    let selectedIdsInGrid = _.map(selectedInGrid, gridPath);
    let currentlySelectedIds = _.map(this.selected, path);
    let missingIdsInGrid = _.difference(currentlySelectedIds, selectedIdsInGrid);

    if (missingIdsInGrid.length > 0) {
      // We're trying to avoid the following loop, since it seems horrible to
      // have to loop through all the nodes only to select some.  I wish there
      // was a way to select nodes/rows based on an id.
      var i;

      gridApi.forEachNode((node) => {
        i = _.indexOf(missingIdsInGrid, _.get(node, gridPath));
        if (i >= 0) {
          // multi=true, suppressEvents=true:
          gridApi.selectNode(node, true, true);

          missingIdsInGrid.splice(i, 1);  // Reduce haystack.
          if (!missingIdsInGrid.length) {
            // I'd love for `forEachNode` to support breaking the loop here.
          }
        }
      });
    }
  }
}
