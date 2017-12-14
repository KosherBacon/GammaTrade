package gy.jk;

import com.google.inject.Guice;
import com.google.inject.Injector;
import gy.jk.datarecorder.DataRecorderModule;
import gy.jk.datarecorder.TradeReceiver;
import gy.jk.email.EmailModule;
import gy.jk.exchange.ExchangeConnector;
import gy.jk.exchange.ExchangeModule;
import gy.jk.tick.TickModule;
import gy.jk.trade.TradeModule;

/**
 * Created by jkahn on 11/15/17.
 */
public class GammaTrade {
  public static void main(String args[]) {

    Injector injector = Guice.createInjector(
        new BackendModule(),
        new ExchangeModule(),
        new DataRecorderModule(),
        new TickModule(),
        new TradeModule(),
        new EmailModule());
    ExchangeConnector connector = injector.getInstance(ExchangeConnector.class);
    injector.getInstance(TradeReceiver.class);
    connector.connectAndSubscribeAll();

    Runtime.getRuntime().addShutdownHook(new Thread(connector::disconnectAll));
  }
}
