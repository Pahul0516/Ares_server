import json
import random
from datetime import datetime
from huggingface_hub import InferenceClient

HF_API_KEY = "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"

REPO_ID = "meta-llama/Llama-3.2-3B-Instruct"  # Excellent performance, very smart

client = InferenceClient(token=HF_API_KEY)

# 1. Define the Game Context (Inputs)
# This is the data your app sends to the server when a player starts a session.
player_context = {
    "player_id": "runner_01",
    "level": 8,  # Advanced player
    "play_style": "Strategist",  # Prefers planning over sprinting
    "recent_history": "conquered_3_zones",  # AI should try to motivate them
    "current_conditions": {
        "time": "13:00",
        "weather": "Sunny",
        "location_type": "Park"  # Dense area
    }
}

# 2. The System Prompt
# We instruct the AI to act as a Game Master and strictly adhere to a list of allowed IDs.
system_prompt = """
You are an AI Game Master for a GPS strategy game. Your goal is to generate a 'Mission Contract' for a player based on their context.

### RULES:
1. You must output Valid JSON only. Do not add conversational text.
2. You must choose ONE Challenge Condition and ONE Reward from the supported lists below. Do not pick the same challenge and reward often. Combine them.
3. Balance the difficulty: If weather is bad, make the challenge easier. If player level is high, make it harder.

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


# 3. Generate the Mission
def generate_mission(context):
    # Construct the message payload for the chat model
    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": f"CURRENT PLAYER CONTEXT:\n{json.dumps(context)}"}
    ]

    try:
        # Call the Hugging Face Inference API
        response = client.chat_completion(
            model=REPO_ID,
            messages=messages,
            max_tokens=500,
            temperature=1
        )

        content = response.choices[0].message.content

        # Cleanup: Smaller models often wrap response in markdown blocks (```json ... ```)
        # We strip them to ensure json.loads works
        if "```json" in content:
            content = content.split("```json")[1].split("```")[0].strip()
        elif "```" in content:
            content = content.split("```")[1].split("```")[0].strip()

        return json.loads(content)

    except Exception as e:
        print(f"Error generating mission: {e}")
        # In a real app, you might print the raw content here to debug if JSON parsing failed
        return None


#for i in range(0, 5):
mission_data = generate_mission(player_context)
print(json.dumps(mission_data, indent=2))