<template>
  <div class="col-md-12">
    <div class="card card-container">
      <Form @submit="doSubmit" :validation-schema="state.schema">

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
          <button class="btn btn-outline-primary btn-block" :disabled="state.loading">
              <span
                  v-show="state.loading"
                  class="spinner-border spinner-border-sm"
              ></span>
            <span>Apply for loan</span>
          </button>
        </div>

        <div class="form-group">
          <div v-if="state.error" class="alert alert-danger alert" role="alert">
            {{ state.error }}
          </div>
        </div>

        <div class="form-group">
          <div v-if="state.eligibilityResponse"
               class="alert" role="alert"
               v-bind:class="{
                  'alert-success': loanApproved,
                  'alert-warning': loanDenied,
                  'alert-danger': loanInvalid }">

            <div class="result-line">
              <span class="result-heading">Loan request: </span>
              <span class="result-detail highlight">{{ state.eligibilityResponse.result }}</span>
            </div>

            <div v-if="state.eligibilityResponse.errors" class="result-line">
              <div class="result-line">Errors:</div>
              <ul>
                <li v-for="error in state.eligibilityResponse.errors" :key="error.toString()">
                  {{ error }}
                </li>
              </ul>
            </div>

            <div class="result-line">
              <span class="result-heading">Requested amount: </span>
              <span class="result-detail">{{ state.eligibilityResponse.loanAmount }} €</span>
            </div>

            <div class="result-line">
              <span class="result-heading">Requested period: </span>
              <span class="result-detail">{{ state.eligibilityResponse.loanPeriodMonths }} months</span>
            </div>

            <div v-if="state.eligibilityResponse.eligibleLoanAmount != null" class="result-line">
              <hr />
              <span class="result-heading" v-if="loanApproved">Offered amount: </span>
              <span class="result-heading" v-else-if="loanDenied">Eligible amount: </span>
              <span class="result-detail highlight">{{ state.eligibilityResponse.eligibleLoanAmount }} €</span>
            </div>

            <div v-if="state.eligibilityResponse!!.eligibleLoanPeriod != null" class="result-line">
              <span class="result-heading">Eligible period: </span>
              <span class="result-detail highlight">{{ state.eligibilityResponse.eligibleLoanPeriod }} months</span>
            </div>
          </div>
        </div>

      </Form>
    </div>
  </div>
</template>


<script lang="ts" setup>
import {ErrorMessage, Field, Form} from 'vee-validate';
import * as yup from 'yup';
import {NumberSchema} from 'yup';
import server, {COMPLETE_SSN_LENGTH} from "../server/server";
import {ValidationStatus} from "@/models/SsnValidationResult";
import ValidationLimits from "@/models/ValidationLimits";
import LoanRequest from "@/models/LoanRequest";
import LoanEligibilityResult, {LoanEligibilityStatus} from "@/models/LoanEligibilityResult";
import {computed, onMounted, reactive} from "vue";

const validateSsnField = (): NumberSchema => {
  return yup.number()
      .typeError('Please enter a valid number')
      .required("Please enter a personal code (SSN)")
      .test(
          'validateSocialSecurityNumber',
          'Personal code (SSN) is not valid',
          async (value) => await validateSocialSecurityNumber(value + '') // TODO hack
      );
}

const validateLoanAmountField = (): NumberSchema => {
  return yup.number()
      .typeError('Please enter a valid number')
      .required("Please enter a loan amount")
      .test(
          "minimumLoanAmount",
          "Loan amount is below minimum allowed",
          (value) => {
            return value >= getValidationLimits().minimumLoanAmount
          }
      )
      .test(
          "maximumLoanAmount",
          "Loan amount is above maximum allowed",
          (value) => {
            return value <= getValidationLimits().maximumLoanAmount
          }
      );
}

const validateLoanPeriodField = (): NumberSchema => {
  return yup.number()
      .typeError('Please enter a valid number')
      .required("Please enter a loan period")
      .test(
          "minimumLoanPeriod",
          "Loan period is below minimum allowed",
          (value) => {
            return value >= getValidationLimits().minimumLoanPeriodMonths
          }
      )
      .test(
          "maximumLoanPeriod",
          "Loan period is above maximum allowed",
          (value) => {
            return value <= getValidationLimits().maximumLoanPeriodMonths
          }
      );
}

const schema = yup.object()
    .shape({
      ssn: validateSsnField(),
      loanAmount: validateLoanAmountField(),
      loanPeriodMonths: validateLoanPeriodField()
    });

const state = reactive({
  loading: false,
  validationLimits: new ValidationLimits(500, 1999, 6, 12) as ValidationLimits,
  error: "",
  eligibilityResponse: null as LoanEligibilityResult | null,
  schema
});

const loanApproved = computed(
    () => state.eligibilityResponse?.result == LoanEligibilityStatus.APPROVED
);

const loanDenied = computed(
    () => state.eligibilityResponse?.result == LoanEligibilityStatus.DENIED
);

const loanInvalid = computed(
    () => state.eligibilityResponse?.result == LoanEligibilityStatus.INVALID
);

const loadValidationLimits = async () => {
  state.loading = true;
  try {
    state.validationLimits = await server.loadValidationLimits();
  }
  catch (error) {
    state.error = 'Error: service unavailable (unable to acquire loan limits, will use default values)';
  }
  state.loading = false;
}

const validateSocialSecurityNumber = async (ssn: string) => {
  if (ssn.toString().length != COMPLETE_SSN_LENGTH) {
    return false
  }
  try {
    state.loading = true
    state.error = ''
    const validationResult = await server.validateSocialSecurityNumber(Number(ssn))
    state.loading = false
    if (validationResult) {
      return validationResult.status == ValidationStatus.OK;
    } else {
      return true;
    }
  } catch (error) {
    state.error = 'Error: service unavailable (unable to perform validation)'
    state.loading = false
    return true
  }
}

onMounted(async () => {
  try {
    await loadValidationLimits();
  }
  catch (error) {
    state.error = 'Error: service unavailable (unable to acquire loan limits, will use default values)';
  }
});

const getValidationLimits = () => {
  return state.validationLimits;
}
const minimumAmount = () => {
  return state.validationLimits.minimumLoanAmount;
}

const maximumAmount = () => {
  return state.validationLimits.maximumLoanAmount;
}

const minimumPeriod = () => {
  return state.validationLimits.minimumLoanPeriodMonths;
}

const maximumPeriod = () => {
  return state.validationLimits.maximumLoanPeriodMonths;
}

const doSubmit = async (request: any) => {
  state.loading = true
  try {
    const {ssn, loanAmount, loanPeriodMonths} = request
    state.eligibilityResponse = await server.calculateEligibilityFor(
        new LoanRequest(ssn, Number(loanAmount), Number(loanPeriodMonths))
    )
    state.error = ''
  } catch (error) {
    state.eligibilityResponse = null
    state.error = `Error: service unavailable (unable to calculate loan eligibility)`
  } finally {
    state.loading = false
  }
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

.form-group label {
  margin-bottom: 5px;
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

.btn.btn-block {
  margin-top: 25px;
  margin-bottom: 20px;
}

</style>
