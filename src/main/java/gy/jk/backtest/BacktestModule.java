package gy.jk.backtest;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gy.jk.strategy.PrimaryStrategy;
import gy.jk.strategy.RSIStrategy;
import gy.jk.strategy.StrategyBuilder;

public class BacktestModule extends AbstractModule {

  private static final Config BACKTEST_CONFIG = ConfigFactory.load("backtest.conf");

  @Override
  protected void configure() {
    switch (BACKTEST_CONFIG.getString("strategy")) {
      case "primary":
        bind(StrategyBuilder.class).to(PrimaryStrategy.class);
        break;
      case "rsi":
        bind(StrategyBuilder.class).to(RSIStrategy.class);
        break;
    }
  }
}
