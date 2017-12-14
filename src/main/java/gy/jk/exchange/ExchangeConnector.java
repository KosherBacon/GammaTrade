package gy.jk.exchange;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.google.inject.Inject;
import gy.jk.datarecorder.TradeReceiver;
import gy.jk.exchange.Annotations.ExchangeConnectionTimeout;
import gy.jk.exchange.Annotations.StreamingExchangeList;
import gy.jk.proto.Shared;
import gy.jk.proto.Shared.Trade;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExchangeConnector {

  private static final Logger LOG = LoggerFactory.getLogger(ExchangeConnector.class);

  private final Table<StreamingExchange, CurrencyPair, Disposable> disposableTable =
      HashBasedTable.create();

  private final List<StreamingExchange> exchanges;
  private final long exchangeConnectionTimeout;
  private final TradeReceiver tradeReceiver;

  @Inject
  ExchangeConnector(
      @StreamingExchangeList List<StreamingExchange> exchanges,
      @ExchangeConnectionTimeout long exchangeConnectionTimeout,
      TradeReceiver tradeReceiver) {
    this.exchanges = exchanges;
    this.exchangeConnectionTimeout = exchangeConnectionTimeout;
    this.tradeReceiver = tradeReceiver;
  }

  public void connectAndSubscribeAll() throws UncheckedTimeoutException {
    exchanges.forEach(exchange -> {
      Throwable connect =
          exchange.connect().blockingGet(exchangeConnectionTimeout, TimeUnit.MILLISECONDS);
      if (connect != null) {
        throw new UncheckedTimeoutException(connect);
      }
      StreamingMarketDataService marketDataService = exchange.getStreamingMarketDataService();
      subscribeFeed(exchange, marketDataService);
    });
  }

  private void subscribeFeed(StreamingExchange exchange,
      StreamingMarketDataService dataService) {
    LOG.info("Initializing: {}", dataService.getClass().getName());
    Disposable disposable = dataService.getTrades(CurrencyPair.BTC_USD)
        .subscribe(tradeFromFeed -> {
          try {
            Trade trade = Trade.newBuilder()
                .setExchange(Shared.Exchange.newBuilder().setName(Shared.Exchange.Name.GDAX))
                .setPrice(tradeFromFeed.getPrice().doubleValue())
                .setSize(tradeFromFeed.getOriginalAmount().doubleValue())
                .setCurrencyPair(Trade.CurrencyPair.BTC_USD)
                .setId(tradeFromFeed.getId())
                .build();
            tradeReceiver.newTrade(trade);
          } catch (NullPointerException e) {
            LOG.error("Received data that shouldn't be NULL from exchange.", e);
          }
    });
    disposableTable.put(exchange, CurrencyPair.BTC_USD, disposable);
  }

  private void verifyConnection() {
    disposableTable.values().forEach(disposable -> {
      if (disposable.isDisposed()) {
        // Connection not open, PANIC.
        // Send email.
        LOG.error("Exchange disconnected unexpectedly!");
      }
    });
  }

  public void disconnectAll() {
    disposableTable.values().forEach(Disposable::dispose);
    exchanges.forEach(StreamingExchange::disconnect);
  }

}
