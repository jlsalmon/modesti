package cern.modesti.schema.options;

import cern.modesti.schema.Schema;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.field.Field;
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
    category.setFields(Collections.singletonList(new OptionsField("optionList")));
    schema.setCategories(Collections.singletonList(category));

    Query q = mock(Query.class);
    when(entityManager.createNativeQuery(contains("OPTION_LIST"))).thenReturn(q);
    when(q.getSingleResult()).thenReturn("a,b,c", (Object) null);

    optionService.injectOptions(schema);

    List<String> options = (List<String>) ((OptionsField) schema.getCategories().iterator().next().getFields().get(0)).getOptions();

    assertTrue(options != null);
    assertTrue(options.get(0).equals("a"));
    assertTrue(options.get(1).equals("b"));
    assertTrue(options.get(2).equals("c"));
  }

  @Test
  public void optionsWithMeanings() {
    Schema schema = new Schema();
    Category category = new Category("testCategory");
    category.setFields(Collections.singletonList(new OptionsField("optionsWithMeanings")));
    schema.setCategories(Collections.singletonList(category));

    Query q = mock(Query.class);
    when(entityManager.createNativeQuery(contains("OPTIONS_WITH_MEANINGS"))).thenReturn(q);
    when(q.getSingleResult()).thenReturn("a,b,c", "a desc", "b desc", "c desc");

    optionService.injectOptions(schema);

    List<String> options = (List<String>) ((OptionsField) schema.getCategories().iterator().next().getFields().get(0)).getOptions();

    assertTrue(options != null);
    assertTrue(options.get(0).equals("a: a desc"));
    assertTrue(options.get(1).equals("b: b desc"));
    assertTrue(options.get(2).equals("c: c desc"));
  }

  @Test
  public void optionRange() {
    Schema schema = new Schema();
    Category category = new Category("testCategory");
    category.setFields(Collections.singletonList(new OptionsField("optionRange")));
    schema.setCategories(Collections.singletonList(category));

    Query q = mock(Query.class);
    when(entityManager.createNativeQuery(contains("OPTION_RANGE"))).thenReturn(q);
    when(q.getSingleResult()).thenReturn("-2:2", (Object) null);

    optionService.injectOptions(schema);

    List<String> options = (List<String>) ((OptionsField) schema.getCategories().iterator().next().getFields().get(0)).getOptions();

    assertTrue(options != null);
    assertTrue(options.get(0).equals("-2"));
    assertTrue(options.get(1).equals("-1"));
    assertTrue(options.get(2).equals("0"));
    assertTrue(options.get(3).equals("1"));
    assertTrue(options.get(4).equals("2"));
  }

  @Test
  public void pointTypes() {
    Schema schema = new Schema();
    Datasource datasource = new Datasource("testDatasource");
    datasource.setName_en(datasource.getId());
    datasource.setFields(Collections.singletonList(new OptionsField("pointType")));
    schema.setDatasources(Collections.singletonList(datasource));

    optionService.injectOptions(schema);

    List<String> options = (List<String>) ((OptionsField) schema.getDatasources().iterator().next().getFields().get(0)).getOptions();

    assertTrue(options != null);
    assertTrue(options.get(0).equals("testDatasource"));
  }
}
