package gy.jk.orderengine;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import gy.jk.exchange.TradingApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author Joshua Kahn
 */
public class LastOrderEngine {

  private static final Logger LOG = LogManager.getLogger();

  private final TradingApi tradingApi;
  private final CurrencyPair currencyPair;
  private final ListeningExecutorService executorService;

  @Inject
  public LastOrderEngine(TradingApi tradingApi,
      CurrencyPair currencyPair,
      ListeningExecutorService executorService) {
    this.tradingApi = tradingApi;
    this.currencyPair = currencyPair;
    this.executorService = executorService;
  }

  public ListenableFuture<OrderType> getLastTrade() {
    ListenableFuture<BigDecimal> baseBalance = tradingApi.getAvailableBalance(currencyPair.base);
    ListenableFuture<BigDecimal> counterBalance = tradingApi.getAvailableBalance(currencyPair.counter);
    ListenableFuture<List<BigDecimal>> balanceResults = Futures.allAsList(baseBalance, counterBalance);
    return Futures.transformAsync(balanceResults, results -> {
      Objects.requireNonNull(results, "BalanceResults were null!");
      BigDecimal bBalance = Objects.requireNonNull(results.get(0), "Base balance was null!");
      BigDecimal cBalance = Objects.requireNonNull(results.get(1), "Counter balance was null!");
      if (cBalance.compareTo(bBalance) < 0) {
        LOG.info("Last order was BUY!");
        return Futures.immediateFuture(OrderType.BID); // BID = Buy
      } else {
        LOG.info("Last order was SELL!");
        return Futures.immediateFuture(OrderType.ASK); // ASK = Sell
      }
    }, executorService);
  }
}
