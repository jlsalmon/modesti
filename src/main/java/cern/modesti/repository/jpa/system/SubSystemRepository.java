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
package cern.modesti.repository.jpa.system;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import cern.modesti.model.SubSystem;
import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(collectionResourceRel = "subsystems", path = "subsystems")
public interface SubSystemRepository extends ReadOnlyRepository<SubSystem, String> {

  /**
   *
   * @param name
   * @return
   */
  @Query(value = "SELECT tes_system_name || ' ' || tess_subsystem_name as name "
               + "FROM   vpts_sysdet "
               + "WHERE  tes_system_name || ' ' || tess_subsystem_name LIKE UPPER(:name || '%') " , nativeQuery = true)
  public List<SubSystem> findByName(@Param("name") String name);

//  @Override
//  @Query(value = "SELECT rv_low_value as name FROM cg_ref_codes WHERE rv_domain = 'PTDATATYPES'", nativeQuery = true)
//  public List<SubSystem> findAll();
}
