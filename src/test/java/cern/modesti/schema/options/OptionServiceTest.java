package cern.modesti.schema.options;

import cern.modesti.schema.Schema;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.field.Field;
import cern.modesti.schema.field.Option;
import cern.modesti.schema.field.OptionsField;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.contains;
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

  @Test
  public void optionList() {
    Schema schema = new Schema();
    Category category = new Category("testCategory");
    Field field = new OptionsField();
    field.setId("optionList");
    category.setFields(Collections.singletonList(field));
    schema.setCategories(Collections.singletonList(category));

    Query q = mock(Query.class);
    when(entityManager.createNativeQuery(contains("OPTION_LIST"))).thenReturn(q);
    when(q.getSingleResult()).thenReturn("a,b,c", (Object) null);

    optionService.injectOptions(schema);

    List<Option> options = (List<Option>) ((OptionsField) schema.getCategories().iterator().next().getFields().get(0)).getOptions();

    assertTrue(options != null);
    assertTrue(options.get(0).getValue().equals("a"));
    assertTrue(options.get(0).getId() == 0);
    assertTrue(options.get(1).getValue().equals("b"));
    assertTrue(options.get(1).getId() == 1);
    assertTrue(options.get(2).getValue().equals("c"));
    assertTrue(options.get(2).getId() == 2);
  }

  @Test
  public void optionsWithMeanings() {
    Schema schema = new Schema();
    Category category = new Category("testCategory");
    Field field = new OptionsField();
    field.setId("optionsWithMeanings");
    category.setFields(Collections.singletonList(field));
    schema.setCategories(Collections.singletonList(category));

    Query q = mock(Query.class);
    when(entityManager.createNativeQuery(contains("OPTIONS_WITH_MEANINGS"))).thenReturn(q);
    when(q.getSingleResult()).thenReturn("a,b,c", "a desc", "b desc", "c desc");

    optionService.injectOptions(schema);

    List<Option> options = (List<Option>) ((OptionsField) schema.getCategories().iterator().next().getFields().get(0)).getOptions();

    assertTrue(options != null);
    assertTrue(options.get(0).getValue().equals("a: a desc"));
    assertTrue(options.get(0).getId() == 0);
    assertTrue(options.get(1).getValue().equals("b: b desc"));
    assertTrue(options.get(1).getId() == 1);
    assertTrue(options.get(2).getValue().equals("c: c desc"));
    assertTrue(options.get(2).getId() == 2);
  }

  @Test
  public void optionRange() {
    Schema schema = new Schema();
    Category category = new Category("testCategory");
    Field field = new OptionsField();
    field.setId("optionRange");
    category.setFields(Collections.singletonList(field));
    schema.setCategories(Collections.singletonList(category));

    Query q = mock(Query.class);
    when(entityManager.createNativeQuery(contains("OPTION_RANGE"))).thenReturn(q);
    when(q.getSingleResult()).thenReturn("-2:2", (Object) null);

    optionService.injectOptions(schema);

    List<Option> options = (List<Option>) ((OptionsField) schema.getCategories().iterator().next().getFields().get(0)).getOptions();

    assertTrue(options != null);
    assertTrue(options.get(0).getValue().equals("-2"));
    assertTrue(options.get(0).getId() == -2);
    assertTrue(options.get(1).getValue().equals("-1"));
    assertTrue(options.get(1).getId() == -1);
    assertTrue(options.get(2).getValue().equals("0"));
    assertTrue(options.get(2).getId() == 0);
    assertTrue(options.get(3).getValue().equals("1"));
    assertTrue(options.get(3).getId() == 1);
    assertTrue(options.get(4).getValue().equals("2"));
    assertTrue(options.get(4).getId() == 2);
  }

  @Test
  public void pointTypes() {
    Schema schema = new Schema();
    Datasource datasource = new Datasource("testDatasource");
    datasource.setName_en(datasource.getId());
    Field field = new OptionsField();
    field.setId("pointType");
    datasource.setFields(Collections.singletonList(field));
    schema.setDatasources(Collections.singletonList(datasource));

    optionService.injectOptions(schema);

    List<Option> options = (List<Option>) ((OptionsField) schema.getDatasources().iterator().next().getFields().get(0)).getOptions();

    assertTrue(options != null);
    assertTrue(options.get(0).getValue().equals("testDatasource"));
  }
}
