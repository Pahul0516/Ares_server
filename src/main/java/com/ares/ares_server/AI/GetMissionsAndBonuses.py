import json
import random
import sys
from datetime import datetime
from huggingface_hub import InferenceClient
from dotenv import load_dotenv
import os


api_key = os.getenv("API_KEY_FROM_JAVA")

if not api_key:
    print("Error: API_KEY_FROM_JAVA not found in environment.", file=sys.stderr)
    sys.exit(1)

REPO_ID = "meta-llama/Llama-3.2-3B-Instruct"

client = InferenceClient(token=api_key)

system_prompt = """
You are an AI Game Master for a GPS game where you need to conquer zones from your city. Your goal is to generate a challenge for a player that will be sent by e-mail to them.

### RULES:
1. You must output Valid JSON only. Do not add conversational text.
2. You must choose ONE Challenge Condition and ONE Reward from the supported lists below. Do not pick the same challenge and reward often. Combine them.
3. Do not be very aggressive, give the challenge steps clearly.

### SUPPORTED CHALLENGE TYPES:
- COND_SHAPE: Zone must resemble the shape (target_value) given. You can choose from: star, square, circle, triangle, heart.
- COND_AREA: Player must enclose an area > target_value (m^2).
- COND_TIME: Player must finish creating the zone in < target_value (minutes).
- COND_INTERSECT: New zone must touch at least target_value (count) existing zones.
- COND_DISTANCE: New zone must be at least target_value (meters) away from own zones.

### SUPPORTED REWARD TYPES:
- REW_LOCK: Zone is immune to attacks for target_value (hours).
- REW_SCORE_MULT: Zone generates target_value (multiplier) points.
- REW_SHIELD: Zone has target_value (count) extra defense layers.

### OUTPUT FORMAT:
{
  "mission_name": "Creative Name",
  "flavor_text": "Short catchy passive-aggressive encouragement words.",
  "email_header": "Header of the e-mail",
  "email_text": "Long description of the challenge, including the type and target values"
  "challenge": {
      "type": "ONE_OF_COND_TYPES",
      "target_value": 100
  },
  "reward": {
      "type": "ONE_OF_REW_TYPES",
      "target_value": 2.0
  }
}
"""


def generate_mission():
    messages = [
        {"role": "system", "content": system_prompt},
    ]

    try:
        response = client.chat_completion(
            model=REPO_ID,
            messages=messages,
            max_tokens=500,
            temperature=1
        )

        content = response.choices[0].message.content

        if "```json" in content:
            content = content.split("```json")[1].split("```")[0].strip()
        elif "```" in content:
            content = content.split("```")[1].split("```")[0].strip()

        return json.loads(content)

    except Exception as e:
        print(f"Error generating mission: {e}")
        return None


mission_data = generate_mission()
print(json.dumps(mission_data, indent=2))