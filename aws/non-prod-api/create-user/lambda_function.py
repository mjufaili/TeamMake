import json
import boto3
import bcrypt
import uuid

# Initialize the DynamoDB client
dynamodb = boto3.client('dynamodb')

# Define the name of your DynamoDB table
table_name = 'user_table'

def lambda_handler(event, context):
    try:
        # Extract user data
        email = event['email']
        phone_number = event.get('phone_number', None)  # Optional field
        password = event['password']
        
        # Check if a user with the same email already exists
        if user_exists(email):
            raise Exception('User with the same email already exists')
        
        # Generate a unique ID for the user
        user_id = str(uuid.uuid4())
        
        # Hash the provided password
        hashed_password = hash_password(password)
        
        # Create an item to put into DynamoDB
        user_item = {
            'id': {'S': user_id},
            'email': {'S': email},
            'hash_password': {'S': hashed_password},
            'registered_classes': {'L': []}
        }
        
        if phone_number:
            user_item['phone_number'] = {'S': phone_number}
        
        # Put the user item into DynamoDB
        dynamodb.put_item(TableName=table_name, Item=user_item)
        
        # Return a successful response
        response = {
            'status': 200,
            'body': {'message': 'User added successfully', 'id': user_id}
        }
    except Exception as e:
        # Handle specific errors and return an error response
        error_message = str(e)
        if 'already exists' in error_message.lower():
            response = {
                'status': 409,  # Conflict
                'body': {'error': error_message}
            }
        else:
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

def user_exists(email):
    # Check if a user with the same email already exists
    response = dynamodb.scan(
        TableName=table_name,
        FilterExpression='email = :email',
        ExpressionAttributeValues={':email': {'S': email}}
    )
    
    return len(response.get('Items', [])) > 0
