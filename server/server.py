# server/server.py
from flask import Flask, request, jsonify
import os
from transcribe import transcribe_audio  # Make sure this import matches your project structure

app = Flask(__name__)

# Define a directory to save uploaded files.
UPLOAD_FOLDER = './uploads'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

@app.route('/upload', methods=['POST'])
def upload_file():
    # Check if the POST request has the file part.
    if 'file' not in request.files:
        return jsonify({'error': 'No file provided'}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400

    # Save the file to the upload folder.
    file_path = os.path.join(UPLOAD_FOLDER, file.filename)
    file.save(file_path)

    try:
        # Process the audio file with the transcription function.
        transcription = transcribe_audio(audio_file=file_path, api_service="google")
    except Exception as e:
        return jsonify({'error': f'Error processing audio: {str(e)}'}), 500

    return jsonify({
        'message': f'File {file.filename} uploaded and processed successfully.',
        'transcription': transcription
    }), 200

if __name__ == '__main__':
    # Use host='0.0.0.0' to allow access from your local network.
    app.run(host='0.0.0.0', port=5000, debug=True)
