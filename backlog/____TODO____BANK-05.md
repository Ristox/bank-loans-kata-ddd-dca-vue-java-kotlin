### BANK-05: Personalized loan eligibility result, with maximum allowed loan amount details

As a user, I want to be able to submit valid personal code (SSN), loan amount (in Euros)  
and period (in months) - and receive a personal credit result, which is calculated  
based on the credit score for given loan applicant.  
This should be calculated based on the requirements described in [BANK-04](backlog/___ONGOING__BANK-04.md) - however,  
I should additionally receive -


* A positive result, if `creditScore > 1` - along with **a maximum available loan amount**  
  that would not yet result in a credit score of 1 (meaning loan denial).  
  (That maximum available loan amount should therefore be larger than the requested amount)


* A negative result, if `creditScore <= 1` - along with maximum allowed loan amount  
  that would result in a creditScore > 1  
  (That maximum available loan amount should therefore be less than the requested amount)


#### Subtasks

* WIP
