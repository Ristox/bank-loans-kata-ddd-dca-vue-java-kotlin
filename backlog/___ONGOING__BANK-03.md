### BANK-03: Default loan elibility result

As a user, I want to be able to submit valid personal code (SSN), loan amount (in Euros)  
and period (in months), and receive a default, positive credit result with the requested  
loan amount and period, which is independent of a given loan applicant's SSN.

#### Subtasks

* **\[ OK ] BANK-03-A: a static loan eligibility endpoint**
  * Implement a loan eligibility endpoint which currently always returns a positive eligibility  
    result, regardless of input data

* **\[ O ] BANK-03-B: calling eligibility service and showing the result details in UI **
  * Implement the client-side call to eligibility endpoint and accepting result data
  * Display the result data with all details (ssn, available loan amount and period) in the form.
  * Remove the usage of initial health service 


* **\[ _ ] BANK-03-C: server side validations for requested loan data (SSN, amount and period)**
  * Remove initially created health endpoint
  * Implement server-side validations for request (personal code, loan amount and loan period) -  
    so that limits are ensured even if endpoint is used directly (bypassing the UI)

