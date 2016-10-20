define(["require", "exports", '../../table/table-factory', 'jquery'], function (require, exports, table_factory_1) {
    "use strict";
    // TODO: import this properly without require()
    var Handsontable = require('handsontable-pro');
    var RequestTableComponent = (function () {
        function RequestTableComponent() {
            this.templateUrl = '/request/table/request-table.component.html';
            this.controller = RequestTableController;
            this.bindings = {
                request: '=',
                tasks: '=',
                schema: '=',
                table: '=',
                activeCategory: '=',
                history: '='
            };
        }
        return RequestTableComponent;
    }());
    exports.RequestTableComponent = RequestTableComponent;
    var RequestTableController = (function () {
        function RequestTableController($scope, $q, $filter, $localStorage, requestService, taskService, schemaService) {
            var _this = this;
            this.$scope = $scope;
            this.$q = $q;
            this.$filter = $filter;
            this.$localStorage = $localStorage;
            this.requestService = requestService;
            this.taskService = taskService;
            this.schemaService = schemaService;
            /**
             * Evaluate "editable" state of each cell
             *
             * TODO: this could happen on init
             */
            this.evaluateCellSettings = function (row, col, prop) {
                if (typeof prop !== 'string') {
                    return;
                }
                var task = _this.taskService.getCurrentTask();
                var authorised = false;
                if (_this.taskService.isCurrentUserAuthorised(task) && _this.taskService.isCurrentUserAssigned(task)) {
                    authorised = true;
                }
                var editable = false;
                if (authorised) {
                    var point_1 = _this.request.points[row];
                    // Evaluate "editable" condition of the category
                    if (_this.activeCategory.editable != null && typeof _this.activeCategory.editable === 'object') {
                        var conditional = _this.activeCategory.editable;
                        if (conditional != null) {
                            editable = _this.schemaService.evaluateConditional(point_1, conditional, _this.request.status);
                        }
                    }
                    // Evaluate "editable" condition of the field as it may override the category
                    _this.activeCategory.fields.forEach(function (field) {
                        if (field.id === prop.split('.')[1]) {
                            var conditional = field.editable;
                            if (conditional != null) {
                                editable = _this.schemaService.evaluateConditional(point_1, conditional, _this.request.status);
                            }
                        }
                    });
                    if (_this.schema.hasRowSelectColumn(_this.request.status) && prop === 'selected') {
                        editable = true;
                    }
                    else if (_this.schema.hasRowCommentColumn(_this.request.status) && prop.contains('message')) {
                        editable = true;
                    }
                }
                return { readOnly: !editable };
            };
            this.renderCell = function (instance, td, row, col, prop, value, cellProperties) {
                switch (cellProperties.type) {
                    case 'text':
                        Handsontable.renderers.TextRenderer.apply(_this, arguments);
                        break;
                    case 'numeric':
                        Handsontable.renderers.NumericRenderer.apply(_this, arguments);
                        break;
                    case 'checkbox':
                        Handsontable.renderers.CheckboxRenderer.apply(_this, arguments);
                        break;
                    default: break;
                }
                if (cellProperties.editor === 'select2') {
                    Handsontable.renderers.AutocompleteRenderer.apply(_this, arguments);
                }
                if (typeof prop !== 'string' || prop.indexOf('properties') === -1) {
                    return;
                }
                var point = _this.request.points[row];
                if (!point || point.isEmpty()) {
                    return;
                }
                var props = prop.split('.').slice(1, 3);
                var field = _this.schema.getField(props[0]);
                if (!field) {
                    return;
                }
                // Check if we need to fill in a default value for this point.
                _this.setDefaultValue(point, field);
                // Highlight errors in a cell by making the background red.
                angular.forEach(point.errors, function (error) {
                    if (error.property === prop.replace('properties.', '') || error.property === props[0] || error.property === '') {
                        // If the property name isn't specified, then the error applies to the whole point.
                        td.className += ' alert-danger';
                        return;
                    }
                    else if (!error.property || error.property === error.category) {
                        // Highlight an entire category if the property matches a category name.
                        var category = _this.schema.getCategory(error.category);
                        if (category) {
                            category.fields.forEach(function (f) {
                                if (f.id === field.id) {
                                    td.className += ' alert-danger';
                                    return;
                                }
                            });
                            return;
                        }
                    }
                });
                if (_this.request.type === 'UPDATE' && point.dirty === true) {
                    var changes_1 = [];
                    $(td).popover('destroy');
                    _this.history.events.forEach(function (event) {
                        event.changes.forEach(function (change) {
                            if (change.path.indexOf(field.id) !== -1 && change.path.indexOf('[' + point.lineNo + ']') !== -1) {
                                changes_1.push(change);
                            }
                        });
                    });
                    if (changes_1.length > 0) {
                        var latest = changes_1[changes_1.length - 1];
                        var original = void 0, modified = void 0;
                        if (field.type === 'autocomplete') {
                            original = field.model ? latest.original[field.model] : latest.original.value;
                            modified = field.model ? latest.modified[field.model] : latest.modified.value;
                        }
                        else {
                            original = latest.original;
                            modified = latest.modified;
                        }
                        var content = '<samp><table>';
                        content += '<tr><td style="background-color: #ffecec">&nbsp;- ' + original + '&nbsp;</td></tr>';
                        content += '<tr><td style="background-color: #dbffdb">&nbsp;+ ' + modified + '&nbsp;</td></tr></table></samp>';
                        td.style.background = '#fcf8e3';
                        $(td).popover({ trigger: 'hover', placement: 'top', container: 'body', html: true, content: content });
                    }
                }
            };
            /**
             * Slightly hacky little function to make sure all the elements on the page
             * are properly initialised.
             */
            this.onAfterRender = function () {
                console.log('onAfterRender');
                // Initialise the popovers in the row headers
                $('.row-header').popover({ trigger: 'hover', delay: { 'show': 100, 'hide': 100 } });
                // Initialise the help text popovers on the column headers
                $('.help-text').popover({ trigger: 'hover', delay: { 'show': 500, 'hide': 100 } });
                if (_this.schema.hasRowSelectColumn(_this.schema, _this.request.status)) {
                    var firstColumnHeader = $('.htCore colgroup col.rowHeader');
                    var lastColumnHeader = $('.htCore colgroup col:last-child');
                    var checkboxColumn = $('.htCore colgroup col:nth-child(2)');
                    // Fix the width of the 'select-all' checkbox column (second column)
                    // and add the surplus to the last column
                    var lastColumnHeaderWidth = lastColumnHeader.width() + (checkboxColumn.width() - 30);
                    lastColumnHeader.width(lastColumnHeaderWidth);
                    checkboxColumn.width('30px');
                    // Fix the width of the first column (point id column)
                    firstColumnHeader.width('45px');
                    // Centre checkboxes
                    var checkboxCells = $('.htCore input.htCheckboxRendererInput').parent();
                    checkboxCells.css('text-align', 'center');
                    // Initialise checkbox header state
                    var checkboxHeader = $('.select-all:checkbox');
                    checkboxHeader.prop(_this.getCheckboxHeaderState(), true);
                    var header = void 0, cells = void 0;
                    if (_this.schema.hasRowCommentColumn(_this.schema, _this.request.status)) {
                        header = $('.htCore thead th:nth-child(3)');
                        cells = $('.htCore tbody td:nth-child(3)');
                    }
                    else {
                        header = $('.htCore thead th:nth-child(2)');
                        cells = $('.htCore tbody td:nth-child(2)');
                    }
                    // Add a thicker border between the control column(s) and the first data column
                    header.css('border-right', '5px double #ccc');
                    cells.css('border-right', '5px double #ccc');
                    // Listen for the change event on the 'select-all' checkbox and act accordingly
                    checkboxHeader.change(function () {
                        for (var i = 0, len = _this.request.points.length; i < len; i++) {
                            _this.request.points[i].selected = _this.checked;
                        }
                        // Need to explicitly trigger a digest loop here because we are out
                        // of the angularjs world and in the happy land of jquery hacking
                        _this.$scope.$apply();
                    });
                    // Listen for change events on all checkboxes
                    $('.htCheckboxRendererInput:checkbox').change(function () {
                        $('.select-all:checkbox').prop(_this.getCheckboxHeaderState(), true);
                    });
                }
            };
            /**
             * Called after a change is made to the table (edit, paste, etc.)
             *
             * @param changes a 2D array containing information about each of the edited
             *                cells [ [row, prop, oldVal, newVal], ... ]
             * @param source one of the strings: "alter", "empty", "edit",
             *               "populateFromArray", "loadData", "autofill", "paste"
             */
            this.onAfterChange = function (changes, source) {
                // When the table is initially loaded, this callback is invoked with
                // source === 'loadData'. In that case, we don't want to do anything.
                if (source === 'loadData') {
                    return;
                }
                console.log('onAfterChange()');
                // Make sure the line numbers are consecutive
                // this.normaliseLineNumbers();
                var promises = [];
                // Loop over the changes and check if anything actually changed. Mark any changed points as dirty.
                var change, row, property, oldValue, newValue, dirty = false;
                for (var i = 0, ilen = changes.length; i < ilen; i++) {
                    change = changes[i];
                    row = change[0];
                    property = change[1];
                    oldValue = change[2];
                    newValue = change[3];
                    // Mark the point as dirty.
                    if (newValue !== oldValue) {
                        console.log('dirty point: ' + _this.request.points[row].lineNo);
                        dirty = true;
                        _this.request.points[row].dirty = true;
                    }
                    // If the value was cleared, make sure any other properties of the object are also cleared.
                    if (newValue == null || newValue === '') {
                        // let point = this.parent.hot.getSourceDataAtRow(row);
                        var point = _this.request.points[row];
                        var propName = property.split('.')[1];
                        var prop = point.properties[propName];
                        if (typeof prop === 'object') {
                            for (var attribute in prop) {
                                if (prop.hasOwnProperty(attribute)) {
                                    prop[attribute] = undefined;
                                }
                            }
                        }
                        else {
                            prop = undefined;
                        }
                    }
                    // This is a workaround. See function documentation for info.
                    var promise = _this.saveNewValue(row, property, newValue);
                    promises.push(promise);
                }
                // Wait for all new values to be updated
                _this.$q.all(promises).then(function () {
                    // If nothing changed, there's nothing to do! Otherwise, save the request.
                    if (dirty) {
                        _this.request.valid = false;
                        _this.requestService.saveRequest(_this.request).then(function (request) {
                            _this.request = request;
                            // Reload the history
                            _this.requestService.getRequestHistory(_this.request.requestId).then(function (history) {
                                _this.history = history;
                                _this.table.hot.render();
                            });
                        });
                    }
                });
            };
            /**
             * Navigate somewhere to focus on a particular field.
             *
             * @param categoryName the name of the category to which the field belongs
             * @param fieldId the id of the field to focus on
             */
            this.navigateToField = function (categoryName, fieldId) {
                // Find the category which contains the field
                var category;
                if (fieldId.indexOf('.') !== -1) {
                    fieldId = fieldId.split('.')[0];
                }
                _this.schema.categories.concat(_this.schema.datasources).forEach(function (cat) {
                    if (cat.name === categoryName || cat.id === categoryName) {
                        cat.fields.forEach(function (field) {
                            if (field.id === fieldId || cat.name === fieldId || cat.id === fieldId) {
                                category = cat;
                            }
                        });
                    }
                });
                if (category) {
                    _this.activeCategory = category;
                }
            };
            var task = this.taskService.getCurrentTask();
            var authorised = false;
            if (this.taskService.isCurrentUserAuthorised(task) && this.taskService.isCurrentUserAssigned(task)) {
                authorised = true;
            }
            var settings = {
                authorised: authorised,
                requestStatus: this.request.status,
                // TODO: is there a better way than passing the service?
                schemaService: this.schemaService,
                cellRenderer: this.renderCell,
                cells: this.evaluateCellSettings,
                afterChange: this.onAfterChange,
                afterRender: this.onAfterRender
            };
            this.table = table_factory_1.TableFactory.createTable('handsontable', this.schema, this.request.points, settings);
            // Add additional helper methods
            this.table.navigateToField = this.navigateToField;
        }
        /**
         * Inspect the given field and set the default value in the point if supplied. The default value can refer
         * to another property of the point via mustache-syntax, so interpolate that as well.
         *
         * @param point
         * @param field
         */
        RequestTableController.prototype.setDefaultValue = function (point, field) {
            var currentValue;
            if (field.type === 'autocomplete') {
                if (point.properties.hasOwnProperty(field.id) && point.properties[field.id]) {
                    currentValue = field.model ?
                        point.properties[field.id][field.model] : point.properties[field.id].value;
                }
            }
            else {
                currentValue = point.properties[field.id];
            }
            if (currentValue == null || currentValue === '') {
                var regex = /^\{\{\s*[\w\.]+\s*}}/g;
                if (field.default && typeof field.default === 'string' && regex.test(field.default)) {
                    var matches = field.default.match(regex).map(function (x) {
                        return x.match(/[\w\.]+/)[0];
                    });
                    var props = matches[0].split('.');
                    if (point.properties.hasOwnProperty(props[0])) {
                        var outerProp = point.properties[props[0]];
                        if (outerProp.hasOwnProperty(props[1])) {
                            var value = outerProp[props[1]];
                            var model = field.model ? field.model : 'value';
                            console.log('setting default value ' + value + ' for ' + field.id + '.' + model
                                + ' on point ' + point.lineNo);
                            if (point.properties.hasOwnProperty(field.id)) {
                                point.properties[field.id][model] = value;
                            }
                            else {
                                point.properties[field.id] = {};
                                point.properties[field.id][model] = value;
                            }
                        }
                    }
                }
            }
        };
        RequestTableController.prototype.getCheckboxHeaderState = function () {
            if (!this.table.hasOwnProperty('getSelectedLineNumbers')) {
                return 'unchecked';
            }
            if (this.table.getSelectedLineNumbers().length === this.request.points.length) {
                return 'checked';
            }
            else if (this.table.getSelectedLineNumbers().length > 0) {
                return 'indeterminate';
            }
            else {
                return 'unchecked';
            }
        };
        /**
         * Currently Handsontable does not support columns backed by complex objects,
         * so for now it's necessary to manually save the object in the background.
         * See https://github.com/handsontable/handsontable/issues/2578.
         *
         * Also, sometimes after a modification, Handsontable does not properly save
         * the new value to the underlying point. So we manually save the value in
         * the background to be doubly sure that the new value is persisted.
         *
         * @param row
         * @param property
         * @param newValue
         */
        RequestTableController.prototype.saveNewValue = function (row, property, newValue) {
            var q = this.$q.defer();
            var point = this.request.points[row];
            var outerProp;
            if (typeof property === 'string') {
                // get the outer object i.e. properties.location.value -> location
                outerProp = property.split('.')[1];
            }
            else {
                outerProp = this.activeCategory.fields[property].id;
            }
            var field = this.schema.getField(outerProp);
            // If there is no corresponding field in the schema, then it must be a "virtual"
            // column (such as "comment" or "checkbox")
            if (field == null) {
                q.resolve();
                return q.promise;
            }
            if (field.type === 'autocomplete') {
                // For autocomplete fields, re-query the values and manually save it back to the point.
                this.schemaService.queryFieldValues(field, newValue, point).then(function (values) {
                    values.forEach(function (item) {
                        var value = (field.model == null && typeof item === 'object') ? item.value : item[field.model];
                        if (value === newValue) {
                            console.log('saving new value');
                            delete item._links;
                            point.properties[outerProp] = item;
                        }
                    });
                    q.resolve();
                });
            }
            else {
                // For non-autocomplete fields, just manually save the new value.
                point.properties[outerProp] = newValue;
                q.resolve();
            }
            return q.promise;
        };
        RequestTableController.$inject = ['$scope', '$q', '$filter', '$localStorage',
            'RequestService', 'TaskService', 'SchemaService'];
        return RequestTableController;
    }());
});
