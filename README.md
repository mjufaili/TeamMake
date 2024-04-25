
# Duck Team #

Made in collaboration in completion for **CSCI 4176 - Mobile Computing**

  

## Developers ##
- Lydia Verboom - [Email](mailto:pr253146@dal.ca)
- Saahir Monowar - [Email](mailto:Saahir.Monowar@dal.ca)
- Andrew Cole - [Email](mailto:andrewcole@dal.ca)
- Al-Mahana Mahmood Al-Jufaili - [Email](mailto:al976777@dal.ca)
- Daniel Kang - [Email](mailto:danielkang@dal.ca)
- Maryam Talal Al-kindi - [Email](mailto:mr537079@dal.ca)
- Chushu Lin - [Email](mailto:ch940650@dal.ca)

## Tables of Content ##
* Getting Started
	* Acknowledgement
	* Motivations to use this app
	* Installation
	* Dependencies
	* Infrastructure
* API
	* Acknowledgement
	* Infrastructure
	* List of available endpoints
	
... Handoff to next iteration group

## Getting Started ##
### Acknowledgement ###
Presented in completion for CSCI 4176, Mobile Computing, Dalhousie University. 
Thanks to every group members and staffs that have contributed to it!
### Motivations ###
This project was initially inspired by the lack of team-making structure for many courses. For example, a teacher may either ask the students to assemble the teams themselves, or force the students into a group in a random matter. 

There are multiple ways to organize these teams in an intuitive way. 

For example, a class organizer may employ the "desire" ratings for creating the teams. This method asks the students to choose the letter grade they are aiming for, where students may choose A-range, B-range, and C-range. However, this has a critical error because many students would desire the higher grades as an incentive. 

Another method is to use an interest group. For example, a user may excel in a certain subject more than the other, or find passion than another individual. By balancing these interest, we can generate a balanced group. However, this also has an error because a class usually tests students for a single subject. Let's take this course as an example. People would usually answer "Native Programming" or "Mobile Development" in their interest groups, and less "DevOps" or "Cloud" because it is not a part of the material's scope.

The Belbin Tests comes to answer this dilemma through a psychological study. It works by asking the users to complete a set of questionnaires that places the user in the following categories:

1. Resource Investigator: *Uses their inquisitive nature to find ideas to bring back to the team.*
2. Teamworker: *Helps the team to gel, using their versatility to identify the work required and complete it on behalf of the team.*
3. Co-ordinator: *Needed to focus on the team's objectives, draw out team memebrs and delegate work appropriately.*
4. Plant: *Tends to be highly creative and good at solving problems in unconventional ways.*
5. Monitor Evaluator: *Provides a logical eye, making impartial judgements where required and weighs  the team's options in a dispassionate way.*
6. Specialist: *Brings in-depth knowledge of a key area to the team.*
7. Shaper: *Provides the necessary drive to ensure that the team keeps moving and does not lose focus on the momentum.*
8. Implementer: *Needed to plan a workable strategy and carry it out as efficiently as possible.*
9. Completer Finisher: *Most effectively used at the end of tasks to polish and scrutinize the work for others, subjecting it to the highest standards of quality control.*

These roles have their own strengths and weaknesses, and by creating groups with a balanced number of these traits should generate teams with the best chemistry. You can learn more about it in https://www.belbin.com/about/belbin-team-roles.
### Installation ###
This application is created using Android Studio "Giraffe". [https://developer.android.com/studio/install?gclid=Cj0KCQjw9fqnBhDSARIsAHlcQYSmK9RZg7sNFORI6r4C219z7vXPOYYNFQQk08D-1qNiLlXA57pzeDgaAuKMEALw_wcB&gclsrc=aw.ds](https://developer.android.com/studio/install?gclid=Cj0KCQjw9fqnBhDSARIsAHlcQYSmK9RZg7sNFORI6r4C219z7vXPOYYNFQQk08D-1qNiLlXA57pzeDgaAuKMEALw_wcB&gclsrc=aw.ds "https://developer.android.com/studio/install?gclid=Cj0KCQjw9fqnBhDSARIsAHlcQYSmK9RZg7sNFORI6r4C219z7vXPOYYNFQQk08D-1qNiLlXA57pzeDgaAuKMEALw_wcB&gclsrc=aw.ds")

1. Do a standard install
2. Choose a theme
3. Accept all

We use Gradle as our version set manager to manage the bunch of libraries we will use.
The version control system is GitLab hosted on Dalhousie. 
[https://git.cs.dal.ca/soonmoc/csci-4176-project](https://git.cs.dal.ca/soonmoc/csci-4176-project)

API level: 30
SDK Compiler level: 34
### Dependencies ###
Please refer to Gradle file to check and install the dependencies.
### Infrastructure ###
The front-end is served by Android Studio. Inside, you will find the `/app` directory, where you will find the application itself. 
In the `/aws` directory, you will find a **non-production** api, designed to mirror/clone the one that is in the production line. This API's endpoint meets at AWS API Gateway, which is linked to each individual AWS Lambda functions, where AWS DynamoDB is used to query the backend database. The reason we cannot give you access to production API is due to security concerns. The AWS account linked to the Lambdas is a personal account, which is why I will be supplying the directory with only the lambda_function.py file. 
If you want to test the production API, please check out the `readme.md` in [https://git.cs.dal.ca/soonmoc/csci-4176-project/-/tree/main/aws/non-prod-api](https://git.cs.dal.ca/soonmoc/csci-4176-project/-/tree/main/aws/non-prod-api). I have disabled token authentication and authorization for the scope of this project!
## API ##
For more details, please refer to the documentation available in: [https://git.cs.dal.ca/soonmoc/csci-4176-project/-/tree/main/aws/non-prod-api](https://git.cs.dal.ca/soonmoc/csci-4176-project/-/tree/main/aws/non-prod-api):
### Acknowledgement ###
This API is created for TeamMake, a Dalhousie University Project for the completion of CSCI 4176.
### Infrastructure ###
* AWS API Gateway

	* An API Creation Tool Hosted by AWS to open REST API endpoints pointing at other AWS services

* AWS Lambda

	* A serverless function hosted by AWS to carry out simple tasks

* AWS DynamoDB

	* A table-based atomic database hosted by AWS
### List of Available Endpoints ###
* User-based Endpoints:
	* Create User
	* Authenticate User
	* Update Belbin Traits
	* Get User Data
* Class-based Endpoints:
	* Class Class
	* Assign User to Class
	* Get Class Data
	* Start Class (Generate Teams)