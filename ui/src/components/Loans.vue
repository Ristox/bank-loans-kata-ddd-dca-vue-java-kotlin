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
          <button class="btn btn-primary btn-block" :disabled="loading">
              <span
                  v-show="loading"
                  class="spinner-border spinner-border-sm"
              ></span>
            <span>Submit</span>
          </button>
        </div>

        <div class="form-group">
          <div v-if="error" class="alert alert-danger" role="alert">
            {{ error }}
          </div>
        </div>

        <div class="form-group">
          <div v-if="eligibilityResult && eligibilityResult.result == LoanEligibilityStatus.APPROVED"
               class="alert alert-success" role="alert">
            <div class="result-line">
              <span class="result-heading">Loan request: </span>
              <span class="result-detail highlight">{{ eligibilityResult.result }}</span>
            </div>
            <div v-if="eligibilityResult.errors" class="result-line">
              <span>ERRORS here</span>
            </div>
            <div class="result-line">
              <span class="result-heading">For amount: </span>
              <span class="result-detail">{{ eligibilityResult.loanAmount }} €</span>
            </div>
            <div class="result-line">
              <span class="result-heading">For period: </span>
              <span class="result-detail">{{ eligibilityResult.loanPeriodMonths }} months</span>
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
import {ValidationError} from 'yup';
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
        ssn:
          yup.number()
            .typeError('Please enter a valid number')
            .required("Please enter a personal code (SSN)")
            .test(
              'validateSocialSecurityNumber',
              'Personal code (SSN) is not valid',
              validateSocialSecurityNumber
            ),

        loanAmount:
          yup.number()
            .typeError('Please enter a valid number')
            .required("Please enter a loan amount")
              .test(
                  "minimumLoanAmount",
                  "Loan amount is below minimum allowed",
                  async (value) => {
                    return value >= this.getValidationLimits().minimumLoanAmount
                  }
              )
              .test(
                  "maximumLoanAmount",
                  "Loan amount is above maximum allowed",
                  async (value) => {
                    return value <= this.getValidationLimits().maximumLoanAmount
                  }
              ),

        loanPeriodMonths:
          yup.number()
            .typeError('Please enter a valid number')
            .required("Please enter a loan period")
            .test(
                "minimumLoanPeriod",
                "Loan period is below minimum allowed",
                async (value) => {
                  return value >= this.getValidationLimits().minimumLoanPeriodMonths
                }
            )
            .test(
                "maximumLoanPeriod",
                "Loan period is above maximum allowed",
                async (value) => {
                  return value <= this.getValidationLimits().maximumLoanPeriodMonths
                }
            )
      });

    async function validateSocialSecurityNumber(value: number): Promise<boolean | ValidationError> {
      if (value.toString().length != COMPLETE_SSN_LENGTH) {
        return false
      }
      try {
        const validationResult = await server.validateSocialSecurityNumber(value)
        if (validationResult) {
          return validationResult.status == ValidationStatus.OK;
        } else {
          return true;
        }
      } catch (error: any) {
        return new ValidationError('Error: validation service unavailable')
      }
    }

    return {
      loading: false,
      validationLimits: new ValidationLimits(500, 1999, 6, 12),
      error: "",
      eligibilityResult: undefined as LoanEligibilityResult | undefined,
      schema,
    };
  },
  computed: {
    LoanEligibilityStatus() {
      return LoanEligibilityStatus
    }
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
      this.validationLimits = await server.loadValidationLimits()
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
      this.error = ''
      this.loading = true

      try {
        const {ssn, loanAmount, loanPeriodMonths} = request
        this.eligibilityResult = await server.calculateEligibilityFor(
            new LoanRequest(ssn, Number(loanAmount), Number(loanPeriodMonths))
        )
      } catch (err) {
        this.error = 'Error: service unavailable'
      } finally {
        this.loading = false
      }
    },
  },
};
</script>

<style scoped>
label {
  display: block;
  margin-top: 10px;
}

.card-container.card {
  max-width: 350px !important;
  padding: 40px 40px;
}

.card {
  background-color: #f7f7f7;
  padding: 20px 25px 30px;
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
  width: 50%;
}

span.result-detail {
  display: inline-block;
  width: 50%;
}

span.result-detail.highlight {
  font-weight: bold;
}

</style>
