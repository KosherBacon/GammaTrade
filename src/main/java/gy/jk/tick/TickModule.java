package gy.jk.tick;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gy.jk.tick.Annotations.TickLengthMillis;

public class TickModule extends AbstractModule {

  private static final Config TRADE_CONFIG = ConfigFactory.load("application.conf");

  @Override
  protected void configure() {
    bindConstant().annotatedWith(TickLengthMillis.class).to(TRADE_CONFIG.getLong("tickLength"));
  }
}
