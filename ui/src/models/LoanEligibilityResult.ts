export default class LoanEligibilityResult {

    constructor(
        public result: LoanEligibilityStatus,
        public errors: Array<String> | null,
        public ssn: string,
        public loanAmount: number,
        public loanPeriodMonths: number,
        public eligibleLoanAmount: number | null,
        public eligibleLoanPeriod: number | null
    ) {}
}

export enum LoanEligibilityStatus {
    APPROVED = 'APPROVED',
    DENIED = 'DENIED',
    INVALID = 'INVALID'
}
