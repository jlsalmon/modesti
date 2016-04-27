package cern.modesti.request;

import cern.modesti.test.BaseIntegrationTest;
import cern.modesti.user.MockUserService;
import cern.modesti.workflow.task.NotAuthorisedException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static cern.modesti.test.TestUtil.getDummyRequest;
import static org.junit.Assert.*;

/**
 * @author Justin Lewis Salmon
 */
@TestPropertySource(locations = "classpath:modesti-test.properties", properties = "mongodb.persistent=false", inheritLocations = false)
public class RequestServiceTest extends BaseIntegrationTest {

  @Autowired
  MockUserService userService;

  @Test
  public void requestIsCreatedInProgress() throws Exception {
    userService.login("ben");
    Request request = getDummyRequest();
    requestService.insert(request);
    assertEquals(request.getStatus(), "IN_PROGRESS");
    requestService.delete(request);
  }

  @Test
  public void requestIdIsGenerated() {
    userService.login("ben");
    Request request = getDummyRequest();
    request = requestService.insert(request);
    assertNotNull(request.getRequestId());
    requestService.delete(request);
  }

  @Test
  public void requestSaveGeneratesPointIds() {
    userService.login("ben");
    Request request = getDummyRequest();
    request = requestService.insert(request);
    request = requestService.save(request);
    assertTrue(request.getPoints().get(0).getLineNo() == 1L);
    assertTrue(request.getPoints().get(1).getLineNo() == 2L);
    requestService.delete(request);
  }

  @Test
  public void creatorIsAutomaticallySetOnInsert() {
    userService.login("ben");
    Request request = getDummyRequest();
    request = requestService.insert(request);
    assertEquals(request.getCreator(), "ben");
    requestService.delete(request);
  }

  @Test(expected = NotAuthorisedException.class)
  public void unauthorisedUserCannotInsert() {
    userService.login("joe");
    requestService.insert(getDummyRequest());
  }
}
