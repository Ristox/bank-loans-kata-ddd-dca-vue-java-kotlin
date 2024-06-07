### BANK-06: Personalized loan eligibility result, with loan period availability details

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


#### Subtasks

* WIP
