<template>
  <div class="col-md-12">
    <div class="card card-container">
      <Form @submit="doSubmit" :validation-schema="schema">
        <div class="form-group required">
          <label for="ssn">Personal code (SSN)</label>
          <Field name="ssn" type="text" class="form-control"/>
          <ErrorMessage name="ssn" class="error-feedback"/>
        </div>

        <div class="form-group required">
          <label for="loanAmount">Loan amount (€)</label>
          <Field name="loanAmount" type="text" class="form-control"/>
          <ErrorMessage name="loanAmount" class="error-feedback"/>
        </div>

        <div class="form-group required">
          <label for="loanPeriodMonths">Loan period (months)</label>
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
          <div v-if="request" class="alert alert-info" role="alert">
            {{ request }}
          </div>
        </div>

        <div class="form-group">
          <div v-if="error" class="alert alert-danger" role="alert">
            {{ error }}
          </div>
        </div>

        <div class="form-group">
          <div v-if="response" class="alert alert-success" role="alert">
            {{ response }}
          </div>
        </div>
      </Form>
    </div>
  </div>
</template>


<script lang="ts">
import { ErrorMessage, Field, Form } from 'vee-validate';
import * as yup from 'yup';
import { ValidationError } from 'yup';
import server, { COMPLETE_SSN_LENGTH } from "../server/server";
import { ValidationStatus } from "../models/SsnValidationResult";

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
            .required("Please enter a loan amount"),

        loanPeriodMonths:
          yup.number()
            .typeError('Please enter a valid number')
            .required("Please enter a loan period"),
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
      error: "",
      request: "",
      response: "",
      schema,
    };
  },
  computed: {},
  created() {
  },
  methods: {
    async doSubmit(request: any) {
      this.response = ''
      this.error = ''
      this.loading = true
      this.request = JSON.stringify(request)

      try {
        const healthResponse = await server.checkHealth()
        this.response = 'Health status: ' + healthResponse.status
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
  -moz-box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
  -webkit-box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
  box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
}

.error-feedback {
  color: red;
}

.form-group.required label:after {
  content:"*";
  color:red;
}
</style>
