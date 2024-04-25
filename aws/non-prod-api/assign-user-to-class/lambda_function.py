import json
import boto3
import bcrypt

# Initialize the DynamoDB client
dynamodb = boto3.client('dynamodb')

# Define the name of your DynamoDB table
table_name = 'class_table'
user_table_name = 'user_table'

def lambda_handler(event, context):
    try:
        # Extract class data
        class_id = str(event['class_id'])
        password = event['class_password']

        # Extract user data
        user_id = event['user_id']

        # Check if user is already registered
        if user_already_registered(user_id, class_id):
            response = {
                'status': 400,
                'body': {'message': 'User is already registered'}
            }   
            return response

        # Retrieve class data from DynamoDB based on email
        class_data = get_class_by_id(class_id)
        
        if class_data:
            # Verify the provided password against the stored hashed password
            if verify_password(password, class_data['hash_password']['S']):
                
                class_query = "SET students = list_append(students, :student)"
                user_query = "SET registered_classes = list_append(registered_classes, :class)"
        
                # Update the item in DynamoDB
                dynamodb.update_item(TableName=table_name, 
                             Key={'id': {'N': class_id}}, 
                             UpdateExpression=class_query,
                             ExpressionAttributeValues={':student': {'L': [{'S': user_id}]}}
                             )
                dynamodb.update_item(TableName=user_table_name, 
                             Key={'id': {'S': user_id}}, 
                             UpdateExpression=user_query,
                             ExpressionAttributeValues={':class': {'L': [{'N': class_id}]}}
                             )
                response = {
                    'status': 200,
                    'body': {'message': 'User added to class successfully', 'id': class_data['id']['N']}
                }
            else:
                response = {
                    'status': 401,
                    'body': {'message': 'Invalid password'}
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

def get_class_by_id(class_id):
    # Retrieve user data from DynamoDB based on email
    response = dynamodb.scan(
        TableName=table_name,
        FilterExpression='id = :id',
        ExpressionAttributeValues={':id': {'N': class_id}}
    )
    
    items = response.get('Items', [])
    
    if items:
        return items[0]
    else:
        return None

def verify_password(plain_password, hashed_password):
    # Verify the password using bcrypt
    return bcrypt.checkpw(plain_password.encode('utf-8'), hashed_password.encode('utf-8'))

def user_already_registered(user_id, class_id):
    # Retrieve user data from DynamoDB based on email
    response = dynamodb.scan(
        TableName=user_table_name,
        FilterExpression='id = :id',
        ExpressionAttributeValues={':id': {'S': user_id}}
    )
    
    user = response.get('Items', [])[0]
    if {'N': class_id} in list(user['registered_classes']['L']):
        return True
    else:
        return False