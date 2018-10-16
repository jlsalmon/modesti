import IComponentOptions = angular.IComponentOptions;
import {TableService} from '../table.service';


export class SearchControlsComponent implements IComponentOptions {
  public templateUrl: string = '/search/footer/search.controls.component.html';
  public controller: Function = SearchControlsController;
  public bindings: any = {
    request: '=' 
  };
}

class SearchControlsController {
  public static $inject: string[] = ['TableService'];

  public constructor(private tableService: TableService) {}

  public updatePoints(): void {
    this.tableService.updatePoints();
  }

  public deletePoints(): void {
    this.tableService.deletePoints();
  }

  public getTable(): TableService {
    return this.tableService;
  }
}


