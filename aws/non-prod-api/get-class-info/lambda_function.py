import json
import boto3

# Initialize the DynamoDB client
dynamodb = boto3.client('dynamodb')

# Define the name of your DynamoDB table
table_name = 'class_table'

def lambda_handler(event, context):
    try:
        # Extract class data
        class_id = str(event['class_id'])

        # Retrieve class data from DynamoDB based on id
        class_data = get_class(class_id)
        
        if class_data:
            body = {
                'message': 'Success', 
                'id': class_data['id']['N'],
                'class_organizer': class_data['class_organizer']['S'],
                'class_name': class_data['class_name']['S'],
                'students': [student['S'] for student in class_data['students']['L']]
            }

            if "teams" in class_data:
                teams = class_data["teams"]["M"]
                serialized = {}
                for team in teams:
                    serialized[team] = [student["S"] for student in teams[team]["L"]]
                body["teams"] = serialized

            response = {
                'status': 200,
                'body': {'message': 'Success', "info" :body}
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

def get_class(class_id):
    # Retrieve class data from DynamoDB based on email
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