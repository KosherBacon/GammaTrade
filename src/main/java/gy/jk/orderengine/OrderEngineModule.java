package gy.jk.orderengine;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class OrderEngineModule extends AbstractModule {

  private static final Config TRADE_CONFIG = ConfigFactory.load("application.conf");

  @Override
  protected void configure() {
    if (TRADE_CONFIG.getBoolean("trading.limitOrders")) {
      bind(OrderEngine.class).to(LimitOrderEngine.class);
    } else {
      bind(OrderEngine.class).to(MarketOrderEngine.class);
    }
  }
}
