<template>
  <div class="col-md-12">
    <div class="card card-container">
      <Form @submit="doSubmit" :validation-schema="schema">

        <div class="form-group required">
          <label for="ssn">SSN (personal code)</label>
          <Field name="ssn" type="text" class="form-control"/>
          <ErrorMessage name="ssn" class="error-feedback"/>
        </div>

        <div class="form-group required">
          <label for="loanAmount">Loan amount ({{ minimumAmount() }} - {{ maximumAmount() }} €)</label>
          <Field name="loanAmount" type="text" class="form-control"/>
          <ErrorMessage name="loanAmount" class="error-feedback"/>
        </div>

        <div class="form-group required">
          <label for="loanPeriodMonths">Loan period ({{ minimumPeriod() }} - {{ maximumPeriod() }} months)</label>
          <Field name="loanPeriodMonths" type="text" class="form-control"/>
          <ErrorMessage name="loanPeriodMonths" class="error-feedback"/>
        </div>

        <div class="form-group">
          <button class="btn btn-outline-primary btn-block" :disabled="loading">
              <span
                  v-show="loading"
                  class="spinner-border spinner-border-sm"
              ></span>
            <span>Apply for loan</span>
          </button>
        </div>

        <div class="form-group">
          <div v-if="error" class="alert alert-danger alert" role="alert">
            {{ error }}
          </div>
        </div>

        <div class="form-group">
          <div v-if="eligibilityResponse"
               class="alert" role="alert"
               v-bind:class="{
                  'alert-success': loanApproved,
                  'alert-warning': loanDenied,
                  'alert-danger': loanInvalid }">

            <div class="result-line">
              <span class="result-heading">Loan request: </span>
              <span class="result-detail highlight">{{ eligibilityResponse.result }}</span>
            </div>

            <div v-if="eligibilityResponse.errors" class="result-line">
              <div class="result-line">Errors:</div>
              <ul>
                <li v-for="error in eligibilityResponse.errors" :key="error.toString()">
                  {{ error }}
                </li>
              </ul>
            </div>

            <div class="result-line">
              <span class="result-heading">Requested amount: </span>
              <span class="result-detail">{{ eligibilityResponse.loanAmount }} €</span>
            </div>

            <div class="result-line">
              <span class="result-heading">Requested period: </span>
              <span class="result-detail">{{ eligibilityResponse.loanPeriodMonths }} months</span>
            </div>

            <div v-if="eligibilityResponse.eligibleLoanAmount != null" class="result-line">
              <hr />
              <span class="result-heading" v-if="loanApproved">Offered amount: </span>
              <span class="result-heading" v-else-if="loanDenied">Eligible amount: </span>
              <span class="result-detail highlight">{{ eligibilityResponse.eligibleLoanAmount }} €</span>
            </div>

            <div v-if="eligibilityResponse.eligibleLoanPeriod != null" class="result-line">
              <span class="result-heading">Eligible period: </span>
              <span class="result-detail highlight">{{ eligibilityResponse.eligibleLoanPeriod }} months</span>
            </div>
          </div>
        </div>

      </Form>
    </div>
  </div>
</template>


<script lang="ts">
import {ErrorMessage, Field, Form} from 'vee-validate';
import * as yup from 'yup';
import {NumberSchema, ValidationError} from 'yup';
import server, {COMPLETE_SSN_LENGTH} from "../server/server";
import {ValidationStatus} from "../models/SsnValidationResult";
import ValidationLimits from "../models/ValidationLimits";
import LoanRequest from "../models/LoanRequest";
import LoanEligibilityResult, {LoanEligibilityStatus} from "../models/LoanEligibilityResult";

export default {
  name: "Loans",
  components: {
    Form,
    Field,
    ErrorMessage,
  },
  data() {
    const schema = yup.object()
      .shape({
        ssn: validateSsnField(this),
        loanAmount: validateLoanAmountField(this),
        loanPeriodMonths: validateLoanPeriodField(this)
      });

    return {
      loading: false,
      validationLimits: new ValidationLimits(500, 1999, 6, 12) as ValidationLimits,
      error: "",
      eligibilityResponse: undefined as LoanEligibilityResult | undefined,
      schema,
    };
  },
  computed: {
    loanApproved() {
      return this.eligibilityResponse?.result == LoanEligibilityStatus.APPROVED
    },
    loanDenied() {
      return this.eligibilityResponse?.result == LoanEligibilityStatus.DENIED
    },
    loanInvalid() {
      return this.eligibilityResponse?.result == LoanEligibilityStatus.INVALID
    },
  },
  created() {
    try {
      this.loadValidationLimits()
    } catch (error) {
      this.error = `Error: unable to load configured limits, using defaults: ${JSON.stringify(this.validationLimits)}`
    }
  },
  methods: {
    async loadValidationLimits() {
      this.loading = true
      try {
        this.validationLimits = await server.loadValidationLimits()
      }
      catch (error) {
        this.error = 'Error: service unavailable (unable to acquire loan limits, will use default values)'
      }
      this.loading = false
    },
    async validateSocialSecurityNumber(value: number): Promise<boolean | ValidationError> {
      if (value.toString().length != COMPLETE_SSN_LENGTH) {
        return false
      }
      try {
        this.loading = true
        this.error = ''
        const validationResult = await server.validateSocialSecurityNumber(value)
        this.loading = false
        if (validationResult) {
          return validationResult.status == ValidationStatus.OK;
        } else {
          return true;
        }
      } catch (error) {
        this.error = 'Error: service unavailable (unable to perform validation)'
        this.loading = false
        return true
      }
    },
    getValidationLimits(): ValidationLimits {
      return this.validationLimits
    },
    minimumAmount() {
      return this.validationLimits.minimumLoanAmount
    },
    maximumAmount() {
      return this.validationLimits.maximumLoanAmount
    },
    minimumPeriod() {
      return this.validationLimits.minimumLoanPeriodMonths
    },
    maximumPeriod() {
      return this.validationLimits.maximumLoanPeriodMonths
    },
    async doSubmit(request: any) {
      this.loading = true
      try {
        const {ssn, loanAmount, loanPeriodMonths} = request
        this.eligibilityResponse = await server.calculateEligibilityFor(
            new LoanRequest(ssn, Number(loanAmount), Number(loanPeriodMonths))
        )
        this.error = ''
      } catch (error) {
        this.eligibilityResponse = undefined
        this.error = `Error: service unavailable (unable to calculate loan eligibility)`
      } finally {
        this.loading = false
      }
    },
  },
};

function validateSsnField(thisComponent: any): NumberSchema {
  return yup.number()
    .typeError('Please enter a valid number')
    .required("Please enter a personal code (SSN)")
    .test(
        'validateSocialSecurityNumber',
        'Personal code (SSN) is not valid',
        async (value) => await thisComponent.validateSocialSecurityNumber(value)
    );
}

function validateLoanAmountField(thisComponent: any): NumberSchema {
  return yup.number()
      .typeError('Please enter a valid number')
      .required("Please enter a loan amount")
      .test(
          "minimumLoanAmount",
          "Loan amount is below minimum allowed",
          (value) => {
            return value >= thisComponent.getValidationLimits().minimumLoanAmount
          }
      )
      .test(
          "maximumLoanAmount",
          "Loan amount is above maximum allowed",
          (value) => {
            return value <= thisComponent.getValidationLimits().maximumLoanAmount
          }
      );
}

function validateLoanPeriodField(thisComponent: any): NumberSchema {
  return yup.number()
      .typeError('Please enter a valid number')
      .required("Please enter a loan period")
      .test(
          "minimumLoanPeriod",
          "Loan period is below minimum allowed",
          (value) => {
            return value >= thisComponent.getValidationLimits().minimumLoanPeriodMonths
          }
      )
      .test(
          "maximumLoanPeriod",
          "Loan period is above maximum allowed",
          (value) => {
            return value <= thisComponent.getValidationLimits().maximumLoanPeriodMonths
          }
      );
}
</script>

<style scoped>
label {
  display: block;
  margin-top: 10px;
}

.card-container.card {
  max-width: 350px !important;
  padding: 35px 25px;
}

.card {
  background-color: #f7f7f7;
  margin: 50px auto 25px;
  -moz-border-radius: 2px;
  -webkit-border-radius: 2px;
  border-radius: 2px;
  -moz-box-shadow: 0 2px 2px rgba(0, 0, 0, 0.3);
  -webkit-box-shadow: 0 2px 2px rgba(0, 0, 0, 0.3);
  box-shadow: 0 2px 2px rgba(0, 0, 0, 0.3);
}

.error-feedback {
  color: red;
}

.form-group.required label:after {
  content:"*";
  color:red;
}

div.result-line {
  width: 100%;
}

span.result-heading {
  display: inline-block;
  width: 65%;
}

span.result-detail {
  display: inline-block;
  width: 35%;
}

span.highlight {
  font-weight: bold;
}

</style>
