Assignment - Maybank
==========================

# About
An assessment assignment task to demonstrate following case study requirement(s) : 

* **🗄️ BATCH JOB** to consume a **🧾 CSV** formatted [resource file](resources/docs/dataSource.txt)
* **RESTful APIs** to : 
	* **🌐 RETRIEVE** the entry record(s) that was imported by the batch job with functionalities below : 
		* API authentication required
		* Pagination result(s) expected
		* Search filter capabilities (`customerId` OR `accountNumber(s)` OR `description`)
	* **🌐 UPDATE** the entry record(s) that was imported by the batch job with functionalities below : 
		* API authentication required
		* `description` field **ONLY**
		* Able to handle concurrent update


# Analyis, Design & Development
This section describes my approaches & assumptions based on the case study requirement(s) mentioned above : 

* Proposing micro-serviced architecture with the following distributed system application(s) : 
	* [User Management Service](user-management-service) 
		* Following is used to service a basic user authentication using `username` and `password` as the credential for API calls
		* It will be populated with pre-defined roles and users when the application is started/booted for the first time using [Liquibase](http://liquibase.com) 
		* `ONE(1)` API will be needed 
			* An authenticate `http://localhost:18081/api/v1/user/auth` **[POST]** API will be exposed that will response HTTP `200(OK)` with JSON response body containing the authenticated user details (i.e. roles)
	* [Accounting Management Service](accounting-management-service)
		* Following is used to service `THREE(3)` APIs for our demo
			* An upload `http://localhost:18082/api/v1/accounting/upload` **[POST]** API to allow to upload the batch [dataSource.txt](resources/docs/dataSource.txt) file as per-required that will response HTTP `200(OK)` with JSON response body containing a simple message 
			* A batch update description `http://localhost:18082/api/v1/accounting` **[PATCH]** API to allow to update description by batch that will response HTTP `200(OK)` with JSON response body containing a simple message 
			* An API to search paginated `http://localhost:18082/api/v1/accounting` **[POST]** API that will response HTTP `200(OK)` with JSON response body of a very basic pagination structure containing the array of result items per-page and a next page cursor to use to obtain the next page(s)
	* [RDS - Postgresql](https://hub.docker.com/_/postgres)
		* Database storage for the [User Management Service](user-management-service) and [Accounting Management Service](accounting-management-service) 
	* [Zookeeper](https://hub.docker.com/r/wurstmeister/zookeeper)
		* Internally used by Kafka to manage brokers to promote a leader
	* [Kafka](https://hub.docker.com/r/wurstmeister/kafka)
		* To produce batch operation instructions to a topic which then the listener to pickup and act on it

## 👤 User Management Service
* It a requirement that the APIs **MUST BE** authenticated
* I'll cover as well the authorized flow (based on simple role-based access)
* We'll introduce at least **ONE(1)** REST API to perform a basic-authentication using username and password
	* Credentials of the users are stored in DB (passwords are hashed, never plain using [Password4j](https://github.com/Password4j/password4j) 3rd party library)
* Users are pre-populated when the application boots up ; no duplicates will happen because we're using [Liquibase](https://www.liquibase.com) to insert the records
* To handle **🌐 AUTH** requirement(s) : 
	* `POST` method will be used for the following API so that we can consume `JSON request body` and will produce `JSON response body` containing a [user response-body structure](user-management-schema/src/main/java/my/com/maybank/schema/entity/User.java) with HTTP status code `200(OK)`

### Database Schema
Below is the schema for **User Management Service** micro-service

![User Managment Service Schema](resources/docs/db-schema.usermgmt.svg)

### Sample Demo Data

#### Roles

| ID | NAME |
|---|---|
| `a9842b08-f03f-479c-8949-c41baf984b9a` | `ROLE_USER` |
| `2240c93b-fa2e-4882-b56a-ae579291d2f0` | `ROLE_ADMIN` |
| `8982f932-dab8-4ed1-9b87-1a2adae5d36e` | `ROLE_CUSTOMER` |

#### Users 

| ID | USERNAME | PASSWORD | ROLE(s) |
|---|---|---|---|
| `1` | `admin` | `admin` | `ROLE_USER`, `ROLE_ADMIN` |
| `222` | `john.doe` | `password` | `ROLE_USER`, `ROLE_CUSTOMER` |
| `333` | `jane.doe` | `password` | `ROLE_USER`, `ROLE_CUSTOMER` |

## 🏦 Accounting Management Service
* To handle **🗄️ BATCH JOB** requirement(s) : 
	* **MUST BE** able to process the **🧾 CSV** formatted [resource file](resources/docs/dataSource.txt)
	* **MUST BE** able to handle batch processing
	* Ideally the entry point to kickstart the batch **🧾 CSV** file processing can be either via `🖥️ CLI` -OR- `🌐 UPLOAD` API
		* ⚖️ **DECISION** : We'll go `🌐 UPLOAD` direction as it can then be written with either scripting OR another application layer to consume the API 
		* To handle **🌐 UPLOAD** requirement(s) :
			* `POST` method will be used for the following API so that we can consume `multipart/data request` and will produce `JSON response body` containing a [simple message structure](commons-core/src/main/java/my/com/maybank/core/models/MessageModel.java) with HTTP status code `200(OK)`
				* Simple message returns a friendly message to inform the API consumer the total number of records imported sent to the batch process (<i>the reported total number **MUST MATCH** the total rows of the **🧾 CSV** file, excluding the header</i>)
			* API error will produce `JSON response body` containing a [simple message structure](commons-core/src/main/java/my/com/maybank/core/models/MessageModel.java) with corresponding HTTP status code `4xx (Client Errors Range)` -OR- `5xx (Server Errors Range)` 
			* API authentication will be using a simple basic authentication (`username+password`) via the micro-service [User Management Service](user-management-service) with admin role authorization access restriction(s)
				* If a user role is *non-admin* `403(FORBIDDEN)` returns
			* **🧾 CSV** records are read line-by-line and will be grouped into batches (see [application.yml](assignment-maybank/accounting-management-service/src/main/resources/application.yml) for property `app.service.accounting.transaction-job-batch-size` as well as the [.env](.env)) environment name `ACCOUNTING_MANAGEMENT_JPA_BATCH_SIZE` to send to [Kafka Topic](https://www.javatpoint.com/kafka-topics) (its default to `10` in this demo)
			* Once the batch(es) are created, there will be a background service that listens to the [Kafka Topic](https://www.javatpoint.com/kafka-topics) to get the batch(es) and then processes (in this case, to call the service-repository class to **CREATE** the records)
* To handle **🌐 RETRIEVE** requirement(s) : 
	* `POST` method will be used for the following API so that we can consume `JSON request body` and will produce `JSON response body` containing a [paginated result structure](accounting-management-service/src/main/java/my/com/maybank/accmgmt/models/AccountTransactions.java) with HTTP status code `200(OK)`
		* JSON structured paginated result
	* API error will produce `JSON response body` containing a [simple message structure](commons-core/src/main/java/my/com/maybank/core/models/MessageModel.java) with corresponding HTTP status code `4xx (Client Errors Range)` -OR- `5xx (Server Errors Range)`
	* API authentication will be using a simple basic authentication (`username+password`) via the micro-service [User Management Service](user-management-service) with user role authorization access restriction(s) 
	* Paginated results
	* Search filter supporting field(s) : 
		* `customerId (NUMBER)` - By customer identifier (*criteria* : `Number`, `Exact`, `Optional`)
			* If the authenticated API consumer is *non-admin* and the provided `customerId` not belonging to the user `403(FORBIDDEN)` returns 
		   * If the authenticated API consumer is *non-admin* and not provided `customerId` then it is enforced/restricted to the API consumer own records ONLY 
		* `accountNumber (STRING)` - By account number (*criteria* : `String`, `Wildcard`, `Optional`)
		* `description (STRING)` - By description (*criteria* : `String`, `Wildcard`, `Optional`)
* To handle **🌐 UPDATE** requirement(s) : 
	* `PATCH` method will be used for the following API so that we can consume `JSON request body` and will produce `JSON response body` containing a [simple message structure](commons-core/src/main/java/my/com/maybank/core/models/MessageModel.java) with HTTP status code `200(OK)`
	* Simple message returns a friendly message to inform the API consumer the total number of records sent to the batch process (<i>the reported total number **MUST MATCH** the total rows that matched with the results returned using the **🌐 RETRIEVE** API</i>)
	* API error will produce `JSON response body` containing a [simple message structure](commons-core/src/main/java/my/com/maybank/core/models/MessageModel.java) with corresponding HTTP status code `4xx (Client Errors Range)` -OR- `5xx (Server Errors Range)`
	* API authentication will be using a simple basic authentication (`username+password`) via the micro-service [User Management Service](user-management-service) with user role authorization access restriction(s) 
	* Paginated results
	* Search filter supporting field(s) : 
		* `customerId (NUMBER)` - By customer identifier (*criteria* : `Number`, `Exact`, `Optional`)
			* If the authenticated API consumer is *non-admin* and the provided `customerId` not belonging to the user `403(FORBIDDEN)` returns 
		   * If the authenticated API consumer is *non-admin* and not provided `customerId` then it is enforced/restricted to the API consumer own records ONLY 
		* `accountNumber (STRING)` - By account number (*criteria* : `String`, `Wildcard`, `Optional`)
		* `description (STRING)` - By description (*criteria* : `String`, `Wildcard`, `Optional`)
	* ONLY `description` field is ALLOWED to be updated
	* Result records are read and will be grouped into batches (see [application.yml](assignment-maybank/accounting-management-service/src/main/resources/application.yml) for property `app.service.accounting.transaction-job-batch-size` as well as the [.env](.env)) environment name `ACCOUNTING_MANAGEMENT_JPA_BATCH_SIZE` to send to [Kafka Topic](https://www.javatpoint.com/kafka-topics) (its default to `10` in this demo)
	* Once the batch(es) are created, there will be a background service that listens to the [Kafka Topic](https://www.javatpoint.com/kafka-topics) to get the batch(es) and then processes (in this case, to call the service-repository class to **UPDATE** the records)

### Database Schema
Below is the schema for **User Management Service** micro-service

![Accounting Managment Service Schema](resources/docs/db-schema.accmgmt.svg)

## 📐👷🏻‍♀️ Diagrams
### Architecture Overview Diagram
Below is an architecture diagram implementation overview

![Architecture Diagram Implementation Overview](resources/docs/arch-diagram-overview.svg)

### Modules Class Diagrams
#### Module [accounting-management-schema](accounting-management-schema) Class Diagram
Following is the schema classes for accounting management (to map RDS table-column to POJO entities)

![Class Diagram Module : accounting-management-schema](resources/docs/class-diagram-accounting-management-schema.svg)

#### Module [user-management-schema](user-management-schema) Class Diagram
Following is the schema classes for user management (to map RDS table-column to POJO entities)

![Class Diagram Module : user-management-schema](resources/docs/class-diagram-user-management-schema.svg)

#### Module [commons-jpa](commons-jpa) Class Diagram
Following is the JPA utility classes (to assist with JPA related processing)

![Class Diagram Module : commons-jpa](resources/docs/class-diagram-commons-jpa.svg)

####< ... more to add if/when needed >

### Activity Diagrams
#### Upload CSV API
Following is the activity diagram for the upload of the **🧾 CSV** file and batch producer process

![Activity Diagram : Upload and Batch Producer Process](resources/docs/activity-diagram-batch-upload-producer.svg)

Following is the activity diagram for the batch consumer process

![Activity Diagram : Upload and Batch Consumer Process](resources/docs/activity-diagram-batch-upload-consumer.svg)

#### Retrieve Records API
Following is the activity diagram for the retrieve records of the transactions made (that was read from the **🧾 CSV** file)

![Activity Diagram : Retrieve Process](resources/docs/activity-diagram-retrieve.svg)

#### Update Records API
Following is the activity diagram for the update records of the transactions made (that was read from the **🧾 CSV** file)

![Activity Diagram : Update and Batch Producer Process](resources/docs/activity-diagram-batch-update-producer.svg)

Following is the activity diagram for the batch consumer update process

![Activity Diagram : Update and Batch Consumer Process](resources/docs/activity-diagram-batch-update-consumer.svg)

## 🚀 Running The Demo
Follow these steps below to be able to run the demo

### Pre-requisites

* Java 17+ 
* Maven
* Docker

### The [startup.sh](startup.sh)
This script will build the project [assignment-maybank](./) and starts all of the micro-services Docker containers

### The [shutdown.sh](shutdown.sh)
This script will stop the running micro-services Docker containers

### The [clean.sh](clean.sh)
This script will clean up all micro-services Docker containers and images as well as all database data files (this basically starts "*like-fresh*")


# History
## 2024-10-09
* Commit & pushed initial source to personal GitHub
* Added all documentation works

## 2024-10-05
* Kick start analysis, design & development

## 2024-10-03
* Received assessment assignment