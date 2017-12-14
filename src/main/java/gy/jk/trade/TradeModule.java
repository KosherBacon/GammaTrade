package gy.jk.trade;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gy.jk.strategy.PrimaryStrategy;
import gy.jk.strategy.RSIStrategy;
import gy.jk.strategy.StrategyBuilder;
import gy.jk.trade.Annotations.MaximumOrderSize;
import gy.jk.trade.Annotations.TradeStrategy;
import org.knowm.xchange.currency.CurrencyPair;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import java.math.BigDecimal;

public class TradeModule extends AbstractModule {

  private static final Config TRADE_CONFIG = ConfigFactory.load("application.conf");

  private static final TimeSeries GDAX_TIME_SERIES = new BaseTimeSeries();
  private static final BigDecimal MAXIMUM_ORDER_SIZE = BigDecimal.ONE;

  @Override
  protected void configure() {
    switch (TRADE_CONFIG.getString("strategy")) {
      case "primary":
        bind(StrategyBuilder.class).annotatedWith(TradeStrategy.class).to(PrimaryStrategy.class);
        break;
      case "rsi":
        bind(StrategyBuilder.class).annotatedWith(TradeStrategy.class).to(RSIStrategy.class);
        break;
    }
    bind(BigDecimal.class).annotatedWith(MaximumOrderSize.class).toInstance(MAXIMUM_ORDER_SIZE);
    bind(CurrencyPair.class).toInstance(CurrencyPair.BTC_USD);
    bind(TimeSeries.class).toInstance(GDAX_TIME_SERIES);
  }
}
