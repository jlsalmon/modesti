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

  /**
   * Gets the schema identifier
   * @return schema identifier
   */
  String getId();

  /**
   * Sets the schema identifier
   * @param id schema identifier
   */
  void setId(String id);

  /**
   * Gets the abstract flag for the schema.
   * @return TRUE if and only if the schema is abstract
   */
  boolean isAbstract();
  
  /**
   * Gets the parent schema
   * @return parent schema
   */
  String getParent();

  /**
   * Sets the parent schema
   * @param parent parent schema
   */
  void setParent(String parent);

  /**
   * Gets the property used as identifier
   * @return id property
   */
  String getIdProperty();

  /**
   * Sets the property used as identifier
   * @param property id property
   */
  void setIdProperty(String property);
  
  /**
   * Gets the property used to define alarms
   * @return alarm property
   */
  String getAlarmProperty();
  
  /**
   * Sets the property used to define alarms
   * @param property alarm property
   */
  void setAlarmProperty(String property);
  
  /**
   * Gets the property used to define commands
   * @return command property
   */
  String getCommandProperty();
  
  /**
   * Sets the property used to define commands
   * @param property command property
   */
  void setCommandProperty(String property);

  /**
   * Gets the list of categories defined in the schema
   * @return list of categories
   */
  List<Category> getCategories();

  /**
   * Sets the list of categories for the schema
   * @param categories List of categories
   */
  void setCategories(List<Category> categories);
  
  /**
   * Gets the schema configuration properties
   * @return Schema configuration properties
   */
  Configuration getConfiguration();
  
  /**
   * Sets the schema configuration properties
   * @param configuration schema configuration properties
   */
  void setConfiguration(Configuration configuration);

  /**
   * Gets the list of fields overwritten from the child schema
   * @return categories including the overwritten fields
   */
  List<Category> getOverrides();

  /**
   * Sets the list of fields overwritten from the child schema
   * @param overrides categories including the overwritten fields
   */
  void setOverrides(List<Category> overrides);

  /**
   * Gets the different schema data sources
   * @return schema data sources
   */
  List<Datasource> getDatasources();

  /**
   * Sets the different schema data sources
   * @param datasources schema data sources
   */
  void setDatasources(List<Datasource> datasources);

  /**
   * Gets the data sources overwritten from the child schema
   * @return data sources overwritten from the child schema
   */
  List<Datasource> getDatasourceOverrides();

  /**
   * Sets the data sources overwritten from the child schema
   * @param overrides sources overwritten from the child schema
   */
  void setDatasourceOverrides(List<Datasource> overrides);

  /**
   * Gets the list of states where a point selection is enabled
   * @return List of workflow states where the point selection is enabled
   */
  List<String> getSelectableStates();

  /**
   * Sets the list of states where a point selection is enabled
   * @param selectableStates List of workflow states where the point selection is enabled
   */
  void setSelectableStates(List<String> selectableStates);

  /**
   * Gets the list of states where the comments on rows is enabled
   * @return list of states where the comments on rows is enabled
   */
  List<RowCommentStateDescriptor> getRowCommentStates();

  /**
   * Sets the list of states where the comments on rows is enabled
   * @param rowCommentStates list of states where the comments on rows is enabled
   */
  void setRowCommentStates(List<RowCommentStateDescriptor> rowCommentStates);
}
