# server/transcribe.py
import asyncio
import json
from api import create_api_instance

VOICE_TRANSCRIPTION_PROMPT = """
You are given an audio file containing spoken content. Your task is to transcribe the audio accurately using the following guidelines:
<Transcription Requirements>
   - Verbatim Transcription: Provide a transcription that captures the audio exactly as spoken. Include all words, filler sounds (e.g., "um," "uh"), and non-speech cues (e.g., `[laughter]`, `[applause]`).
   - Uncertainty Marking: If any words or phrases are unclear, mark them with `[?]`.
   - Speaker Identification: If multiple speakers are present, label each speaker as "Speaker 1", "Speaker 2", etc. Begin each speaker's turn on a new line. Use timestamps at the start of each speaker turn if possible (e.g., `[00:02:15]`).
</Transcription Requirements>
<Refined Version>
   - Provide a second version of the transcription where you correct any obvious grammatical errors and punctuation mistakes.
   - Remove unnecessary filler words while preserving the core content and meaning.
   - Ensure the structure (speaker labels, timestamps) remains clear and consistent.
</Refined Version>
<Output Format>
   - Present the transcription in a clear and organized manner using JSON.
   - Separate the output into two distinct sections: one for the raw (verbatim) transcription and one for the refined version.
</Output Format>
<Example>
{
  "raw_transcription": "[00:00:00] Speaker 1: Hello, everyone, um, welcome to the session.\n[00:00:05] Speaker 2: Thank you, it's great to be here, uh, really excited to talk about this.",
  "refined_transcription": "Speaker 1: Hello, everyone. Welcome to the session.\nSpeaker 2: Thank you; it's great to be here. I'm really excited to discuss this."
}
</Example>
"""

def transcribe_audio(audio_file: str, api_service: str = "openai") -> str:
    """
    Transcribes an audio file using the specified API service.
    
    Args:
        audio_file (str): Path to the audio file.
        api_service (str, optional): The API service to use. Defaults to "openai".
    
    Returns:
        str: The refined transcription if available, otherwise the raw transcription.
    
    Raises:
        Exception: If the audio processing or JSON parsing fails.
    """
    api = create_api_instance(api_service)
    
    try:
        # Process the audio asynchronously.
        response = asyncio.run(api.process_audio(VOICE_TRANSCRIPTION_PROMPT, audio_file))
        print("API Response:", response)
    except Exception as e:
        raise Exception(f"Failed to process audio file '{audio_file}': {e}")
    
    try:
        transcription = json.loads(response)
    except json.JSONDecodeError as e:
        raise Exception(f"Invalid JSON response: {e}")
    
    # Return the refined transcription if available; otherwise, return the raw transcription.
    return transcription.get("refined_transcription", transcription.get("raw_transcription", "Transcription not available."))

if __name__ == "__main__":
    audio_path = "audio_sample.wav"
    try:
        result = transcribe_audio(audio_file=audio_path)
        print("Transcription Result:")
        print(result)
    except Exception as error:
        print(f"An error occurred: {error}")
