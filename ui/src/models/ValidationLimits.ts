export default class ValidationLimits {

  constructor(
    public minimumLoanAmount: number,
    public maximumLoanAmount: number,
    public minimumLoanPeriodMonths: number,
    public maximumLoanPeriodMonths: number
  ) {}
}
