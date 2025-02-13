# api/openai_api.py
import asyncio
from api.api import API
from api import register_api
from openai import OpenAI
import json


@register_api("openai")
class OpenAIAPI(API):
    """
    Concrete class for interactions with the OpenAI API.
    """

    def __init__(self, api_key=None):
        """
        Initializes the OpenAI API object.

        :param api_key: Can be either an actual API key string or
                        a path to a file containing the API key.
        """
        super().__init__(api_key, api_env="OPENAI_API_KEY")
        self.client = OpenAI(api_key=self.api_key)
        # If we don’t have a key or a client, raise an error.
        if not self.api_key or not self.client:
            raise ValueError(
                "No valid OpenAI API key found. Provide it as a string, file path, "
                "or set OPENAI_API_KEY in the environment."
            )

    async def process_text(
        self,
        prompt,
        model="chatgpt-4o-latest",
        max_tokens=8192,
        temperature=1.0,
        timeout=10,
        **kwargs,
    ):
        """
        Generates text using the OpenAI API.

        Args:
            prompt (str): The input prompt for text generation.
            model (str): The OpenAI model to use.
            max_tokens (int): The maximum number of tokens for the generated text.
            temperature (float): The sampling temperature.
            timeout (int): Timeout in seconds for the API call.
            **kwargs: Additional keyword arguments for the API call.

        Returns:
            str: The generated text, or None if an error occurred.
        """
        # Convert a plain string prompt into a "system" message.
        # If `prompt` is a list, assume it's already in the correct chat format.
        if isinstance(prompt, str):
            messages = [{"role": "system", "content": prompt}]
        else:
            if not isinstance(prompt, list):
                raise TypeError(
                    "Prompt must be either a string or a list of messages (JSON)."
                )
            messages = prompt

        try:
            response = self.client.chat.completions.create(
                model=model,
                messages=messages,
                stream=False,
                max_tokens=max_tokens,
                temperature=temperature,
                **kwargs,
            )
            generated_text = response.choices[0].message.content
            return generated_text
        except Exception as e:
            print(f"An error occurred while generating text: {e}")
            return None

    async def process_audio(
        self,
        prompt: str,
        audio_file: str,
        **kwargs,
    ):
        """
        Generates speech to text using the OpenAI API.

        Args:
            prompt (str): The input prompt for text generation.
            audio_file (str): Path to the audio file.
            **kwargs: Additional keyword arguments for the API call.

        Returns:
            str: The generated text, or None if an error occurred.
        """
        # Convert a plain string prompt into a "system" message.
        # If `prompt` is a list, assume it's already in the correct chat format.
        if not isinstance(prompt, str):
            raise TypeError("prompt must be string")

        audio_bytes = open(audio_file, "rb")
        try:
            transcription = self.client.audio.transcriptions.create(
                file=audio_bytes,
                # prompt=prompt, # the prompt is unnecessary, since it's handled by openai
                model="whisper-1",
                response_format="verbose_json",
                timestamp_granularities=kwargs.get("timestamp", []), #List[Literal['word', 'segment']]
            )
            # transcription items. duration, language, text, segments, words, task
            generated_text = transcription.text
            return json.dumps({"raw_transcription": generated_text})
        except Exception as e:
            print(f"An error occurred while generating text: {e}")
            return None

    def test_api(self):
        """
        A simple test method to verify the API setup by making a single request.
        """
        prompt = "You are a helpful assistant. What is the capital of France?"
        result = asyncio.run(self.generate_text(prompt))
        print("Test API result:", result)


if __name__ == "__main__":
    # Example: Supply a path to a file containing your key,
    # or just ensure OPENAI_API_KEY is set in your environment.
    api = OpenAIAPI("openai_api.key")
    api.test_api()
