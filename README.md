# BANK LOAN ELIGIBILITY

* **[BUSINESS REQUIREMENTS](DOC01-REQUIREMENTS.md)**


* **[BACKLOG](DOC02-BACKLOG.md)**


### Technical requirements

* Java 17
* Node v18.18 or later

### Running (only in development mode, as of now) 

From project root -

1. Execute `npm run build`
2. Execute `cd server && ./gradlew bootRun`  
   (runs the backend server)
3. In a new terminal window, execute `cd ui && npm run dev`  
   (runs the UI frontend)

NB - make sure you're using Node v18.18 or newer in both terminals
