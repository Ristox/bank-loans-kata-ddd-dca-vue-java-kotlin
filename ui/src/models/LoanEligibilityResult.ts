export default class LoanEligibilityResult {

    constructor(
        public result: LoanEligibilityStatus,
        public errors: Array<String>,
        public ssn: string,
        public loanAmount: number,
        public loanPeriodMonths: number
    ) {}
}

export enum LoanEligibilityStatus {
    APPROVED = 'APPROVED',
    DENIED = 'DENIED',
    INVALID = 'INVALID'
}
