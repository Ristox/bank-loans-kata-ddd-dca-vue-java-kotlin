import SsnValidationResult from "../models/SsnValidationResult";
import ValidationLimits from "../models/ValidationLimits";
import LoanRequest from "../models/LoanRequest";
import LoanEligibilityResult from "../models/LoanEligibilityResult";

const SERVER_URL = '/api'
const LOANS_VALIDATION_URL = '/loans/validation'
const LOANS_ELIGIBILITY_URL = '/loans/eligibility'

export const COMPLETE_SSN_LENGTH = 11

class Server {

    private lastRequestedSsn?: number = undefined
    private lastValidationResult?: SsnValidationResult = undefined

    constructor() {}

    async validateSocialSecurityNumber(ssn: number): Promise<SsnValidationResult | undefined> {
        const isCompleteLength = ssn.toString().length == COMPLETE_SSN_LENGTH;
        const isFirstValidationRequest = !this.lastRequestedSsn;
        const ssnHasChanged = this.lastRequestedSsn && ssn != this.lastRequestedSsn

        if (isCompleteLength && (isFirstValidationRequest || ssnHasChanged)) {
            this.lastRequestedSsn = ssn

            const response = await fetch(`${SERVER_URL}${LOANS_VALIDATION_URL}/ssn?value=${ssn}`)
            if (response.status >= 500) {
                throw new Error(response.statusText);
            }
            this.lastValidationResult = await response.json()
        }

        return this.lastValidationResult
    }

    async loadValidationLimits(): Promise<ValidationLimits> {
        const response = await fetch(`${SERVER_URL}${LOANS_VALIDATION_URL}/limits`);
        if (response.status >= 500) {
            throw new Error(response.statusText);
        }
        return await response.json();
    }

    async calculateEligibilityFor(loanRequest: LoanRequest): Promise<LoanEligibilityResult> {
       const response = await fetch(
            `${SERVER_URL}${LOANS_ELIGIBILITY_URL}`,
            {
                method: 'POST',
                headers: {
                  'Accept': 'application/json',
                  'Content-Type': 'application/json'
                },
                body: JSON.stringify(loanRequest)
            }
        )
        console.log(response.type)
        if (response.status >= 500) {
            throw new Error(response.statusText);
        }
        return response.json()
    }
}

export default new Server()
