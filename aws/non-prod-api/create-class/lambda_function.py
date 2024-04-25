import json
import boto3
import bcrypt
import uuid
import random

# Initialize the DynamoDB client
dynamodb = boto3.client('dynamodb')

# Define the name of your DynamoDB table
table_name = 'class_table'

def lambda_handler(event, context):
    try:
        # Extract class data
        password = event['class_password']
        class_name = event['class_name']

        # Extract user data
        class_organizer = event['class_organizer']
    
        # Generate a unique ID for the class
        class_id = unique_id()
        
        # Hash the provided password
        hashed_password = hash_password(password)
        
        # Create an item to put into DynamoDB
        user_item = {
            'id': {'N': str(class_id)},
            'hash_password': {'S': hashed_password},
            'class_organizer': {'S': class_organizer},
            'class_name': {'S': class_name},
            'students': {'L': []}
        }
        
        # Put the user item into DynamoDB
        dynamodb.put_item(TableName=table_name, Item=user_item)
        
        # Return a successful response
        response = {
            'status': 200,
            'body': {'message': 'Class created successfully', 'id': class_id}
        }
    except Exception as e:
        # Handle specific errors and return an error response
        error_message = str(e)
        response = {
            'status': 500,
            'body': {'error': error_message}
        }
    
    return response

def hash_password(plain_password):
    # Hash the password using bcrypt
    salt = bcrypt.gensalt()
    hashed_password = bcrypt.hashpw(plain_password.encode('utf-8'), salt)
    return hashed_password.decode('utf-8')

def unique_id():
    # Create new unique random id by checking against table
    random_id = None
    while True:
        random_id = random.randint(1,999999)
        response = dynamodb.scan(
            TableName=table_name,
            FilterExpression='id = :id',
            ExpressionAttributeValues={':id': {'N': str(random_id)}}
        )
        if len(response.get('Items', [])) == 0:
            break
    
    return random_id