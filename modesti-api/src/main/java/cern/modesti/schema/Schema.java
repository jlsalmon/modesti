package cern.modesti.schema;

import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.configuration.Configuration;

import java.io.Serializable;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public interface Schema extends Serializable {

  String getId();

  void setId(String id);

  boolean isAbstract();

  String getParent();

  void setParent(String parent);

  String getIdProperty();

  void setIdProperty(String property);

  List<Category> getCategories();

  void setCategories(List<Category> categories);
  
  Configuration getConfiguration();
  
  void setConfiguration(Configuration configuration);

  List<Category> getOverrides();

  void setOverrides(List<Category> overrides);

  List<Datasource> getDatasources();

  void setDatasources(List<Datasource> datasources);

  List<Datasource> getDatasourceOverrides();

  void setDatasourceOverrides(List<Datasource> overrides);

  List<String> getSelectableStates();

  void setSelectableStates(List<String> selectableStates);

  List<RowCommentStateDescriptor> getRowCommentStates();

  void setRowCommentStates(List<RowCommentStateDescriptor> rowCommentStates);
}
