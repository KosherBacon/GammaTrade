package gy.jk.exchange;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gy.jk.exchange.Annotations.*;
import info.bitrich.xchangestream.bitstamp.BitstampStreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.gdax.GDAXExchange;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.TradeService;

import java.util.List;

public class ExchangeModule extends AbstractModule {

  private static final Config EXCHANGE_CONFIG = ConfigFactory.load("exchanges.conf");
  private static final Exchange GDAX_EXCHANGE = ExchangeFactory.INSTANCE.createExchange(GDAXExchange.class.getName());
  private static final ExchangeSpecification GDAX_EXCHANGE_SPECIFICATION = GDAX_EXCHANGE.getDefaultExchangeSpecification();;

  @Override
  protected void configure() {
    GDAX_EXCHANGE_SPECIFICATION.setApiKey(EXCHANGE_CONFIG.getString("gdax.key"));
    GDAX_EXCHANGE_SPECIFICATION.setSecretKey(EXCHANGE_CONFIG.getString("gdax.secret"));
    GDAX_EXCHANGE_SPECIFICATION.setExchangeSpecificParametersItem("passphrase",
        EXCHANGE_CONFIG.getString("gdax.passphrase"));
    GDAX_EXCHANGE.applySpecification(GDAX_EXCHANGE_SPECIFICATION);

    bind(TradeService.class).toInstance(GDAX_EXCHANGE.getTradeService());
    bind(AccountService.class).toInstance(GDAX_EXCHANGE.getAccountService());

    // TODO - Read a configuration to determine which exchange to use.

    /*
     * Bindings for GdaxTradingEngine.
     */
    bind(TradingApi.class).to(GdaxTradingEngine.class);

    bindConstant().annotatedWith(ExchangeConnectionTimeout.class).to(3000L);
    bindConstant().annotatedWith(LiveTrading.class).to(false);
  }

  /**
   * Create a streaming exchange so that we can get live data from GDAX.
   */
  @Singleton
  @Provides
  @GDAXStreamingExchange
  @SuppressWarnings("unused")
  public StreamingExchange provideGDAXStreamingExchange() {
    return StreamingExchangeFactory.INSTANCE
        .createExchange(info.bitrich.xchangestream.gdax.GDAXStreamingExchange.class.getName());
  }

  @Singleton
  @Provides
  @BitstampExchange
  @SuppressWarnings("unused")
  public StreamingExchange provideBitstampExchange() {
    return StreamingExchangeFactory.INSTANCE
        .createExchange(BitstampStreamingExchange.class.getName());
  }

  @Singleton
  @Provides
  @StreamingExchangeList
  @SuppressWarnings("unused")
  public List<StreamingExchange> provideStreamingExchangeList(
      @GDAXStreamingExchange StreamingExchange gdax) {
    return new ImmutableList.Builder<StreamingExchange>()
        .add(gdax)
        //.add(bitstamp)
        .build();
  }
}
