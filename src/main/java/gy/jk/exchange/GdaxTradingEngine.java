package gy.jk.exchange;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import gy.jk.email.Emailer;
import gy.jk.exchange.Annotations.LiveTrading;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;

import java.math.BigDecimal;

public class GdaxTradingEngine implements TradingApi {

  private final ListeningExecutorService executorService;
  private final TradeService tradeService;
  private final AccountService accountService;
  private final boolean liveTrading;
  private final Emailer emailer;

  @Inject
  GdaxTradingEngine(ListeningExecutorService executorService, TradeService tradeService,
      AccountService accountService, @LiveTrading boolean liveTrading, Emailer emailer) {
    this.executorService = executorService;
    this.tradeService = tradeService;
    this.accountService = accountService;
    this.liveTrading = liveTrading;
    this.emailer = emailer;
  }

  @Override
  public ListenableFuture<String> createOrder(OrderType orderType, CurrencyPair currencyPair,
      BigDecimal amount, BigDecimal price) {
    LimitOrder limitOrder = new LimitOrder.Builder(orderType, currencyPair)
        .limitPrice(price)
        .cumulativeAmount(amount)
        .build();
    return executorService.submit(() -> tradeService.placeLimitOrder(limitOrder));
  }

  @Override
  public ListenableFuture<OpenOrders> getOpenOrders(CurrencyPair currencyPair) {
    DefaultOpenOrdersParamCurrencyPair orderParams =
        new DefaultOpenOrdersParamCurrencyPair(currencyPair);
    return executorService.submit(() -> tradeService.getOpenOrders(orderParams));
  }

  @Override
  public ListenableFuture<String> createMarketOrder(OrderType orderType, CurrencyPair currencyPair,
      BigDecimal amount) {
    if (!liveTrading) {
      return Futures.immediateFuture("not-live-trade-id");
    }
    MarketOrder marketOrder = new MarketOrder(orderType, amount, currencyPair);
    return executorService.submit(() -> tradeService.placeMarketOrder(marketOrder));
  }

  @Override
  public ListenableFuture<BigDecimal> getAvailableBalance(Currency currency) {
    return executorService.submit(() ->
        accountService.getAccountInfo().getWallet().getBalance(currency).getAvailable());
  }

  @Override
  public String getMarketName() {
    return "GDAX";
  }

  @Override
  public void haltTrading() {
    emailer.sendErrorEmail();
  }

  @Override
  public void resumeTrading() {
  }
}
