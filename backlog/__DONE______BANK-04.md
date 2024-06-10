### BANK-04: Personalized loan eligibility result

As a user, I want to be able to submit valid personal code (SSN), loan amount (in Euros)  
and period (in months), and receive a personal credit result, which is calculated based on  
the credit score for given loan applicant. 


This should be calculated based on the following example data

        49002010965 - debt
        49002010976 - segment 1 (credit_modifier = 100)
        49002010987 - segment 2 (credit_modifier = 300)
        49002010998 - segment 3 (credit_modifier = 1000)

using formula:

        creditScore = (creditModifier / loanAmount) * loanPeriod

deciding on a positive result (approval), if `creditScore > 1` - along with the  
requested loan amount and requested period

deciding on a negative result (denial), if `credicScore <= 1` - along with  
no available loan amount, for requested period


#### Subtasks

* **\[ OK ] BANK-04-A: simple storage for personalized credit segments**
    * Implement a domain gateway + an in-memory adapter with described personalized  
      credit segments for each specified personal code (SSN)

* **\[ OK ] BANK-04-B: personalized credit score calculation**
    * implement credit score calculation which utilizes the personal credit segment  
      gateway and requested loan data
    * return a DENIED eligibility result for any personal code (SSN) which has no  
      credit segment

