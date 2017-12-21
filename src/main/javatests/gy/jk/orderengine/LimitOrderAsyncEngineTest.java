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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
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
    /*
     * Procedure:
     * - Check open orders
     * - Verify order of same type
     * - Cancel orders
     * - Place new order
     */
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

}
