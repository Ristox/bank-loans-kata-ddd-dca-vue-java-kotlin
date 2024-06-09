export default class SsnValidationResult {
  constructor(public ssn: String, public  status:ValidationStatus ) {}
};

export enum ValidationStatus {
  OK = 'OK',
  INVALID = 'INVALID'
}
