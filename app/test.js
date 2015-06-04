angular.module('test', ['ngHandsontable']).controller('TestController', TestController);

function TestController($scope) {

  var firstNames = ["Ted", "John", "Macy", "Rob", "Gwen", "Fiona", "Mario", "Ben", "Kate", "Kevin", "Thomas", "Frank"];
  var lastNames = ["Tired", "Johnson", "Moore", "Rocket", "Goodman", "Farewell", "Manson", "Bentley", "Kowalski", "Schmidt", "Tucker", "Fancy"];

  $scope.colHeaders = true;
  $scope.rowHeaders = true;

  $scope.db = {};
  $scope.db.items = [];
  for (var i = 0; i < 10; i++) {
    $scope.db.items.push(
      {
        name: {
          first: firstNames[Math.floor(Math.random() * firstNames.length)],
          last: lastNames[Math.floor(Math.random() * lastNames.length)]
        },
        isActive: Math.floor(Math.random() * firstNames.length) / 2 == 0 ? 'Yes' : 'No'

      }
    );
  }

  $scope.afterInit = function () {
    // get the handsontable instance to use
    $scope.handsTableInstance = this;

    var example = document.getElementById('table');

    var offset;


    offset = Handsontable.Dom.offset(example);
    var availableWidth = Handsontable.Dom.innerWidth(document.body) - offset.left + window.scrollX;
    var availableHeight = Handsontable.Dom.innerHeight(document.body) - offset.top + window.scrollY;

    example.style.width = availableWidth + 'px';
    example.style.height = availableHeight + 'px';
  }
}