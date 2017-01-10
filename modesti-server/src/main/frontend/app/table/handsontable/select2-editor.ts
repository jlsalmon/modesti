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
export class Select2Editor extends Handsontable.editors.TextEditor {

  private table: Table;
  private field: Field;
  private schemaService: SchemaService;
  private select2Options: any;

  public init(): void {
    super.init();
  }

  public prepare(row: number, col: number, prop: string, td: Element, originalValue: any, cellProperties: any): void {
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
        value = value[this.field.getModelPath()];
      }

      callback({id: value, text: value});
    };

    this.select2Options.nextSearchTerm = (selectedObject: any, currentSearchTerm: string) => {
      return currentSearchTerm;
    };
  }

  public createElements(): void {
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

  public onBeforeKeyDown = (event: KeyboardEvent): void => {
    var keyCodes = Handsontable.helper.KEY_CODES;
    // Catch CTRL but not right ALT (which in some systems triggers ALT+CTRL)
    var ctrlDown = (event.ctrlKey || event.metaKey) && !event.altKey;

    event.isImmediatePropagationStopped = false;

    // Process only events that have been fired in the editor
    if (!$(event.target).hasClass('select2-input') || Handsontable.Dom.isImmediatePropagationStopped(event)) {
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
          this.$textarea.select2('close');
        }
        break;

      case keyCodes.ARROW_LEFT:
        if (Handsontable.Dom.getCaretPosition(target) !== 0) {
          event.stopImmediatePropagation();
        } else {
          this.$textarea.select2('close');
        }
        break;

      case keyCodes.ENTER:
        var selected = this.instance.getSelected();
        var isMultipleSelection = !(selected[0] === selected[2] && selected[1] === selected[3]);
        // If ctrl+enter or alt+enter, add new line
        if ((ctrlDown && !isMultipleSelection) || event.altKey) {
          if (this.isOpened()) {
            this.val(this.val() + '\n');
            this.focus();
          } else {
            this.beginEditing(this.originalValue + '\n')
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
      if (eventData.choice) {
        // item selected
        var dataObj = eventData.choice.data;
        var selectedId = eventData.choice.id;
      } else {
        // item cleared

      }
    });

    // Set reference to the instance and row, so we can access them in the query function
    this.$textarea[0].instance = self.instance;
    this.$textarea[0].row = self.row;

    this.$textarea.select2('open');

    // For autocomplete fields, push the original value to select2 if we have one
    if (this.originalValue != null) {
      if (this.field.type === 'autocomplete' || (this.field.type === 'text' && this.field.url != null)) {
        this.$textarea.select2('search', this.originalValue);
      }
    }
  }

  public close() {
    this.instance.listen();
    this.instance.removeHook('beforeKeyDown', onBeforeKeyDown);
    this.instance.selection.deselect();
    this.$textarea.off();
    this.$textarea.hide();
    super.close();
  };

  public val(value) {
    if (typeof value == 'undefined') {
      return this.$textarea.val();
    } else {
      this.$textarea.val(value);
    }
  };

  public onSelect2Changed = () => {
    this.close();
    this.finishEditing();
  };

  public onSelect2Closed = () => {
    this.close();
    this.finishEditing();
  };

  public close(): void {
    super.close();
  }

  public focus(): void {
    this.instance.listen();
  }

  public beginEditing(initialValue: any, event: KeyboardEvent): void {
    var onBeginEditing = this.instance.getSettings().onBeginEditing;
    if (onBeginEditing && onBeginEditing() === false) {
      return;
    }

    super.beginEditing(initialValue, event);
  }

  public finishEditing(isCancelled: boolean, ctrlDown: boolean): void {
    super.finishEditing(isCancelled, ctrlDown);
  }
}
