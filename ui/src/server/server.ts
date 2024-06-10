import SsnValidationResult from "../models/SsnValidationResult";
import ValidationLimits from "../models/ValidationLimits";

const SERVER_URL = '/api'
const LOANS_VALIDATION_URL = '/loans/validation'

export const COMPLETE_SSN_LENGTH = 11

class Server {

    private lastRequestedSsn?: number = undefined
    private lastValidationResult?: SsnValidationResult = undefined

    constructor() {}

    async checkHealth(): Promise<any> {
        const response = await fetch(`${SERVER_URL}/health`)
        return await response.json()
    }

    async validateSocialSecurityNumber(ssn: number): Promise<SsnValidationResult | undefined> {
        const isCompleteLength = ssn.toString().length == COMPLETE_SSN_LENGTH;
        const isFirstValidationRequest = !this.lastRequestedSsn;
        const ssnHasChanged = this.lastRequestedSsn && ssn != this.lastRequestedSsn

        if (isCompleteLength && (isFirstValidationRequest || ssnHasChanged)) {
            this.lastRequestedSsn = ssn

            const response = await fetch(`${SERVER_URL}${LOANS_VALIDATION_URL}/ssn?value=${ssn}`)
            this.lastValidationResult = await response.json()
        }

        return this.lastValidationResult
    }

    async loadValidationLimits(): Promise<ValidationLimits> {
        const response = await fetch(`${SERVER_URL}${LOANS_VALIDATION_URL}/limits`);
        return await response.json()
    }
}

export default new Server()
