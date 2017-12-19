package gy.jk.trade;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gy.jk.strategy.*;
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
    }
    bind(BigDecimal.class).annotatedWith(MaximumOrderSize.class).toInstance(MAXIMUM_ORDER_SIZE);
    bind(CurrencyPair.class).toInstance(CurrencyPair.BTC_USD);
    bind(TimeSeries.class).toInstance(GDAX_TIME_SERIES);
  }
}
