package cern.modesti;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

public class PasswordCheckerTest {
  @Test
  public void testCheckPassword() {
    User testUser = new User("user", "pass");

    // Create the mock
    DataAccess mock = EasyMock.createMock(DataAccess.class);
    PasswordChecker passwordChecker = new PasswordChecker(mock);

    // Record the expected behaviour
    EasyMock.expect(mock.getUser("user")).andReturn(testUser).once();

    EasyMock.expect(mock.getUser("user")).andAnswer(new IAnswer<User>() {
      @Override
      public User answer() throws Throwable {
        String username = (String) EasyMock.getCurrentArguments()[0];

        if (username.equals("user")) {
          return new User("unknown", "unknown");
        }

        return new User("user", "pass");
      }
    });

    EasyMock.expect(mock.getUser("user")).andDelegateTo(new DataAccess() {

      @Override
      public User getUser(String username) {
        return new User("fake", "fake");
      }
    });

    EasyMock.expect(mock.getUser(EasyMock.<String> anyObject())).andReturn(testUser);

    // Switch to replay state
    EasyMock.replay(mock);

    boolean result = passwordChecker.checkPassword("user", "pass");
    // Make sure the result was correct.
    assertEquals(true, result);

    // Verify that the expected behaviour happened.
    EasyMock.verify(mock);
  }

  class User {
    public User(String username, String password) {

    }
  }

  interface DataAccess {
    public User getUser(String username);
  }

  class DataAccessImpl implements DataAccess {

    @Override
    public User getUser(String username) {
      return null;
    }

  }

  class PasswordChecker {
    public PasswordChecker(DataAccess dataAccess) {
    }

    public boolean checkPassword(String username, String password) {
      return false;
    }
  }
}
