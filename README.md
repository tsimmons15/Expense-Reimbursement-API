# EXPENSE REIMBURSEMENT API

## Project Description

The Employee Reimbursement System (ERS) is a REST API that helps manage the process of reimbursing employees for expenses. 

## Technologies Used

 * Java - build 15
 * Maven - version 3.8.5
   * Maven-shade-plugin - version 3.3.0
   * Maven-surefire-plugin - version 2.22.2
 * JUnit Jupiter - version 5.8.2
   * JUnit Platform suite - version 1.8.2
 * Javalin - version 4.4.0
 * GSON - version 2.9.0
 * PostgreSQL - version 42.2.25

## Features



## Getting Started
   
To clone: `git clone https://github.com/tsimmons15.git`

### Setup
 * Getting started with Java: ![Link to download Java](https://www.oracle.com/java/technologies/downloads/)
 * Getting started with Maven: ![Link to find the download for Maven](https://maven.apache.org/download.cgi)
 * Getting started with Postman: ![Download Postman to handle making requests](https://www.postman.com/downloads/)
 * Getting started with PostgreSQL: ![If you need to install an instance locally.](https://www.postgresql.org/download/)
   * Alternatively, using an AWS RDS instance: ![Getting started with an AWS RDS PostgreSQL instance.](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_GettingStarted.CreatingConnecting.PostgreSQL.html)
   * Once the PostgreSQL database is set up run the following script to create the database structure.
```PostgreSQL
create database expenses;

create table employee (
	employee_id serial primary key,
	first_name varchar(30) not null,
	last_name varchar(30) not null
);

create table expense (
	expense_id serial primary key,
	amount bigint not null check(amount > 0),
	status varchar(8) not null,
	date bigint not null,
	issuer int
);

alter table expense add constraint exp_employee_fk foreign key(issuer) references Employee(employee_id);

create function checkExpenseStatus() 
returns trigger
language plpgsql
as $$
begin
	if (old.status != 'PENDING') then
		raise exception 'Attempt to update a non-pending expense.';
		return null;
	end if;
	if (TG_OP = 'DELETE') then
		return old;
	end if;
	return new;
end; $$



create trigger checkStatusOnUpdate before update on expense
for each row
execute function checkExpenseStatus();

create trigger checkStatusOnDelete before delete on expense
for each row 
execute function checkExpenseStatus();
```

 * After downloading everything and setting up your environment, in the root folder with the *pom.xml* file run the following in either a terminal(Mac/Linux) or cmd(Windows):
```
  mvn package -f pom.xml
  cd ./target
  java -jar timothy_simmons_p1-1.0.jar
```

## Usage

From Postman, the routes you can handle are below, with a little information about what to expect for each.

 * Valid JSON for the entities is:
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

 * Routes
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

## License

This project uses the following license: [The Unlicense](https://choosealicense.com/licenses/unlicense/).
