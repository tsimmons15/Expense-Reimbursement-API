# Expense Reimburesement API

## Objective
The Employee Reimbursement System (ERS) is a REST API that helps manage the process of reimbursing employees for expenses. 

### Requirements
 - Employees can be created and edited via the API. 
 - Expenses for employees can be added and updated to pending and approved. 
 - Approved/Denied expenses can not be edited.
 - A valid employee:
  ```javascript
    {
      'firstName': string,
      'lastName': string
    }
  ```
 - A valid expense:
  ```javascript
    {
      'amount': long,
      'issuer': int, // must be a valid Employee ID
      'status': ('APPROVED', 'PENDING', 'DENIED'),
      'date': long
     }
  ```

## Technologies
 - Java
   - Javalin
   - JDBC
   - Custom ORM
   - Custom Logger
- PostgreSQL
- AWS
  - Elastic Beanstalk
  - RDS PotgreSQL instance
- Github

## Getting Started
 - The project is written in Java, so make sure you have at least Java. The specific version used was corretto-1.8 (v1.8.0_322) but any version of Java past Java 8 should work. ![Link to download Java](https://www.oracle.com/java/technologies/downloads/)
 - The project is packaged with Maven, specifically 3.8.5. ![Link to find the download for Maven](https://maven.apache.org/download.cgi)
 - Three system variables need to be set up to store the database location, password and username.
   - POSTGRES_AWS needs to be jdbc:postgresql:<your server location/url>:5432
   - POSTGRES_PASSWORD needs to be the password for the database.
   - POSTGRES_USER needs to be the username for the database.
### Building
After downloading everything and setting up your environment, in the root folder with the *pom.xml* file run:
```bash
  mvn package -f pom.xml
  cd ./target
  java -jar timothy_simmons_p1-1.0.jar
```
### Usage
 The routes handled by the API
- GET /
  - returning a default 200 OK
- GET /employees
  - returns the list of employees
- GET /employees/{id}
  - returns the specified employee, if present
- POST /employees
  - if a valid employee JSON is provided, will insert into the database
- PUT /employees/{id}
  - updates an existing employee with the JSON provided, if valid
- DELETE /employees/{id}
  - removes an employee from the database, if possible.
- GET /employees/{id}/expenses
  - returns the list of expenses for the specified employee.
- POST /employees/{id}/expenses
  - If provided a valid expense JSON, will attempt to add the expense to the database with the given issuer id.
- GET /expenses
  - returns the list of expenses
- GET /expenses/{id}
  - returns the specified expense, if present
- GET /expenses?status={status}
  - returns the list of expenses matching the given status.
- POST /expenses
  - if provided a valid expense JSON will insert the expense into the database
- PUT /expenses/{id}
  - if provided a valid expense JSON, will attempt to update that expense in the database.
- PATCH /expenses/{id}/approved
  - will attempt to approve the specified expense
- PATCH /expenses/{id}/denied
  - will attempt to deny the specified expense
- DELETE /expenses/{id}
  - attempts to delete the provided expense
