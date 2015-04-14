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
package cern.modesti.repository.jpa;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(collectionResourceRel="validate", path="validate")
public interface UserRepository extends CrudRepository<User, String> {

  // Explicitly mapped to named stored procedure {@code User.plus1} in the {@link EntityManager}.
  // By default, we would've try to find a procedure declaration named User.plus1BackedByOtherNamedStoredProcedure
  @Procedure(name = "User.plus1")
  Integer plus1BackedByOtherNamedStoredProcedure(@Param("arg") Integer arg);

  // Directly map the method to the stored procedure in the database (to avoid the annotation madness on your domain classes).
  //@Procedure
  //Integer plus1inout(@Param("arg") Integer arg);
}
