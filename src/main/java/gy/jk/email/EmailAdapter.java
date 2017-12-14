package gy.jk.email;

import com.google.inject.Inject;
import gy.jk.email.Annotations.ErrorEmail;
import org.simplejavamail.email.Email;
import org.simplejavamail.mailer.Mailer;

public class EmailAdapter implements Emailer {

  private final Mailer mailer;
  private final Email errorEmail;

  @Inject
  EmailAdapter(Mailer mailer, @ErrorEmail Email errorEmail) {
    this.mailer = mailer;
    this.errorEmail = errorEmail;
  }

  @Override
  public void sendErrorEmail() {
    mailer.sendMail(errorEmail);
  }

}
