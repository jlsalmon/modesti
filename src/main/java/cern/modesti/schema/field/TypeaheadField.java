/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.modesti.schema.field;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public class TypeaheadField extends Field {

  private String url;

  private List<String> params = new ArrayList<String>();

  private Integer minLength;

  private String returnPropertyName;

  private String template;

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the params
   */
  public List<String> getParams() {
    return params;
  }

  /**
   * @param params the params to set
   */
  public void setParams(List<String> params) {
    this.params = params;
  }

  /**
   * @return the minLength
   */
  public Integer getMinLength() {
    return minLength;
  }

  /**
   * @param minLength the minLength to set
   */
  public void setMinLength(Integer minLength) {
    this.minLength = minLength;
  }

  /**
   * @return the returnPropertyName
   */
  public String getReturnPropertyName() {
    return returnPropertyName;
  }

  /**
   * @param returnPropertyName the returnPropertyName to set
   */
  public void setReturnPropertyName(String returnPropertyName) {
    this.returnPropertyName = returnPropertyName;
  }

  /**
   * @return the template
   */
  public String getTemplate() {
    return template;
  }

  /**
   * @param template the template to set
   */
  public void setTemplate(String template) {
    this.template = template;
  }
}
