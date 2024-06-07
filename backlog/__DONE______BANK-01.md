### BANK-01:  Loan application input form

As a user, I want to see the input form for loan application -  
- with fields for a personal code (SSN), loan amount (in Euros) and loan period (in months)  
- so that I can enter my desired loan information, currently without any restrictions


#### Subtasks

* **\[ OK ] BANK-01-A: initial setup**  
  * Define requirements and divide them into incremental stories (create an initial backlog)  
  * Create initial project structure - separating UI and Server into distinct subdirectories
  * Create a blank / template frontend and backend which are runnable together


* **\[ OK ] BANK-01-B: initial loan application form**
  * Server: create an empty "health" endpoint, which does nothing, just responds with a static json
  * UI: create a simple web form with three numeric fields for the loan input data (no restrictions)  
  * UI: have "submit" button call the "health" endpoint
