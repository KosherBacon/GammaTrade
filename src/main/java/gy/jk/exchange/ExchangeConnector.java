package gy.jk.exchange;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.google.inject.Inject;
import gy.jk.datarecorder.TradeReceiver;
import gy.jk.email.Emailer;
import gy.jk.exchange.Annotations.ExchangeConnectionTimeout;
import gy.jk.proto.Shared;
import gy.jk.proto.Shared.Trade;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.disposables.Disposable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.concurrent.TimeUnit;

public class ExchangeConnector {

  private static final Logger LOG = LogManager.getLogger();

  private final Table<StreamingExchange, CurrencyPair, Disposable> disposableTable =
      HashBasedTable.create();

  private final StreamingExchange exchange;
  private final long exchangeConnectionTimeout;
  private final TradeReceiver tradeReceiver;
  private final CurrencyPair currencyPair;
  private final Emailer emailer;

  @Inject
  ExchangeConnector(StreamingExchange exchange,
      @ExchangeConnectionTimeout long exchangeConnectionTimeout,
      TradeReceiver tradeReceiver,
      CurrencyPair currencyPair,
      Emailer emailer) {
    this.exchange = exchange;
    this.exchangeConnectionTimeout = exchangeConnectionTimeout;
    this.tradeReceiver = tradeReceiver;
    this.currencyPair = currencyPair;
    this.emailer = emailer;
  }

  public void connectAndSubscribeAll() throws UncheckedTimeoutException {
    Throwable connect =
        exchange.connect().blockingGet(exchangeConnectionTimeout, TimeUnit.MILLISECONDS);
    if (connect != null) {
      throw new UncheckedTimeoutException(connect);
    }
    StreamingMarketDataService marketDataService = exchange.getStreamingMarketDataService();
    subscribeFeed(exchange, marketDataService);
  }

  private void subscribeFeed(StreamingExchange exchange,
      StreamingMarketDataService dataService) {
    LOG.info("Initializing: {}", dataService.getClass().getName());
    Disposable disposable = dataService.getTrades(currencyPair)
        .subscribe(tradeFromFeed -> {
          try {
            Trade trade = Trade.newBuilder()
                .setExchange(Shared.Exchange.newBuilder().setName(Shared.Exchange.Name.GDAX))
                .setPrice(tradeFromFeed.getPrice().doubleValue())
                .setSize(tradeFromFeed.getOriginalAmount().doubleValue())
                .setCurrencyPair(Trade.CurrencyPair.ETH_BTC)
                .setId(tradeFromFeed.getId())
                .build();
            tradeReceiver.newTrade(trade);
          } catch (NullPointerException e) {
            LOG.error("Received data that shouldn't be NULL from exchange.", e);
          }
    });
    disposableTable.put(exchange, currencyPair, disposable);
  }

  public void verifyConnection() {
    disposableTable.values().forEach(disposable -> {
      if (disposable.isDisposed()) {
        // Connection not open, PANIC.
        // Send email.
        LOG.error("Exchange disconnected unexpectedly!");
        emailer.sendErrorEmail();
      }
    });
  }

  public void disconnectAll() {
    disposableTable.values().forEach(Disposable::dispose);
    exchange.disconnect();
  }

}
