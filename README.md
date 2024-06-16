# BANK LOAN ELIGIBILITY

* **[BUSINESS REQUIREMENTS](DOC01-REQUIREMENTS.md)**


* **[BACKLOG](DOC02-BACKLOG.md)**


### Technical requirements

* Java 17
* Node v18.18 or later

### Running the project 

**From project root -**

***Using Docker Compose (local java and node installations not required)***

1. Execute `docker-compose up`
2. Application UI is available at http://localhost
3. Server API is available at http://localhost/api or http://localhost:8080/api
   (root API URL does not have a service mapping)

***Without  Docker(local java and node installations are required)***
1. Make sure your command line / terminal is using
   * node v18.8 or higher (run `node -v`)
   * java v17 or higher (run `java -version`)
  

2. Execute `run.sh` (builds and runs the UI and the server)
   * or, execute `npm run build`, followed by `npm start`
  

3. Application UI is available at http://localhost:8088  
   Server API is available at http://localhost:8088/api/ or http://localhost:8088/api  
   (root API URL does not have a service mapping)

