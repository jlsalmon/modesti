package cern.modesti.schema.category;


import lombok.NoArgsConstructor;

/**
 * A datasource is a special type of {@link Category}. Semantically, each
 * {@link cern.modesti.point.Point} should be associated with one and
 * only one datasource, although this concept is optional.
 */
@NoArgsConstructor
public class DatasourceImpl extends CategoryImpl implements Datasource {

  public DatasourceImpl(String id) {
    super(id);
  }

  public DatasourceImpl(CategoryImpl category) {
    super(category);
  }
}
