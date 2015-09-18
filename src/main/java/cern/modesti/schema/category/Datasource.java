package cern.modesti.schema.category;


import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Datasource extends Category {

  public Datasource(String id) {
    super(id);
  }

  public Datasource(Category category) {
    super(category);
  }
}
