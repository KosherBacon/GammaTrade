# GammaTrade

Automated trading bot for cryptocurrencies.

## Requirements

- [Bazel](https://bazel.build "Bazel")

## Strategy

Build strategies using [ta4j](https://github.com/ta4j/ta4j "ta4j"). Indicators are provided [here](https://github.com/ta4j/ta4j/tree/master/ta4j-core/src/main/java/org/ta4j/core/indicators "Indicators").

There are many examples of strategies in the strategies package.

## Backtest

Backtest by running the backtest binary. From a command line, this can be done with `bazel run :Backtest` from the root directory. Or by running the Backtest build configuration in IntelliJ.

To select the strategy in a backtest, change the `strategy` in `backtest.conf` in the `resources` package. Options will match those in `BacktestModule`.

The data set used by the backtest is 1 million trades from Kraken on USD/BTC.

