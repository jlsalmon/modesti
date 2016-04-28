package cern.modesti.schema.category;


import lombok.NoArgsConstructor;

/**
 * A datasource is a special type of {@link Category}. Semantically, each
 * {@link cern.modesti.request.point.Point} should be associated with one and
 * only one datasource, although this concept is optional.
 */
@NoArgsConstructor
public class Datasource extends Category {

  public Datasource(String id) {
    super(id);
  }

  public Datasource(Category category) {
    super(category);
  }
}
