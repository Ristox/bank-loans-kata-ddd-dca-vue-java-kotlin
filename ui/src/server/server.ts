import SsnValidationResult from "../models/SsnValidationResult";

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
        let isCompleteLength = ssn.toString().length == COMPLETE_SSN_LENGTH;
        let isFirstValidationRequest = !this.lastRequestedSsn;
        let ssnHasChanged = this.lastRequestedSsn && ssn != this.lastRequestedSsn

        if (isCompleteLength && (isFirstValidationRequest || ssnHasChanged)) {
            this.lastRequestedSsn = ssn

            const response = await fetch(`${SERVER_URL}${LOANS_VALIDATION_URL}/ssn?value=${ssn}`)
            this.lastValidationResult = await response.json()
        }

        return this.lastValidationResult
    }
}

export default new Server()
