package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.database.User;

public class ResponseUser extends RestResponseBody {

  private User user;

  /** @return the user */
  public User getUser() {

    return user;
  }

  /** @param user the user to set */
  public void setUser(User user) {

    this.user = user;
  }
}
