export default class LoanRequest {

    constructor(
        public ssn: string,
        public loanAmount: number,
        public loanPeriodMonths: number
    ) {}
}
