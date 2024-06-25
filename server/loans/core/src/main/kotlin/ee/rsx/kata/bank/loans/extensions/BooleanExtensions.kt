package ee.rsx.kata.bank.loans.extensions

 fun <R> Boolean.ifTrue(block: () -> R) = if (this) block() else null
