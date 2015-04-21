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
package cern.modesti.repository.jpa.person;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cern.modesti.model.Person;
import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 */
public interface PersonRepository extends ReadOnlyRepository<Person, String> {

  @Query(value = "SELECT person_id as id, first_name || ' ' || last_name as name "
               + "FROM   persons_mv "
               + "WHERE  at_cern = 'Y' AND cern_class = 'STAF' "
               + "AND   (person_id  LIKE UPPER(:id || '%') "
               + "OR     first_name || ' ' || last_name LIKE UPPER('%' || :name || '%'))", nativeQuery = true)
  @Cacheable("persons")
  public List<Person> findByIdOrName(@Param("id") String id, @Param("name") String name);
}
