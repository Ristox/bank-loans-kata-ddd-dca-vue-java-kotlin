### BANK-06: Personalized loan eligibility result, with loan period availability details

As a user, I want to be able to submit valid personal code (SSN), loan amount (in Euros)  
and period (in months), but receive a different available loan period (and corresponding amount)  
than requested as part of loan eligibility result - depending on loan applicant (SSN).


I.e.:
*  For a customer of low credit score (i.e. 49002010976), if I apply for a loan of 5,000 Euros,  
   for only a 12-month period, then my credit score would not allow any loans for that period,
   since it'd only allow up to 1,200 Euros which is less than loan minimum of 2,000 Euros.
   However, in this case I want to receive a new period which would be eligible for me, along
   with a new suggested loan amount. In this example, I would receive a new period of 51 months,
   which allows for a loan of up to 5099, given the credit score of 100.  

  
* Hence, I wish for the system to return me not just a pre-defined eligible period, but one that  
  still satisfies credit requirements eg results in credit score > 1.

  
* However, if I am a loan applicant with debt - i.e. 49002010965, then applying for same loan  
  (or any loan, for that matter) will result in DENIED result without any eligible loan options.


#### Subtasks

* **\[ OK ] BANK-06-A: invoke calculation of new eligible loan period, when eligible amount not available**
    * When initially calculated eligible amount is absent and person is not debt (by credit segment),  
      then invoke calculation for a new eligible loan period, given the request and credit segment.
    * When a new eligible period is available, recalculate the eligible amount for that period
    * Return loan eligibility result with the new eligible period and amount details

* **\[ OK ] BANK-06-B: perform the new eligible loan period calculation, as a "first minimum period"**
    * Create a gateway adapter for determining the eligible loan period, which simply calculates the  
      first eligible (minimum) period for the given amount, which would satisfy credit requirements  
      of credit score > 1.
    * Ensure that the adapter does not calculate any eligible loan period for a DEBT credit segment  
      (returns empty result)

* **\[ OK ] BANK-06-C: when available, show the new eligible period (along with eligible amount) in UI**
    * In UI, show the new eligible period, when its calculated - along with the eligible amount.  


* **\[ OK ] BANK-06-D: architecture restructuring + technical refactorings**
    * Modularize the application - create separate modules for app runnable and loans functionality.
    * Split loans module into a) public API b) internal core implementation and c) infrastructure adapters  
      (submodules)
    * Simplify running the whole project in preview / prod mode, via a single command (`run.sh`)
    * Finalize README.md
