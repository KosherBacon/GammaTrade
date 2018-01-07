package gy.jk.backtest;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gy.jk.backtest.Annotations.ExchangeFixedFee;
import gy.jk.backtest.Annotations.ExchangePercentFee;
import gy.jk.strategy.*;
import gy.jk.tick.Annotations.TickLengthMillis;

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
      case "globalExtrema":
        bind(StrategyBuilder.class).to(GlobalExtremaStrategy.class);
        break;
      case "cciCorrection":
        bind(StrategyBuilder.class).to(CCICorrectionStrategy.class);
        break;
      case "kurt":
        bind(StrategyBuilder.class).to(KurtStrategy.class);
        break;
      case "momentum":
        bind(StrategyBuilder.class).to(MomentumStrategy.class);
        break;
      case "bollinger":
        bind(StrategyBuilder.class).to(BollingerStrategy.class);
        break;
      case "honeyBadger":
        bind(StrategyBuilder.class).to(HoneyBadgerStrategy.class);
        break;
      case "sar":
        bind(StrategyBuilder.class).to(ParabolicSARStrategy.class);
        break;
    }
    bindConstant().annotatedWith(TickLengthMillis.class)
        .to(BACKTEST_CONFIG.getLong("tickLength"));
    bindConstant().annotatedWith(ExchangePercentFee.class)
        .to(BACKTEST_CONFIG.getDouble("exchangePercentFee"));
    bindConstant().annotatedWith(ExchangeFixedFee.class)
        .to(BACKTEST_CONFIG.getDouble("exchangeFixedFee"));
  }
}
