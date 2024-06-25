package ee.rsx.kata.bank.loans.extensions

 fun <R> Boolean.ifTrue(applyOperation: () -> R) = if (this) applyOperation() else null
