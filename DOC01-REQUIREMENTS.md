# BANK LOAN ELIGIBILITY - REQUIREMENTS

As a bank, we need to be able to determine loan eligibility for a given person (loan applicant)

### Input and output

Loan eligibility feature enables the user to provide:
- the loan applicant's personal code (SSN)
- a requested loan amount
- a requested loan period (in months)

and responds to the user with
- a decision (negative or positive eg loan approval or denial),
- the maximum loan amount (available for loan, for given person)
- the allowed loan period (either the one requested, or if not eligible  
   for a specific loan request, the range of periods)

### Detailed constraints

1. As part of loan eligibility, we need to determine - what would be  
   the maximum allowed loan amount, regardless of the loan applicant  
   (a person) for whom the loan was requested.  

   - I.e., if a person applies for 4000 €, but we determine that their credit score  
     allows for a larger amount,then the result should be the larger sum,  
     which we would approve

   - Or - in reverse, if a person applies for 4000 € and we would not approve it -  
   then we want to return the largest amount which we would approve (i.e. 2500 €)


2. If the requested loan amount is not permitted for the requested period,  
   then we should try to determine a new suitable period, given the requested  
   loan amount and the loan applicant's SSN.

   - _In real life we should connect to external registries and compose  
     a comprehensive user profile - but for the sake of simplicity, this part  
     can be a hardcoded result for certain personal codes._
  

3. Currently we only need to support **4 different scenarios**
    - a person has debt
    - or a person falls under one of 3 different segmentations.  
   
    - I.e.:
  
          49002010965 - debt
          49002010976 - segment 1 (credit_modifier = 100)
          49002010987 - segment 2 (credit_modifier = 300)
          49002010998 - segment 3 (credit_modifier = 1000)

4. If a person has debt, then we do not approve any amount.


5. If a person has no debt then we take the segment identifier (_credit_modifier_)  
   and use it for calculating person's credit score - taking into account  
   the requested input


6. **Minimum** input and output sum can be **2000 €**  
   **Maximum** input and output sum can be **10000 €**  
   **Minimum** loan period can be **12 months**  
   **Maximum** loan period can be **60 months**


7. **Scoring** -  
   For calculating credit score, we currently need just a really primitive algorithm:
    - divide the credit modifier with the loan amount
    - multiply that result with the loan period in months, eg: 
   
          creditScore = (creditModifier / loanAmount) * loanPeriod

   - If the result is less than 1 - then we would deny the requested amount.
   - If the result is larger than or equal to 1 - then we would approve  
     the requested amount.


8. We need a Web UI with a form for the described input data, along with  
   a calculation (submit) button


9. When a loan request is submitted and processed, the form should display  
   the loan eligibility decision (approved / denied), along with the details, based on  
   the constraints described above
    * _Technically - the WEB UI should call a single REST API service, eg:_  
   
          POST/api/loan/eligibility

