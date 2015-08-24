package cern.modesti.schema.options;

import cern.modesti.schema.Schema;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.field.OptionsField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@RunWith(MockitoJUnitRunner.class)
public class OptionServiceTest {

  @InjectMocks
  OptionService optionService;

  @Mock
  EntityManager entityManager;

  @Mock
  Query query;

  @Test
  public void optionListIsInjectedCorrectly() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.getSingleResult()).thenReturn("a,b,c", (Object) null);

    Schema schema = getTestSchema();
    optionService.injectOptions(schema);

    List<String> options = (List<String>) ((OptionsField) schema.getCategories().iterator().next().getFields().get(0)).getOptions();

    assertTrue(options != null);
    assertTrue(options.get(0).equals("a"));
    assertTrue(options.get(1).equals("b"));
    assertTrue(options.get(2).equals("c"));
  }

  @Test
  public void optionRangeIsInjectedCorrectly() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.getSingleResult()).thenReturn("-2:2", (Object) null);

    Schema schema = getTestSchema();
    optionService.injectOptions(schema);

    List<String> options = (List<String>) ((OptionsField) schema.getCategories().iterator().next().getFields().get(0)).getOptions();

    assertTrue(options != null);
    assertTrue(options.get(0).equals("-2"));
    assertTrue(options.get(1).equals("-1"));
    assertTrue(options.get(2).equals("0"));
    assertTrue(options.get(3).equals("1"));
    assertTrue(options.get(4).equals("2"));
  }

  private Schema getTestSchema() {
    OptionsField field1 = new OptionsField();
    field1.setId("lsacType");

    Category category = new Category("test");
    category.setFields(Collections.singletonList(field1));

    Schema schema = new Schema();
    schema.setCategories(Collections.singletonList(category));
    return schema;
  }
}
