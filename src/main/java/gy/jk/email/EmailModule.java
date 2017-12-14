package gy.jk.email;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gy.jk.email.Annotations.ErrorEmail;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.ServerConfig;

public class EmailModule extends AbstractModule {

  private static final Config EMAIL_CONFIG = ConfigFactory.load("email.conf");

  private static final Email ERROR_EMAIL = new EmailBuilder()
      .from("GammaTrade", "noreply@jk.gy")
      .to("Joshua Kahn", "josh@jk.gy")
      .subject("URGENT - Trade error occurred!")
      .text("Error occurred while trading, bot shutting down. Please investigate.")
      .build();

  private static final Mailer MAILER = new Mailer(
      new ServerConfig(EMAIL_CONFIG.getString("outbound.host"),
          EMAIL_CONFIG.getInt("outbound.port"),
          EMAIL_CONFIG.getString("outbound.username"),
          EMAIL_CONFIG.getString("outbound.password")));

  @Override
  protected void configure() {
    bind(Mailer.class).toInstance(MAILER);

    if (EMAIL_CONFIG.getBoolean("useEmail")) {
      bind(Emailer.class).to(EmailAdapter.class);
      bind(Email.class).annotatedWith(ErrorEmail.class).toInstance(ERROR_EMAIL);
      bind(Mailer.class).toInstance(MAILER);
    } else {
      bind(Emailer.class).to(FakeEmailAdapter.class);
    }
  }
}
