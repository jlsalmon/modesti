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
package cern.modesti.repository.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.stereotype.Component;

import cern.modesti.model.Point;
import cern.modesti.model.Request;
import cern.modesti.model.Request.RequestStatus;
import cern.modesti.repository.request.schema.Category;
import cern.modesti.repository.request.schema.Schema;
import cern.modesti.repository.request.schema.SchemaRepository;
import cern.modesti.repository.request.schema.field.Field;
import cern.modesti.repository.request.util.CounterService;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Component
@RepositoryEventHandler(Request.class)
public class RequestEventHandler implements BackendIdConverter {

  Logger logger = LoggerFactory.getLogger(RequestEventHandler.class);

  @Autowired
  private CounterService counterService;

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private SchemaRepository schemaRepository;

  @Autowired
  private MongoTemplate mongoTemplate;

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

    for (Point point : request.getPoints()) {
      if (point.getId() == null) {
        point.setId(counterService.getNextSequence("points"));
        logger.trace("beforeCreate() generated point id: " + point.getId());
      }
    }

//    // Link the correct schema to this request
//    Schema schema = schemaRepository.findOneByName(request.getDomain().toLowerCase());
//
//    // Merge the domain-specific schema with the core schema
//    Schema coreSchema = schemaRepository.findOneByName("core");
//    List<Category> categories = schema.getCategories();
//    List<Category> newCategories = new ArrayList<>();
//
//    for (Category coreCategory : coreSchema.getCategories()) {
//      if (!categories.contains(coreCategory)) {
//        newCategories.add(coreCategory);
//        
//      } else {
//        Category category = categories.get(categories.indexOf(coreCategory));
//        List<Field> newFields = new ArrayList<>();
//        
//        for (Field coreField : coreCategory.getFields()) {
//          if (!category.getFields().contains(coreField)) {
//            newFields.add(coreField);
//          }
//        }
//        
//        newFields.addAll(category.getFields());
//        category.setFields(newFields);
//      }
//    }
//    
//    newCategories.addAll(schema.getCategories());
//    schema.setCategories(newCategories);
//
//    schemaRepository.save(schema);
//
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
        logger.trace("beforeSave() generated point id: " + point.getId());
      }
    }
  }

  /**
   * TODO
   *
   * @param delimiter
   * @return
   */
  @Override
  public boolean supports(Class<?> delimiter) {
    return true;
  }

  /**
   * TODO
   *
   * @param id
   * @param entityType
   * @return
   */
  @Override
  public Serializable fromRequestId(String id, Class<?> entityType) {

    if (entityType.equals(Request.class)) {
      logger.trace("fromRequestId() converting request id: " + id);

      BasicQuery query = new BasicQuery("{ requestId : \"" + id + "\" }");
      Request request = mongoTemplate.findOne(query, Request.class);

      if (request != null) {
        return request.getId();
      }
    } else if (entityType.equals(Schema.class)) {
      logger.trace("fromRequestId() converting schema id: " + id);

      BasicQuery query = new BasicQuery("{ name : \"" + id + "\" }");
      Schema schema = mongoTemplate.findOne(query, Schema.class);

      if (schema != null) {
        return schema.getId();
      }
    }

    return id;
  }

  /**
   * TODO
   *
   * @param id
   * @param entityType
   * @return
   */
  @Override
  public String toRequestId(Serializable id, Class<?> entityType) {

    if (entityType.equals(Request.class)) {
      logger.trace("toRequestId() converting request id : " + id);
      Request request = requestRepository.findOne(id.toString());

      if (request != null) {
        return request.getRequestId().toString();
      }

    } else if (entityType.equals(Schema.class)) {
      logger.trace("toRequestId() converting schema id : " + id);
      Schema schema = schemaRepository.findOne(id.toString());
      
      if (schema != null) {
        return schema.getName();
      }
    }

    return id.toString();
  }
}
