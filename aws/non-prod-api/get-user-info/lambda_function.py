import json
import boto3

# Initialize the DynamoDB client
dynamodb = boto3.client('dynamodb')

# Define the name of your DynamoDB table
table_name = 'user_table'
class_table = 'class_table'

def lambda_handler(event, context):
    try:
        # Extract user login data
        user_id = event['user_id']

        # Retrieve user data from DynamoDB based on email
        user_data = get_user(user_id)
        
        if user_data:
            body = {
                'message': 'Success', 
                'id': user_data['id']['S'],
                'email': user_data['email']['S'],
                'registered_classes': [registered_class["N"] for registered_class in user_data['registered_classes']['L']],
                'owned_classes': get_classes_user_organizes(user_id)
            }

            # Belbin
            if "belbin_traits" in user_data:
                body["belbin_traits"] = {}
                body["belbin_traits"]["teamworker"] = int(user_data['belbin_traits']["M"]["teamworker"]['N'])
                body["belbin_traits"]["resource_investigator"] = int(user_data['belbin_traits']["M"]["resource_investigator"]['N'])
                body["belbin_traits"]["implementer"] = int(user_data['belbin_traits']["M"]["implementer"]['N'])
                body["belbin_traits"]["completer_finisher"] = int(user_data['belbin_traits']["M"]["completer_finisher"]['N'])
                body["belbin_traits"]["monitor_evaluator"] = int(user_data['belbin_traits']["M"]["monitor_evaluator"]['N'])
                body["belbin_traits"]["shaper"] = int(user_data['belbin_traits']["M"]["shaper"]['N'])
                body["belbin_traits"]["specialist"] = int(user_data['belbin_traits']["M"]["specialist"]['N'])
                body["belbin_traits"]["coordinator"] = int(user_data['belbin_traits']["M"]["coordinator"]['N'])
                body["belbin_traits"]["plant"] = int(user_data['belbin_traits']["M"]["plant"]['N'])

            # Phone
            if "phone_number" in user_data:
                body["phone_number"] = user_data["phone_number"]["S"]
            
            response = {
                'status': 200,
                'body': {'message': 'Success', "info" :body}
            }

        else:
            response = {
                'status': 404,
                'body': {'message': 'User not found'}
            }

    except Exception as e:
        # Handle any errors and return an error response
        response = {
            'status': 500,
            'body': {'error': str(e)}
        }
    
    return response

def get_user(user_id):
    # Retrieve user data from DynamoDB based on email
    response = dynamodb.scan(
        TableName=table_name,
        FilterExpression='id = :id',
        ExpressionAttributeValues={':id': {'S': user_id}}
    )
    
    items = response.get('Items', [])
    
    if items:
        return items[0]
    else:
        return None
    
def get_classes_user_organizes(user_id):
    response = dynamodb.scan(
        TableName=class_table,
        FilterExpression='class_organizer = :class_organizer',
        ExpressionAttributeValues={':class_organizer': {'S': user_id}}
    )

    items = response.get('Items', [])
    serialized = [owned_class['id']['N'] for owned_class in items]
    return serialized