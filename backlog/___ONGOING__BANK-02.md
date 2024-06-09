### BANK-02: Requested loan parameters validation

As a user, I want to enter loan applicant's personal code (SSN), loan amount (in Euros)  
and period (in months), and immediately understand whether the provided values are valid  
or not, as per the system's rules:

- Personal code (SSN) must be a valid Estonian SSN (in valid format)
- Requested loan amount can be from **2,000EUR up to 10,000EUR**
- Requested loan period can be from **12 months up to 60 months**

#### Subtasks  

  * **\[ OK ] BANK-02-A: Estonian SSN validation**
    * Ensure that the personal code is a valid Estonian personal code

  * **\[ O ] BANK-02-B: Requested loan amount range validation**
      * Ensure that the loan amount is between 2,000EUR and 10,000EUR  
    
  * **\[ O ] BANK_02_C: Requested loan period range validation**
      * Ensure that the loan period is between 12 months and 60 months  
