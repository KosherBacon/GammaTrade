package gy.jk;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import gy.jk.datarecorder.DataRecorderModule;
import gy.jk.datarecorder.TradeReceiver;
import gy.jk.email.EmailModule;
import gy.jk.exchange.ExchangeConnector;
import gy.jk.exchange.ExchangeModule;
import gy.jk.tick.TickModule;
import gy.jk.trade.TradeModule;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jkahn on 11/15/17.
 */
public class GammaTrade {

  private final ExchangeConnector exchangeConnector;

  @Inject
  GammaTrade(ScheduledExecutorService executorService, ExchangeConnector exchangeConnector) {
    this.exchangeConnector = exchangeConnector;

    // Verify that we still have a connection to the exchange, do so every 10 seconds.
    executorService.scheduleAtFixedRate(
        exchangeConnector::verifyConnection, 10000, 10000, TimeUnit.MILLISECONDS);
  }

  public void startGammaTrade() {
    exchangeConnector.connectAndSubscribeAll();
    Runtime.getRuntime().addShutdownHook(new Thread(exchangeConnector::disconnectAll));
  }

  public static void main(String args[]) {

    Injector injector = Guice.createInjector(
        new BackendModule(),
        new ExchangeModule(),
        new DataRecorderModule(),
        new TickModule(),
        new TradeModule(),
        new EmailModule());
    GammaTrade connector = injector.getInstance(GammaTrade.class);
    connector.startGammaTrade();
  }
}
