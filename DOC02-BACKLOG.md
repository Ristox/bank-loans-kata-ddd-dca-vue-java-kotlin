# BANK LOAN ELIGIBILITY - BACKLOG

## Iteration #1  

###  \[ O ] BANK-01:  Loan application input form

   As a user, I want to see the input form for loan application -  
    - with fields for a personal code (SSN), loan amount (in Euros) and loan period (in months)  
    - so that I can enter my desired loan information, currently without any restrictions


#### Subtasks

   * **\[ O ] BANK-01-A: initial setup**  
     Define requirements and divide them into incremental stories (create an initial backlog)  
     Create initial project structure - separating UI and Server into distinct subdirectories
     Create a blank / template frontend and backend which are runnable together  


   * **\[ _ ] BANK-01-B: initial loan application form**
     Server: create an empty "ping" endpoint, which does nothing, just responds with a static json
     UI: create a simple web form with three numeric fields for the loan input data (no restrictions)  
     UI: have "submit" button call the empty "ping" endpoint
     

## Iteration #2  

### \[ _ ] BANK-02: Requested loan parameters validation

   As a user, I want to enter loan applicant's personal code (SSN), loan amount (in Euros)  
   and period (in months), and immediately understand whether the provided values are valid  
   or not, as per the system's rules:

   - Personal code (SSN) must be a valid Estonian SSN (in valid format)
   - Requested loan amount can be from **2,000EUR up to 10,000EUR**
   - Requested loan period can be from **12 months up to 60 months**


## Iteration #3

### \[ _ ] BANK-03: Default loan elibility result

   As a user, I want to be able to submit valid personal code (SSN), loan amount (in Euros)  
   and period (in months), and receive a default, positive credit result with the requested  
   loan amount and period, which is independent of a given loan applicant's SSN.


## Iteration #4

### \[ _ ] BANK-04: Personalized loan eligibility result

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



## Iteration #5

### \[ _ ] BANK-05: Personalized loan eligibility result, with maximum allowed loan amount details

   As a user, I want to be able to submit valid personal code (SSN), loan amount (in Euros)  
   and period (in months) - and receive a personal credit result, which is calculated  
   based on the credit score for given loan applicant.  
   This should be calculated based on the requirements described in `INBANK-05` - however,  
   I should additionally receive -


   * A positive result, if `creditScore > 1` - along with **a maximum available loan amount**  
     that would not yet result in a credit score of 1 (meaning loan denial).  
     (That maximum available loan amount should therefore be larger than the requested amount)


   * A negative result, if `creditScore <= 1` - along with maximum allowed loan amount  
     that would result in a creditScore > 1  
     (That maximum available loan amount should therefore be less than the requested amount)



## Iteration #6

### \[ _ ] BANK-06: Personalized loan eligibility result, with loan period availability details 

   As a user, I want to be able to submit valid personal code (SSN), loan amount (in Euros)  
   and period (in months), but receive a different available loan period than requested,  
   as part of loan eligibility result, depending on loan applicant (SSN).  
  
 
   I.e.: 
   *  For 49002010987, if I submit a loan period less than 24 or more than 36 months,  
      I receive a valid range of 24...36 months - for any requested loan of 5000 Euros or greater.  
      For any sum below 5000 Euros, the originally requested loan period should remain  
      as a part of eligibility result
  

   *  For 49002010998, if I submit a loan period more than 48 months,  I receive  
     a valid range of 12...48 months - for any requested loan of 7700 Euros or greater.  
  

   * For 49002010976, any valid period will do for any requested loan amount
