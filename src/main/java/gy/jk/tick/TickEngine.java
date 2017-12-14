package gy.jk.tick;

import com.google.inject.Inject;
import gy.jk.proto.Shared.Trade;
import gy.jk.tick.Annotations.TickLengthMillis;
import gy.jk.trade.Trader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BaseTick;
import org.ta4j.core.Tick;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TickEngine {

  private static final Logger LOG = LoggerFactory.getLogger(TickEngine.class);

  private final long tickLengthMillis;
  private final ScheduledExecutorService executorService;
  private final Trader trader;

  private Tick currentTick;
  private ZonedDateTime lastTickEnd;

  @Inject
  TickEngine(@TickLengthMillis long tickLengthMillis, ScheduledExecutorService executorService,
      Trader trader) {
    this.tickLengthMillis = tickLengthMillis;
    this.executorService = executorService;
    this.trader = trader;

    createNewTick();

    executorService.scheduleAtFixedRate(this::fireTick,
        tickLengthMillis, tickLengthMillis, TimeUnit.MILLISECONDS);

    LOG.info("Trader initialized.");
  }

  public synchronized void receiveTrade(Trade trade) {
    if (!trade.hasSize() || !trade.hasPrice() || !trade.hasExchange() || !trade.hasCurrencyPair()) {
      LOG.warn("Trade missing necessary information.\n{}", trade.toString());
      throw new IllegalArgumentException("Trade is missing necessary information.");
    }
    double volume = trade.getSize();
    double price = trade.getPrice();
    currentTick.addTrade(volume, price);
  }

  private synchronized void fireTick() {
    LOG.info("Last Tick Close: {} | Volume: {}",
        currentTick.getClosePrice().toDouble(), currentTick.getVolume());
    Tick tick = currentTick;
    executorService.submit(() -> trader.receiveAndProcessTick(tick));
    createNewTick();
  }

  private void createNewTick() {
    if (lastTickEnd == null) {
      lastTickEnd = ZonedDateTime.now();
    }
    lastTickEnd = lastTickEnd.plus(tickLengthMillis, ChronoUnit.MILLIS);
    currentTick = new BaseTick(Duration.ofMillis(tickLengthMillis), lastTickEnd);
  }

}
