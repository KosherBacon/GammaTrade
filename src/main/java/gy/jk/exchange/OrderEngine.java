package gy.jk.exchange;

import com.google.inject.Inject;
import gy.jk.exchange.Annotations.LiveTrading;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.service.trade.TradeService;

import java.io.IOException;
import java.util.Optional;

@Deprecated
public class OrderEngine {

  private final TradeService gdaxTradeService;
  private final boolean liveTrading;

  @Inject
  OrderEngine(TradeService gdaxTradeService,
      @LiveTrading boolean liveTrading) {
    this.gdaxTradeService = gdaxTradeService;
    this.liveTrading = liveTrading;
  }

  public Optional<String> gdaxMarketBuyOrder() {
    if (!liveTrading) {
      // TODO - Log message.
      return Optional.empty();
    }
    MarketOrder marketOrder = new MarketOrder.Builder(OrderType.BID, CurrencyPair.BTC_USD)
        //.tradableAmount()
        .build();
    try {
      return Optional.of(gdaxTradeService.placeMarketOrder(marketOrder));
    } catch (IOException e) {
      // TODO - Log error message.
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public Optional<String> gdaxMarketSellOrder() {
    if (!liveTrading) {
      // TODO - Log message.
      return Optional.empty();
    }
    MarketOrder marketOrder = new MarketOrder.Builder(OrderType.ASK, CurrencyPair.BTC_USD)
        //.tradableAmount()
        .build();
    try {
      return Optional.of(gdaxTradeService.placeMarketOrder(marketOrder));
    } catch (IOException e) {
      // TODO - Log error message.
      e.printStackTrace();
    }
    return Optional.empty();
  }

}
