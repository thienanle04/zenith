# The lint check uses Detekt 2 and splits the book

Kotlin 2.3 is the compiler. Detekt 1.23.8 cannot read that metadata and reports false findings, and the Detekt 2 line is still an alpha. The check uses the current Detekt 2 alpha. The stable 1.23 line was the alternative, and it misreads this compiler.

Detekt’s default cap is 11 functions on a file and on an object. `PortfolioBook` had five public functions and about forty-five private ones. Raising the cap to fit that object would leave the rule with nothing to catch. Excluding the file would leave the pile-up in place. The five public functions stay on `PortfolioBook`. The private helpers are `internal` functions in `PortfolioRecords`, `PortfolioValues`, `PortfolioSections`, `PortfolioRowOrder`, `PortfolioAccounts`, and `PortfolioHoldings`, each file at or under the cap. Callers still use `PortfolioBook`.
