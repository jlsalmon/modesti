import {Table} from '../table';
import {Point} from '../../request/point/point';
import {Field} from '../../schema/field/field';
import {SchemaService} from '../../schema/schema.service';

import 'jquery';
import 'select2';

// TODO: import this properly without require()
let Handsontable: any = require('handsontable-pro');

/**
 * Custom Handsontable editor using Select2: https://select2.github.io
 *
 * To understand how this works, see the documentation about creating custom
 * editors: http://docs.handsontable.com/pro/1.7.0/tutorial-cell-editor.html
 */
export class Select2EditorNew extends Handsontable.editors.TextEditor {

  private table: Table;
  private field: Field;
  private schemaService: SchemaService;
  private select: any;
  private select2Options: any;

  public init(): void {
    console.log('select2-editor: init');
    super.init();
  }

  public prepare(row: number, col: number, prop: string, td: Element, originalValue: any, cellProperties: any): void {
    console.log('select2-editor: prepare');
    super.prepare(row, col, prop, td, originalValue, cellProperties);

    this.table = cellProperties.table;
    this.field = cellProperties.field;
    this.schemaService = cellProperties.schemaService;
    this.select2Options = cellProperties.select2Options;

    this.select2Options.formatSelection = (option: any) => {
      return option.text;
    };

    this.select2Options.initSelection = (element: any, callback: any) => {
      let point: Point = this.table.data[row];
      let value: any = point.getProperty(this.prop);

      if (value && typeof value === 'object') {
        value = value[this.getModelAttribute(this.field)];
      }

      callback({id: value, text: value});
    };

    this.select2Options.nextSearchTerm = (selectedObject: any, currentSearchTerm: string) => {
      return currentSearchTerm;
    };
  }

  public createElements(): void {
    console.log('select2-editor: createElements');
    // super.createElements();

    this.TEXTAREA = document.createElement('input');
    this.TEXTAREA.setAttribute('type', 'text');
    this.$textarea = $(this.TEXTAREA);

    Handsontable.Dom.addClass(this.TEXTAREA, 'handsontableInput');

    this.textareaStyle = this.TEXTAREA.style;
    this.textareaStyle.width = 0;
    this.textareaStyle.height = 0;

    this.TEXTAREA_PARENT = document.createElement('DIV');
    Handsontable.Dom.addClass(this.TEXTAREA_PARENT, 'handsontableInputHolder');

    this.textareaParentStyle = this.TEXTAREA_PARENT.style;
    this.textareaParentStyle.top = 0;
    this.textareaParentStyle.left = 0;
    this.textareaParentStyle.display = 'none';

    this.TEXTAREA_PARENT.appendChild(this.TEXTAREA);

    this.instance.rootElement.appendChild(this.TEXTAREA_PARENT);

    var that = this;
    this.instance._registerTimeout(setTimeout(function () {
      that.refreshDimensions();
    }, 0));
  }

  public onBeforeKeyDown(event: KeyboardEvent): void {
    var keyCodes = Handsontable.helper.keyCode;
    // Catch CTRL but not right ALT (which in some systems triggers ALT+CTRL)
    var ctrlDown = (event.ctrlKey || event.metaKey) && !event.altKey;

    //Handsontable.Dom.enableImmediatePropagation(event);

    // Process only events that have been fired in the editor
    if (!$(event.target).hasClass('select2-input') || event.isImmediatePropagationStopped()) {
      return;
    }
    if (event.keyCode === 17 || event.keyCode === 224 || event.keyCode === 91 || event.keyCode === 93) {
      // When CTRL or its equivalent is pressed and cell is edited, don't prepare selectable text in textarea
      event.stopImmediatePropagation();
      return;
    }

    var target = event.target;

    switch (event.keyCode) {
      case keyCodes.ARROW_RIGHT:
        if (Handsontable.Dom.getCaretPosition(target) !== target.value.length) {
          event.stopImmediatePropagation();
        } else {
          that.$textarea.select2('close');
        }
        break;

      case keyCodes.ARROW_LEFT:
        if (Handsontable.Dom.getCaretPosition(target) !== 0) {
          event.stopImmediatePropagation();
        } else {
          that.$textarea.select2('close');
        }
        break;

      case keyCodes.ENTER:
        var selected = that.instance.getSelected();
        var isMultipleSelection = !(selected[0] === selected[2] && selected[1] === selected[3]);
        // If ctrl+enter or alt+enter, add new line
        if ((ctrlDown && !isMultipleSelection) || event.altKey) {
          if (that.isOpened()) {
            that.val(that.val() + '\n');
            that.focus();
          } else {
            that.beginEditing(that.originalValue + '\n')
          }
          event.stopImmediatePropagation();
        }
        // Don't add newline to field
        event.preventDefault();
        break;

      case keyCodes.A:
      case keyCodes.X:
      case keyCodes.C:
      case keyCodes.V:
        if (ctrlDown) {
          // CTRL+A, CTRL+C, CTRL+V, CTRL+X should only work locally when cell is edited (not in table context)
          event.stopImmediatePropagation();
        }
        break;

      case keyCodes.BACKSPACE:
      case keyCodes.DELETE:
      case keyCodes.HOME:
      case keyCodes.END:
        // Backspace, delete, home, end should only work locally when cell is edited (not in table context)
        event.stopImmediatePropagation();
        break;
    }
  }

  public open(keyboardEvent: KeyboardEvent): void {
    console.log('select2-editor: open');

    this.refreshDimensions();
    this.textareaParentStyle.display = 'block';
    this.instance.addHook('beforeKeyDown', this.onBeforeKeyDown);

    this.$textarea.css({
      height: $(this.TD).height() + 4,
      'min-width': $(this.TD).outerWidth() - 4
    });

    // Display the list
    this.$textarea.show();

    var self = this;
    this.$textarea.select2(this.select2Options)
    .on('change', this.onSelect2Changed)
    .on('select2-close', this.onSelect2Closed)
    .on('select2-selected', (eventData: any) => {
      if ( eventData.choice ) {
        // item selected
        var dataObj = eventData.choice.data;
        var selectedId = eventData.choice.id;
      } else {
        // item cleared

      }
    });

    // Set reference to the instance and row, so we can access them in the query function
    self.$textarea[0].instance = self.instance;
    self.$textarea[0].row = self.row;

    self.$textarea.select2('open');

    // Pushes initial character entered into the search field, if available
    if (keyboardEvent && keyboardEvent.keyCode) {
      var key = keyboardEvent.keyCode;
      var keyText = (String.fromCharCode((96 <= key && key <= 105) ? key-48 : key)).toLowerCase();
      self.$textarea.select2('search', keyText);
    }

    // super.open(event);
    //
    // this.select = $(this.TEXTAREA).select2({
    //  dropdownAutoWidth: true,
    //  width: 'resolve',
    //  query: (query: any) => this.load(this.row, this.col, query.term, query.callback),
    //
    //  /**
    //   * Called when Select2 is created to allow the user to initialize the
    //   * selection based on the value of the element select2 is attached to.
    //   * Essentially this is an id->object mapping function.
    //   *
    //   * @param element element Select2 is attached to.
    //   * @param callback callback function that should be called with the data
    //   * which is either an object in case of a single select or an array of
    //   * objects in case of multi-select.
    //   */
    //  initSelection: (element: any, callback: any) => {
    //    console.log('select2-editor: initSelection');
    //    // Push the current value to the editor
    //    let value: any = this.table.data[this.row].getProperty(this.prop);
    //
    //    if (typeof value === 'object') {
    //      let id: string = JSON.stringify(value);
    //      let text: any = value[this.getModelAttribute(this.field)];
    //
    //      if (text) {
    //        callback({ id: id, text: text.toString() });
    //      } else {
    //        callback({ id: '', text: '' });
    //      }
    //
    //    } else {
    //      callback({ id: '', text: '' });
    //    }
    //  },
    //
    //  /**
    //   * Function used to render the current selection.
    //   *
    //   * @param option The selected result object returned from the query
    //   * function.
    //   * @returns {any} Html string, a DOM element, or a jQuery object that
    //   * renders the selection.
    //   */
    //  formatSelection: (option: any) => {
    //    console.log('select2-editor: formatSelection');
    //
    //    //if (option.id !== '') {
    //    //  let value: any = JSON.parse(option.id);
    //    //  return value[this.getModelAttribute(this.field)].toString();
    //    //} else {
    //    //  return option.id;
    //    //}
    //  },
    //
    //  /**
    //   * Function used to determine what the next search term should be.
    //   *
    //   * @param selectedObject Retrieved data.
    //   * @param currentSearchTerm Search term that yielded the current result
    //   * set.
    //   * @returns {any}
    //   */
    //  nextSearchTerm: (selectedObject: any, currentSearchTerm: any) => {
    //    console.log('select2-editor: nextSearchTerm');
    //
    //    if (selectedObject.id !== '') {
    //      let value: any = JSON.parse(selectedObject.id);
    //      return value[this.getModelAttribute(this.field)].toString();
    //    } else {
    //      return selectedObject.id;
    //    }
    //  }
    // });
    //
    // this.select.on('change', () => {this.close(); this.finishEditing(undefined, undefined); });
    // this.select.on('select2-close', () => {
    //  this.close();
    //  this.finishEditing(undefined, undefined);
    // });
    //
    //// Push initial character entered into the search field, if available
    // if (event && event.keyCode) {
    //  let key: number = event.keyCode;
    //  let keyText: string = (String.fromCharCode((96 <= key && key <= 105) ? key - 48 : key)).toLowerCase();
    //  this.select.select2('search', keyText);
    //  console.log(keyText + ' ' + this.select.val());
    // } else {
    //  this.select.select2('open');
    //}
  }

  public close() {
    this.instance.listen();
    this.instance.removeHook('beforeKeyDown', onBeforeKeyDown);
    this.instance.selection.deselect();
    this.$textarea.off();
    this.$textarea.hide();
    super.close();
  };

  public val (value) {
    if (typeof value == 'undefined') {
      return this.$textarea.val();
    } else {
      this.$textarea.val(value);
    }
  };

  public onSelect2Changed = () => {
    this.close();
    this.finishEditing();
  }

  public onSelect2Closed = () => {
    this.close();
    this.finishEditing();
  }

  //public load(row: number, col: number, query: string, callback: Function): void {
  //  let point: Point = this.table.data[row];
  //
  //  this.schemaService.queryFieldValues(this.field, query, point).then((values: any[]) => {
  //
  //    let currentValue: any = point.getProperty(this.prop);
  //
  //    // Duck-schema the property if it doesn't exist, in order to make
  //    // handsontable happy
  //    if (!currentValue) {
  //      let firstOption: any = values[0];
  //      point.setProperty(this.prop, this.duckSchema(firstOption));
  //    }
  //
  //    // Re-map the values in a format that the select2 editor likes
  //    let results: any[] = values.map((value: any) => {
  //      if (typeof value === 'string') {
  //        return {id: value, text: value.toString()};
  //      } else {
  //        // Serialise the whole thing to JSON for later
  //        delete value._links;
  //        return {id: JSON.stringify(value), text: value[this.getModelAttribute(this.field)].toString()};
  //      }
  //    });
  //
  //    // Invoke the editor callback so it can populate itself
  //    callback({results: results, text: 'text'});
  //  });
  //}
  //
  //public duckSchema(value: any): any {
  //  let schema: ValueHolder = new ValueHolder();
  //
  //  for (let property in value) {
  //    if (value.hasOwnProperty(property) && property !== '_links') {
  //      schema[property] = null;
  //    }
  //  }
  //
  //  return Object.keys(schema).length === 0 ? undefined : schema;
  //}

  public getModelAttribute(field: Field): string {
    // For fields that are objects but have no 'model' attribute defined, assume that
    // the object has only a single property called 'value'.
    return field.model ? field.model : 'value';
  }

  public close(): void {
    console.log('select2-editor: close');
    super.close();
  }

  ///**
  // * Return the current editor value, that is value that should be saved
  // * as the new cell value.
  // *
  // * @returns {any}
  // */
  //public getValue(): any {
  //  console.log('select2-editor: getValue');
  //
  //  if (this.select) {
  //    let value: any = this.select.val();
  //
  //    if (value === '') {
  //      return value;
  //    }
  //
  //    // Should always be a JSON string
  //    value = JSON.parse(value);
  //
  //    let valueHolder: ValueHolder = new ValueHolder();
  //    for (let key in value) {
  //      if (value.hasOwnProperty(key)) {
  //        valueHolder[key] = value[key];
  //      }
  //    }
  //
  //    return valueHolder;
  //  }
  //}
  //
  ///**
  // * Set the editor value.
  // *
  // * @param value the value to set
  // */
  //public setValue(value: any): void {
  //  console.log('select2-editor: setValue');
  //
  //  if (this.select) {
  //    if (value != null && value !== '') {
  //      this.select.val(JSON.stringify(value));
  //    }
  //  }
  //}

  public focus(): void {
    console.log('select2-editor: focus');
    this.instance.listen();
    // super.focus();
  }

  public beginEditing(initialValue: any, event: KeyboardEvent): void {
    console.log('select2-editor: beginEditing');
    var onBeginEditing = this.instance.getSettings().onBeginEditing;
    if (onBeginEditing && onBeginEditing() === false) {
      return;
    }

    super.beginEditing(initialValue, event);
  }

  public finishEditing(isCancelled: boolean, ctrlDown: boolean): void {
    console.log('select2-editor: finishEditing');

    //if (this.select) {
    //  this.select.select2('close');
    //}

    super.finishEditing(isCancelled, ctrlDown);
  }
}

//class ValueHolder {
//
//  public toString() {
//    return 'LOL';
//  }
//}
