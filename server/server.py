from flask import Flask, request, jsonify
import os

app = Flask(__name__)

# Define a directory to save uploaded files.
UPLOAD_FOLDER = './uploads'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

@app.route('/upload', methods=['POST'])
def upload_file():
    # Check if the POST request has the file part
    if 'file' not in request.files:
        return jsonify({'error': 'No file provided'}), 400
    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400
    
    # Save the file to the upload folder.
    file_path = os.path.join(UPLOAD_FOLDER, file.filename)
    file.save(file_path)
    
    return jsonify({'message': f'File {file.filename} uploaded successfully'}), 200

if __name__ == '__main__':
    # Use host='0.0.0.0' to allow access from your local network.
    app.run(host='0.0.0.0', port=5000, debug=True)
