# CSCI 4176 API #

## WARNING ##
This is a non-production api. Any change you make here will not affect the real api.
The real api is hosted behind AWS! This requires you to have my AWS permissions so I can't share it, but feel free to look at the ~~spaghetti~~ code that mirrors the prod line.

## Tables of Content ##
1. Before Getting Started
    a. Acknowledgement
    b. Endpoint
        i. Alpha (Pre-release)
    c. Infrastructure
    d. API Authentication and Authorization
    e. Installing Dependencies
    f. Zipping for AWS Lambda
2. User-Based API Routes
    a. Create User
    b. Authenticate User
    c. Update Belbin Traits
    d. Get User Data
3. Course-based API Routes
    a. Create Class
    b. Assign User to Class
    c. Get Class Data
    d. Start Class (Generate teams)

## Before Getting Started ##

### Acknowledgement ###
This API is created for TeamMake, a Dalhousie University Project for the completion of CSCI 4176.

### Endpoint ###
#### Alpha (Pre-release) ####
https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/

### Infrastructure ###
* AWS API Gateway
    * An API Creation Tool Hosted by AWS to open REST API endpoints pointing at other AWS services
* AWS Lambda
    * A serverless function hosted by AWS to carry out simple tasks
* AWS DynamoDB
    * A table-based atomic database hosted by AWS

### API Authentication and Authorization ###
*Open to all public for now*

### Installing Dependencies ###
```
pip install --platform manylinux2014_x86_64 --target=package --implementation cp --python-version 3.11 --only-binary=:all: --upgrade --target ./ <package>
```

### Zipping for AWS Lambda ###
```
Compress-Archive -Path ./* -DestinationPath deployment.zip -Force
```

## User-based API Routes ##

### Create User ###
* Endpoint: https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/create-user
* Method: POST

* Input:

| **Input**    | **Type** | **Description**          | **Required?** |
|--------------|----------|--------------------------|---------------|
| email        | String   | Email of the user        | Yes           |
| password     | String   | Password of the user     | Yes           |
| phone_number | String   | Phone Number of the user | No            |

* Output:

| **Output**      | **Type** | **Description**  | **Required?** |
|-----------------|----------|------------------|---------------|
| status          | Integer  | HTTP Status      | Yes           |
| body["message"] | String   | Success Message  | Yes           |
| body["id"]      | String   | User's Unique ID | Yes           |

* Error Codes
    * 409: User email already exists
    * 500: Missing field or generic internal server error

### Authenticate User ###
* Endpoint: https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/authenticate-user
* Method: POST

* Input:

| **Input**    | **Type** | **Description**          | **Required?** |
|--------------|----------|--------------------------|---------------|
| email        | String   | Email of the user        | Yes           |
| password     | String   | Password of the user     | Yes           |

* Output:

| **Output**      | **Type** | **Description**  | **Required?** |
|-----------------|----------|------------------|---------------|
| status          | Integer  | HTTP Status      | Yes           |
| body["message"] | String   | Success Message  | Yes           |
| body["id"]      | String   | User's Unique ID | Yes           |

* Error Codes
    * 401: Wrong password
    * 404: User not found
    * 500: Missing field or generic internal server error

### Update Belbin Traits ###
* Endpoint: https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/update-belbin-traits
* Method: POST

* Input:

| **Input**     | **Type** | **Description**           | **Required?** |
|---------------|----------|---------------------------|---------------|
| user_id       | String   | ID of the user            | Yes           |
| belbin_traits | Map      | Belbin traits of the user | Yes           |

... where Belbin Traits are organized like:
```json
"belbin_traits": {
    "resource_investigator": <number>,
    "teamworker": <number>,
    "coordinator": <number>,
    "plant": <number>,
    "monitor_evaluator": <number>,
    "specialist": <number>,
    "shaper": <number>,
    "implementer": <number>,
    "completer_finisher": <number>
  }
```

* Output:

| **Output**      | **Type** | **Description**  | **Required?** |
|-----------------|----------|------------------|---------------|
| status          | Integer  | HTTP Status      | Yes           |
| body["message"] | String   | Success Message  | Yes           |

* Error Codes
    * 500: Missing field or generic internal server error

### Get User Data ###
* Endpoint: https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/get-user-data
* Method: POST

* Input:

| **Input**    | **Type** | **Description**          | **Required?** |
|--------------|----------|--------------------------|---------------|
| user_id      | String   | ID of the user           | Yes           |

* Output:

| **Output**                       | **Type**          | **Description**               | **Required?** |
|----------------------------------|-------------------|-------------------------------|---------------|
| status                           | Integer           | HTTP Status                   | Yes           |
| body["message"]                  | String            | Success Message               | Yes           |
| body["info"]["id"]               | String            | User's Unique ID              | Yes           |
| body["info"]["email"]            | String            | User's Email                  | Yes           |
| body["info"]["belbin_traits"]    | Map               | Belbin Traits                 | Yes           |
| body["id"]["registered_classes"] | List<Integer>     | List of registered class' IDs | Yes           |
| body["id"]["owned_classes"]      | List<Integer>     | List of classes the user organizes | Yes           |

... where Belbin Traits are organized like:
```json
"belbin_traits": {
    "resource_investigator": <number>,
    "teamworker": <number>,
    "coordinator": <number>,
    "plant": <number>,
    "monitor_evaluator": <number>,
    "specialist": <number>,
    "shaper": <number>,
    "implementer": <number>,
    "completer_finisher": <number>
  }
```

* Error Codes
    * 404: User not found
    * 500: Missing field or generic internal server error

## Course-Based API Routes ##

### Create Class ###
* Endpoint: https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/create-class
* Method: POST

* Input:

| **Input**       | **Type** | **Description**          | **Required?** |
|-----------------|----------|--------------------------|---------------|
| class_organizer | String   | ID of the organizer      | Yes           |
| class_name      | String   | Name of the class        | Yes           |
| class_password  | String   | Class password           | Yes           |

* Output:

| **Output**      | **Type**  | **Description**     | **Required?** |
|-----------------|-----------|---------------------|---------------|
| status          | Integer   | HTTP Status         | Yes           |
| body["message"] | String    | Success Message     | Yes           |
| body["id"]      | Integer   | Class password      | Yes           |

* Error Codes
    * 500: Missing field or generic internal server error


### Assign User to Class ###
* Endpoint: https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/assign-user-to-class
* Method: POST

* Input:

| **Input**       | **Type** | **Description**          | **Required?** |
|-----------------|----------|--------------------------|---------------|
| class_id        | Integer  | Class ID                 | Yes           |
| user_id         | String   | User ID                  | Yes           |
| class_password  | String   | Class password           | Yes           |

* Output:

| **Output**      | **Type**  | **Description**          | **Required?** |
|-----------------|-----------|--------------------------|---------------|
| status          | Integer   | HTTP Status              | Yes           |
| body["message"] | String    | Success Message          | Yes           |
| body["id"]      | Integer   | Class ID                 | Yes           |

* Error Codes
    * 500: Missing field or generic internal server error

### Get Class Data ###
* Endpoint: https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/get-class-data
* Method: POST

* Input:

| **Input**       | **Type**  | **Description**          | **Required?** |
|-----------------|-----------|--------------------------|---------------|
| class_id        | Integer   | ID of the class          | Yes           |

* Output:

| **Output**                      | **Type**  | **Description**                    | **Required?** |
|---------------------------------|-----------|------------------------------------|---------------|
| status                          | Integer   | ID of the organizer                | Yes           |
| body["message"]                 | String    | Success Message                    | Yes           |
| body["info"]["id"]              | Integer   | Class ID                           | Yes           |
| body["info"]["class_organizer"] | String    | User ID of who organizes the class | Yes           |
| body["info"]["students"]        | List      | List of enrolled students' ID      | Yes           |
| body["info"]["teams"]           | Map       | Map of the generated teams         | Yes           |
| body["info"]["class_name"]      | String    | The name of this class             | Yes           |

...where the map of the generated teams is:
```json
"teams": {
    "0": [
        "b07c0ba6-4684-4f0a-a590-9e0b42727e6d"
    ],
    "1": [
        "..."
    ],
    "2": [
        "..."
    ],...
}

```
* Error Codes
    * 404: Class not found
    * 500: Missing field or generic internal server error

### Start Class ###
* Endpoint: https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/start-class
* Method: POST

* Input:

| **Input**       | **Type** | **Description**          | **Required?** |
|-----------------|----------|--------------------------|---------------|
| class_id        | Integer  | Class ID                 | Yes           |
| max_team_size   | Integer  | Max size of each team    | Yes           |

* Output:

| **Output**      | **Type**  | **Description**          | **Required?** |
|-----------------|-----------|--------------------------|---------------|
| status          | Integer   | HTTP Status              | Yes           |
| body["message"] | String    | Success Message          | Yes           |

* Error Codes
    * 500: Missing field or generic internal server error