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
package cern.modesti.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import cern.modesti.request.point.Point;
import cern.modesti.request.Request.RequestStatus;
import cern.modesti.repository.mongo.request.counter.CounterService;
import cern.modesti.schema.Schema;
import cern.modesti.repository.mongo.schema.SchemaRepository;

/**
 * TODO
 *
 * The {@link cern.modesti.repository.mongo.request.RequestRepository} is automatically exposed as a REST resource via Spring Data REST, hence why there is no explicit MVC
 * controller for it. This class simply hooks into the Spring Data REST lifecycle and intercepts request create/save events.
 *
 *
 * @author Justin Lewis Salmon
 */
@Component
@RepositoryEventHandler(Request.class)
public class RequestEventHandler {

  Logger logger = LoggerFactory.getLogger(RequestEventHandler.class);

  @Autowired
  private CounterService counterService;

  @Autowired
  private SchemaRepository schemaRepository;

  /**
   * TODO
   *
   * @param request
   */
  @HandleBeforeCreate
  public void handleRequestCreate(Request request) {
    request.setStatus(RequestStatus.IN_PROGRESS);
    request.setRequestId(counterService.getNextSequence("requests").toString());
    logger.trace("beforeCreate() generated request id: " + request.getRequestId());

    if (request.getPoints().isEmpty()) {
      // TODO add a default, pre-filled point to a new request

    }

    for (Point point : request.getPoints()) {
      if (point.getId() == null) {
        point.setId(counterService.getNextSequence("points"));
        logger.debug("beforeCreate() generated point id: " + point.getId());
      }
    }

    // Link the correct schema to this request
//    Schema schema = schemaRepository.findOneByNameIgnoreCase(request.getDatasource());
//    request.setSchema(schema);
  }

  /**
   * TODO
   *
   * @param request
   */
  @HandleBeforeSave
  public void handleRequestSave(Request request) {
    for (Point point : request.getPoints()) {
      if (point.getId() == null) {
        point.setId(counterService.getNextSequence("points"));
        logger.debug("beforeSave() generated point id: " + point.getId());
      }
    }
  }
}
