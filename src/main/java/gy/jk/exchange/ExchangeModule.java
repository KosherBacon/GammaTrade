package gy.jk.exchange;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gy.jk.exchange.Annotations.*;
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

  private static final Config APPLICATION_CONFIG = ConfigFactory.load("application.conf");
  private static final Config EXCHANGE_CONFIG = ConfigFactory.load("exchanges.conf");

  @Override
  protected void configure() {


    switch (APPLICATION_CONFIG.getString("exchange")) {
      case "gdax":
        Exchange gdaxExchange = ExchangeFactory.INSTANCE.createExchange(GDAXExchange.class.getName());
        ExchangeSpecification gdaxExchangeSpec = gdaxExchange.getDefaultExchangeSpecification();

        gdaxExchangeSpec.setApiKey(EXCHANGE_CONFIG.getString("gdax.key"));
        gdaxExchangeSpec.setSecretKey(EXCHANGE_CONFIG.getString("gdax.secret"));
        gdaxExchangeSpec.setExchangeSpecificParametersItem("passphrase",
            EXCHANGE_CONFIG.getString("gdax.passphrase"));
        gdaxExchange.applySpecification(gdaxExchangeSpec);

        StreamingExchange streamingExchange = StreamingExchangeFactory.INSTANCE
            .createExchange(info.bitrich.xchangestream.gdax.GDAXStreamingExchange.class.getName());

        bind(TradeService.class).toInstance(gdaxExchange.getTradeService());
        bind(AccountService.class).toInstance(gdaxExchange.getAccountService());
        bind(StreamingExchange.class).toInstance(streamingExchange);
        bind(TradingApi.class).to(GdaxTradingEngine.class);
        break;
    }

    /*
     * Bindings for GdaxTradingEngine.
     */

    bindConstant().annotatedWith(ExchangeConnectionTimeout.class).to(3000L);
    bindConstant().annotatedWith(LiveTrading.class).to(false);
  }
}
