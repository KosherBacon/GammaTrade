package gy.jk.exchange;

import com.google.common.util.concurrent.ListenableFuture;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.OpenOrders;

import java.math.BigDecimal;

public interface TradingApi {

  /**
   * Create an order and return a future for its order id.
   *
   * @param orderType BID = Buy, ASK = Sell
   * @param currencyPair The currency pair being traded.
   * @param amount How much the order is for.
   * @param price What price to buy or sell at.
   * @return ListenableFuture for the order id.
   */
  ListenableFuture<String> createLimitOrder(OrderType orderType, CurrencyPair currencyPair, BigDecimal
      amount, BigDecimal price);

  ListenableFuture<OpenOrders> getOpenOrders(CurrencyPair currencyPair);

  ListenableFuture<String> createMarketOrder(OrderType orderType, CurrencyPair currencyPair,
      BigDecimal amount);

  ListenableFuture<BigDecimal> getAvailableBalance(Currency currency);

  ListenableFuture<BigDecimal> getBestPriceFromOrderBook(OrderType orderType, CurrencyPair
      currencyPair);

  ListenableFuture<Boolean> cancelAllOrders();

  /**
   * Gets the market name.
   * @return The market name.
   */
  String getMarketName();

  /**
   * When called, the system will halt any future trading.
   */
  void haltTrading();

  /**
   * When called, the system will resume any future trading.
   */
  void resumeTrading();
}
