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

import cern.modesti.request.counter.CounterService;
import cern.modesti.request.point.Point;
import cern.modesti.workflow.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * The {@link cern.modesti.request.RequestRepository} is
 * automatically exposed as a REST resource via Spring Data REST, hence why
 * there is no explicit MVC controller for it. This class simply hooks into the
 * Spring Data REST lifecycle and intercepts request create/save events, and
 * lets Spring Data REST do everything else automatically.
 *
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
@RepositoryEventHandler(Request.class)
public class RequestEventHandler {

  @Autowired
  private CounterService counterService;

  @Autowired
  private WorkflowService workflowService;

  /**
   * TODO
   *
   * @param request
   */
  @HandleBeforeCreate
  public void handleRequestCreate(Request request) {
    request.setRequestId(counterService.getNextSequence(CounterService.REQUEST_ID_SEQUENCE).toString());
    log.trace("beforeCreate() generated request id: " + request.getRequestId());

    // Add some empty points if there aren't any yet
    if (request.getPoints().isEmpty()) {
      for (int i = 0; i < 10; i++) {
        // Point IDs are 1-based
        Point point = new Point((long) (i + 1));
        request.getPoints().add(point);
      }
    }

    for (Point point : request.getPoints()) {
      if (point.getId() == null) {
        // Point IDs are 1-based
        point.setId((long) (request.getPoints().indexOf(point) + 1));
        log.debug("beforeCreate() generated point id: " + point.getId());
      }
    }

    // Kick off the workflow process
    workflowService.startProcessInstance(request);
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
        // Point IDs are 1-based
        point.setId((long) (request.getPoints().indexOf(point) + 1));
        log.debug("beforeSave() generated point id: " + point.getId());
      }
    }
  }
}
