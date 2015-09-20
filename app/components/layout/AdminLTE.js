'use strict';

/*! AdminLTE app.js
 * ================
 * Main JS application file for AdminLTE v2. This file
 * should be included in all pages. It controls some layout
 * options and implements exclusive AdminLTE plugins.
 *
 * @Author  Almsaeed Studio
 * @Support <http://www.almsaeedstudio.com>
 * @Email   <support@almsaeedstudio.com>
 * @version 2.0
 * @license MIT <http://opensource.org/licenses/MIT>
 */

//Make sure jQuery has been loaded before app.js
if (typeof jQuery === 'undefined') {
  throw new Error('AdminLTE requires jQuery');
}

/* AdminLTE
 *
 * @type Object
 * @description $.AdminLTE is the main object for the template's app.
 *				It's used for implementing functions and options related
 *				to the template. Keeping everything wrapped in an object
 *				prevents conflict with other plugins and is a better
 *				way to organize our code.
 */
$.AdminLTE = {};

/* --------------------
 * - AdminLTE Options -
 * --------------------
 * Modify these options to suit your implementation
 */
$.AdminLTE.options = {
  //Add slimscroll to navbar menus
  //This requires you to load the slimscroll plugin
  //in every page before app.js
  navbarMenuSlimscroll: true,
  navbarMenuSlimscrollWidth: '3px', //The width of the scroll bar
  navbarMenuHeight: '200px', //The height of the inner menu
  //Sidebar push menu toggle button selector
  sidebarToggleSelector: '[data-toggle="offcanvas"]',
  //Activate sidebar push menu
  sidebarPushMenu: true,
  //Activate sidebar slimscroll if the fixed layout is set (requires SlimScroll Plugin)
  sidebarSlimScroll: true,
  //BoxRefresh Plugin
  enableBoxRefresh: true,
  //Bootstrap.js tooltip
  enableBSToppltip: true,
  BSTooltipSelector: '[data-toggle="tooltip"]',
  //Enable Fast Click. Fastclick.js creates a more
  //native touch ecperience with touch devices. If you
  //choose to enable the plugin, make sure you load the script
  //before AdminLTE's app.js
  enableFastclick: true,
  //Box Widget Plugin. Enable this plugin
  //to allow boxes to be collapsed and/or removed
  enableBoxWidget: true,
  //Box Widget plugin options
  boxWidgetOptions: {
    boxWidgetIcons: {
      //The icon that triggers the collapse event
      collapse: 'fa fa-minus',
      //The icon that trigger the opening event
      open: 'fa fa-plus',
      //The icon that triggers the removing event
      remove: 'fa fa-times'
    },
    boxWidgetSelectors: {
      //Remove button selector
      remove: '[data-widget="remove"]',
      //Collapse button selector
      collapse: '[data-widget="collapse"]'
    }
  },
  //Define the set of colors to use globally around the website
  colors: {
    lightBlue: '#3c8dbc',
    red: '#f56954',
    green: '#00a65a',
    aqua: '#00c0ef',
    yellow: '#f39c12',
    blue: '#0073b7',
    navy: '#001F3F',
    teal: '#39CCCC',
    olive: '#3D9970',
    lime: '#01FF70',
    orange: '#FF851B',
    fuchsia: '#F012BE',
    purple: '#8E24AA',
    maroon: '#D81B60',
    black: '#222222',
    gray: '#d2d6de'
  }
};

/* ------------------
 * - Implementation -
 * ------------------
 * The next block of code implements AdminLTE's
 * functions and plugins as specified by the
 * options above.
 */
$(function () {
  //Easy access to options
  var o = $.AdminLTE.options;

  //Activate the layout maker
  $.AdminLTE.layout.activate();

  //Enable sidebar tree view controls
  $.AdminLTE.tree('.sidebar');

  //Add slimscroll to navbar dropdown
  if (o.navbarMenuSlimscroll && typeof $.fn.slimscroll !== 'undefined') {
    $('.navbar .menu').slimscroll({
      height: '200px',
      alwaysVisible: false,
      size: '3px'
    }).css('width', '100%');
  }

  //Activate sidebar push menu
  if (o.sidebarPushMenu) {
    $.AdminLTE.pushMenu(o.sidebarToggleSelector);
  }

  //Activate Bootstrap tooltip
  if (o.enableBSToppltip) {
    $(o.BSTooltipSelector).tooltip();
  }

  //Activate box widget
  //if (o.enableBoxWidget) {
  //  $.AdminLTE.boxWidget.activate();
  //}

  //if(o.enableFastclick && typeof FastClick !== 'undefined') {
  //  FastClick.attach(document.body);
  //}

  /*
   * INITIALIZE BUTTON TOGGLE
   * ------------------------
   */
  $('.btn-group[data-toggle="btn-toggle"]').each(function () {
    var group = $(this);
    $(this).find('.btn').click(function (e) {
      group.find('.btn.active').removeClass('active');
      $(this).addClass('active');
      e.preventDefault();
    });

  });
});

/* ----------------------
 * - AdminLTE Functions -
 * ----------------------
 * All AdminLTE functions are implemented below.
 */

/* prepareLayout
 * =============
 * Fixes the layout height in case min-height fails.
 *
 * @type Object
 * @usage $.AdminLTE.layout.activate()
 *        $.AdminLTE.layout.fix()
 *        $.AdminLTE.layout.fixSidebar()
 */
$.AdminLTE.layout = {
  activate: function () {
    var _this = this;
    _this.fix();
    _this.fixSidebar();
    $(window, '.wrapper').resize(function () {
      _this.fix();
      _this.fixSidebar();
    });
  },
  fix: function () {
//    //Get window height and the wrapper height
//    var neg = $('.main-header').outerHeight() + $('.main-footer').outerHeight();
//    var window_height = $(window).height();
//    var sidebar_height = $('.sidebar').height();
//    //Set the min-height of the content and sidebar based on the
//    //the height of the document.
//    if ($('body').hasClass('fixed')) {
//      $('.content-wrapper, .right-side').css('min-height', window_height - $('.main-footer').outerHeight());
//    } else {
//      if (window_height >= sidebar_height) {
//        $('.content-wrapper, .right-side').css('min-height', window_height - neg);
//      } else {
//        $('.content-wrapper, .right-side').css('min-height', sidebar_height);
//      }
//    }
  },
  fixSidebar: function () {
    //Make sure the body tag has the .fixed class
    if (!$('body').hasClass('fixed')) {
      if (typeof $.fn.slimScroll !== 'undefined') {
        $('.sidebar').slimScroll({destroy: true}).height('auto');
      }
      return;
    } else if (typeof $.fn.slimScroll === 'undefined' && console) {
      console.error('Error: the fixed layout requires the slimscroll plugin!');
    }
    //Enable slimscroll for fixed layout
    if ($.AdminLTE.options.sidebarSlimScroll) {
      if (typeof $.fn.slimScroll !== 'undefined') {
        //Distroy if it exists
        $('.sidebar').slimScroll({destroy: true}).height('auto');
        //Add slimscroll
        $('.sidebar').slimscroll({
          height: ($(window).height() - $('.main-header').height()) + 'px',
          color: 'rgba(0,0,0,0.2)',
          size: '3px'
        });
      }
    }
  }
};

/* PushMenu()
 * ==========
 * Adds the push menu functionality to the sidebar.
 *
 * @type Function
 * @usage: $.AdminLTE.pushMenu('[data-toggle='offcanvas']')
 */
$.AdminLTE.pushMenu = function (toggleBtn) {
  //Enable sidebar toggle
  $(toggleBtn).click(function (e) {
    e.preventDefault();
    //Enable sidebar push menu
    $('body').toggleClass('sidebar-collapse');
    $('body').toggleClass('sidebar-open');
  });
  $('.content-wrapper').click(function () {
    //Enable hide menu when clicking on the content-wrapper on small screens
    if ($(window).width() <= 767 && $('body').hasClass('sidebar-open')) {
      $('body').removeClass('sidebar-open');
    }
  });

};

/* Tree()
 * ======
 * Converts the sidebar into a multilevel
 * tree view menu.
 *
 * @type Function
 * @Usage: $.AdminLTE.tree('.sidebar')
 */
$.AdminLTE.tree = function (menu) {
  $('li a', $(menu)).click(function (e) {
    //Get the clicked link and the next element
    var $this = $(this);
    var checkElement = $this.next();

    //Check if the next element is a menu and is visible
    if ((checkElement.is('.treeview-menu')) && (checkElement.is(':visible'))) {
      //Close the menu
      checkElement.slideUp('normal', function () {
        checkElement.removeClass('menu-open');
      });
      checkElement.parent('li').removeClass('active');
    }
    //If the menu is not visible
    else if ((checkElement.is('.treeview-menu')) && (!checkElement.is(':visible'))) {
      //Get the parent menu
      var parent = $this.parents('ul').first();
      //Close all open menus within the parent
      var ul = parent.find('ul:visible').slideUp('normal');
      //Remove the menu-open class from the parent
      ul.removeClass('menu-open');
      //Get the parent li
      var parentLi = $this.parent('li');

      //Open the target menu and add the menu-open class
      checkElement.slideDown('normal', function () {
        //Add the class active to the parent li
        checkElement.addClass('menu-open');
        parent.find('li.active').removeClass('active');
        parentLi.addClass('active');
      });
    }
    //if this isn't a link, prevent the page from being redirected
    if (checkElement.is('.treeview-menu')) {
      e.preventDefault();
    }
  });
};
