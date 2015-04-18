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
package cern.modesti.repository.mongo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import cern.modesti.Request;

/**
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(collectionResourceRel = "requests", path = "requests")
public interface RequestRepository extends MongoRepository<Request, String> {

//  Page<Request> findByRequestId(@Param("id") Long requestId, Pageable pageable);

//  @Query(value = "{'title': {$regex : ?0, $options: 'i'}}")
//  Page<Request> findAllByRegex(String regexString);

  /**
   *
   * @param criteria
   * @param page
   * @return
   */
  Page<Request> findAllByOrderByScoreDesc(@Param("q") TextCriteria criteria, Pageable page);
}
