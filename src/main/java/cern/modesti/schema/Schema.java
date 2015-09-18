/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * <p>
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
 * <p>
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.modesti.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Id;

import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Datasource;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Justin Lewis Salmon
 */
@Document
@Data
@NoArgsConstructor
public class Schema {

  @Id
  private String id;

  private String name;

  private String domain;

  @JsonProperty("extends")
  private String parent;

  @JsonDeserialize(using = CategoryDeserializer.class)
  private List<Category> categories = new ArrayList<>();

  private List<Datasource> datasources = new ArrayList<>();

  private List<Category> overrides = new ArrayList<>();

  private List<Datasource> datasourceOverrides = new ArrayList<>();

  /**
   * @param id
   * @param name
   * @param domain
   */
  public Schema(String id, String name, String domain) {
    this.id = id;
    this.name = name;
    this.domain = domain;
  }

  public static class CategoryDeserializer extends JsonDeserializer<List<Category>> {

    @Override
    public List<Category> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      List<Category> categories = new ArrayList<>();

      JsonNode list = parser.getCodec().readTree(parser);

      for (JsonNode node : list) {
        if (node.isObject()) {
          ObjectMapper mapper = new ObjectMapper();
          Category category = mapper.treeToValue(node, Category.class);
          categories.add(category);
        }

        else if (node.isTextual()) {
          categories.add(new Category(node.asText()));
        }
      }

      return categories;
    }
  }
}
