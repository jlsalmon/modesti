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
package cern.modesti.repository.jpa.location.site;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import cern.modesti.model.Site;
import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 */
public interface SiteRepository extends ReadOnlyRepository<Site, String> {

  /**
   * TODO
   *
   * We don't need a manual query here, as the functionalities table is very
   * simple. So we annotate the {@link Site} class with the necessary table and
   * column names and let Spring create a query automatically based on the
   * method name.
   *
   * We also rename the REST endpoint for this resource via the
   * {@link RestResource} annotation to make things nicer.
   *
   * @param name
   * @return
   */
  @RestResource(rel = "findByName", path = "findByName")
  @Cacheable("sites")
  List<Site> findByNameStartsWithIgnoreCase(@Param("name") String name);
}
