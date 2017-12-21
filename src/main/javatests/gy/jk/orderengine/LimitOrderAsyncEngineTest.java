package jk.gy.orderengine;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import gy.jk.exchange.TradingApi;
import gy.jk.orderengine.LimitOrderAsyncEngine;
import jk.gy.TestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LimitOrderAsyncEngineTest {

  private static final CurrencyPair CURRENCY_PAIR = CurrencyPair.BTC_USD;

  @Mock private TradingApi tradingApi;

  private OpenOrders openOrders;
  private LimitOrderAsyncEngine limitOrderAsyncEngine;

  @Before
  public void setUp() {
    limitOrderAsyncEngine = new LimitOrderAsyncEngine(TestHelper.TEST_EXECUTOR_SERVICE, tradingApi,
        CURRENCY_PAIR, false);
  }

  @Test
  public void testNoConflictingOrdersBuy() throws Exception {
    openOrders = new OpenOrders(Collections.emptyList());

    ListenableFuture<BigDecimal> price = Futures.immediateFuture(BigDecimal.ONE);
    ListenableFuture<BigDecimal> balance = Futures.immediateFuture(BigDecimal.TEN);
    Optional<String> orderId = Optional.of("not-live-trade-id");

    when(tradingApi.getOpenOrders(eq(CURRENCY_PAIR)))
        .thenReturn(Futures.immediateFuture(openOrders));
    when(tradingApi.getBestPriceFromOrderBook(OrderType.BID, CURRENCY_PAIR))
        .thenReturn(price);
    when(tradingApi.getAvailableBalance(eq(CURRENCY_PAIR.counter)))
        .thenReturn(balance);

    assertThat(limitOrderAsyncEngine.placeOrder(OrderType.BID).get())
        .isEqualTo(orderId);
  }

  @Test
  public void testRemnantOrderSameType() throws Exception {
    LimitOrder limitOrder = new LimitOrder.Builder(OrderType.BID, CURRENCY_PAIR)
        .limitPrice(BigDecimal.valueOf(0.5))
        .originalAmount(BigDecimal.TEN)
        .build();
    openOrders = new OpenOrders(Collections.singletonList(limitOrder));
    Optional<String> orderId = Optional.of("not-live-trade-id");

    when(tradingApi.getOpenOrders(eq(CURRENCY_PAIR)))
        .thenReturn(Futures.immediateFuture(openOrders));
    when(tradingApi.cancelAllOrders())
        .thenReturn(Futures.immediateFuture(Boolean.TRUE));

    ListenableFuture<BigDecimal> price = Futures.immediateFuture(BigDecimal.ONE);
    ListenableFuture<BigDecimal> balance = Futures.immediateFuture(BigDecimal.TEN);

    when(tradingApi.getBestPriceFromOrderBook(OrderType.BID, CURRENCY_PAIR))
        .thenReturn(price);
    when(tradingApi.getAvailableBalance(eq(CURRENCY_PAIR.counter)))
        .thenReturn(balance);

    assertThat(limitOrderAsyncEngine.placeOrder(OrderType.BID).get())
        .isEqualTo(orderId);
  }

  @Test
  public void testRemnantOrderSameTypeSamePrice() throws Exception {
    LimitOrder limitOrder = new LimitOrder.Builder(OrderType.BID, CURRENCY_PAIR)
        .limitPrice(BigDecimal.ONE)
        .originalAmount(BigDecimal.TEN)
        .build();
    openOrders = new OpenOrders(Collections.singletonList(limitOrder));

    when(tradingApi.getOpenOrders(eq(CURRENCY_PAIR)))
        .thenReturn(Futures.immediateFuture(openOrders));

    ListenableFuture<BigDecimal> price = Futures.immediateFuture(BigDecimal.ONE);

    when(tradingApi.getBestPriceFromOrderBook(OrderType.BID, CURRENCY_PAIR))
        .thenReturn(price);

    verify(tradingApi, never()).cancelAllOrders();
    assertThat(limitOrderAsyncEngine.placeOrder(OrderType.BID).get())
        .isEmpty();
  }

}
