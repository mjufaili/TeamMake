import json
import boto3
import math

# Student class
class Student:
    def __init__(self, id, belbin_traits):
        self.id = id
        self.belbin_traits = belbin_traits

# Initialize the DynamoDB client
dynamodb = boto3.client('dynamodb')

# Define the name of your DynamoDB table
user_table = 'user_table'
class_table = 'class_table'

# Stacks
trait_stacks = {
    "coordinator": [], 
    "resource_investigator": [], 
    "specialist": [], 
    "shaper": [], 
    "plant": [], 
    "teamworker": [], 
    "implementer": [], 
    "monitor_evaluator": [], 
    "completer_finisher": [],
    "wild_card": []
}

def lambda_handler(event, context):
    try:
        # Extract class data
        class_id = str(event['class_id'])
        max_team_size = event['max_team_size']

        # Retrieve user data from DynamoDB based on email
        class_data = get_class_data(class_id)
        
        if class_data:

            students = []
            
            for student in list(class_data['students']['L']):
                id = student['S']
                if "belbin_traits" in get_user_data(id):
                    traits = get_user_data(id)['belbin_traits']['M']
                else:
                    traits = {"wild_card": {"N": 1}}
                belbin = {}
                for trait in traits:
                    belbin[trait] = int(traits[trait]["N"])
                students.append(Student(id, belbin))
            
            teams = distribute(students, max_team_size)

            query = "SET teams = :teams"
        
            # Update the item in DynamoDB
            dynamodb.update_item(TableName=class_table, 
                Key={'id': {'N': class_id}}, 
                UpdateExpression=query,
                ExpressionAttributeValues={':teams': {'M': teams}}
            )

            body = {
                'message': 'Success', 
                'teams': teams
            }

            response = {
                'status': 200,
                'body': body
            }

        else:
            response = {
                'status': 404,
                'body': {'message': 'Class not found'}
            }

    except Exception as e:
        # Handle any errors and return an error response
        response = {
            'status': 500,
            'body': {'error': str(e)}
        }
    
    return response

def get_user_data(user_id):
    # Retrieve user data from DynamoDB based on id
    response = dynamodb.scan(
        TableName=user_table,
        FilterExpression='id = :id',
        ExpressionAttributeValues={':id': {'S': user_id}}
    )
    
    items = response.get('Items', [])
    
    if items:
        return items[0]
    else:
        return None
    
def get_class_data(class_id):
    # Retrieve class data from DynamoDB based on id
    response = dynamodb.scan(
        TableName=class_table,
        FilterExpression='id = :id',
        ExpressionAttributeValues={':id': {'N': class_id}}
    )
    
    items = response.get('Items', [])
    
    if items:
        return items[0]
    else:
        return None

def find_best_trait(student, disallowed_traits = []):
    best_trait_rank = -1
    best_trait = None
    for trait in student.belbin_traits:
        if best_trait_rank < student.belbin_traits[trait] and trait not in disallowed_traits:
            best_trait = trait
            best_trait_rank = student.belbin_traits[trait]
    return best_trait

def distribute(students, max_team_size):
    teams = {i:[] for i in range(math.ceil(len(students)/max_team_size))}

    # Prime stacks
    for student in students:
        trait_stacks[find_best_trait(student)].append(student)

    # Sort stacks
    for trait in trait_stacks:
        trait_stacks[trait].sort(key=lambda student: student.belbin_traits[trait])

    # Belbin Distribution
    enrolled_count = 0
    current_team = 0
    while enrolled_count < len(students):
        for trait in trait_stacks:
            if len(trait_stacks[trait]) > 0:
                student = trait_stacks[trait].pop()
                if len(teams[current_team]) == max_team_size:
                    current_team += 1
                teams[current_team].append(student)
                enrolled_count += 1

    # DDB Serialization
    serialized = {}
    for team in teams:
        serialized[str(team)] = {'L': []}
        for student in teams[team]:
            serialized[str(team)]['L'].append({'S': student.id})
        
    return serialized