package gy.jk.backtest;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import gy.jk.BackendModule;
import gy.jk.proto.Shared.BacktestResult;
import gy.jk.strategy.StrategyBuilder;
import gy.jk.tick.TickModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TimeSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.*;

import java.time.Duration;
import java.time.Instant;

/**
 * @author Joshua Kahn
 */
public class Backtester {

  private static final Logger LOG = LoggerFactory.getLogger(Backtester.class);

  private final StrategyBuilder strategyBuilder;
  private final BacktestDataLoader backtestDataLoader;

  @Inject
  Backtester(StrategyBuilder strategyBuilder,
      BacktestDataLoader backtestDataLoader) {
    this.strategyBuilder = strategyBuilder;
    this.backtestDataLoader = backtestDataLoader;
  }

  public ListenableFuture<BacktestResult> runBacktest() {
    ListenableFuture<TimeSeries> krakenFuture = backtestDataLoader.getKrakenBTCUSD();
    return Futures.transformAsync(krakenFuture, timeSeries -> {
      Instant start = Instant.now();
      LOG.info("Beginning backtest.");

      TimeSeriesManager timeSeriesManager = new TimeSeriesManager(timeSeries);
      TradingRecord tradingRecord = timeSeriesManager.run(
          strategyBuilder.buildStrategy(timeSeries));
      ListenableFuture<BacktestResult> result = Futures.immediateFuture(BacktestResult.newBuilder()
          .setTotalProfit(new TotalProfitCriterion().calculate(timeSeries, tradingRecord))
          .setNumberOfTicks(new NumberOfTicksCriterion().calculate(timeSeries, tradingRecord))
          .setAverageProfit(new AverageProfitCriterion().calculate(timeSeries, tradingRecord))
          .setNumberOfTrades(new NumberOfTradesCriterion().calculate(timeSeries, tradingRecord))
          .setProfitableTradesRatio(
              new AverageProfitableTradesCriterion().calculate(timeSeries, tradingRecord))
          .setMaximumDrawdown(new MaximumDrawdownCriterion().calculate(timeSeries, tradingRecord))
          .setRewardRiskRatio(new RewardRiskRatioCriterion().calculate(timeSeries, tradingRecord))
          .setBuyAndHold(new BuyAndHoldCriterion().calculate(timeSeries, tradingRecord))
          .build());

      Instant end = Instant.now();
      LOG.info("Finished backtest, took {} seconds.",
          Duration.between(start, end).toMillis() / 1000L);

      return result;
    });
  }

  /*
   * Entry point for the backtester.
   */
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new BacktestModule(),
        new BackendModule(), new TickModule());
    Backtester backtester = injector.getInstance(Backtester.class);
    ListenableFuture<BacktestResult> result = backtester.runBacktest();
    Futures.addCallback(result, new FutureCallback<BacktestResult>() {
      public void onSuccess(BacktestResult result) {
        System.out.println(result);
        System.exit(0);
      }
      public void onFailure(Throwable t) {
        t.printStackTrace();
        System.exit(1);
      }
    });
  }

}
