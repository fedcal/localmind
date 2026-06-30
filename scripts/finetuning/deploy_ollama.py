#!/usr/bin/env python3
"""
Deploy a fine-tuned model to Ollama.
Effettua il deploy di un modello fine-tuned su Ollama.

Usage / Utilizzo:
    python deploy_ollama.py --job-id <id> --model-name <name> --output-dir <dir>
"""

import argparse
import json
import os
import subprocess
import sys


def main():
    parser = argparse.ArgumentParser(description="Deploy model to Ollama / Deploy del modello su Ollama")
    parser.add_argument("--job-id", required=True, help="Job ID / ID del job")
    parser.add_argument("--model-name", required=True, help="Model name for Ollama / Nome del modello per Ollama")
    parser.add_argument("--output-dir", required=True, help="Output directory / Directory di output")
    args = parser.parse_args()

    model_dir = os.path.join(args.output_dir, args.job_id, "model")
    model_info_path = os.path.join(model_dir, "model_info.json")

    if not os.path.exists(model_info_path):
        print(json.dumps({"error": f"Model info not found: {model_info_path}"}), file=sys.stderr)
        sys.exit(1)

    with open(model_info_path, "r", encoding="utf-8") as f:
        model_info = json.load(f)

    base_model = model_info.get("base_model", "llama3.2")

    # Create Modelfile for Ollama / Crea il Modelfile per Ollama
    modelfile_path = os.path.join(model_dir, "Modelfile")
    with open(modelfile_path, "w", encoding="utf-8") as f:
        f.write(f"FROM {base_model}\n")
        f.write(f"# Fine-tuned model: {args.model_name}\n")
        f.write(f"# Technique: {model_info.get('technique', 'unknown')}\n")

        # Placeholder: in production, add ADAPTER directive for LoRA weights
        # Placeholder: in produzione, aggiungere la direttiva ADAPTER per i pesi LoRA
        adapter_path = model_info.get("adapter_path", "")
        if adapter_path and os.path.exists(os.path.join(adapter_path, "adapter_model.safetensors")):
            f.write(f"ADAPTER {adapter_path}\n")

    # Create model in Ollama / Crea il modello in Ollama
    try:
        result = subprocess.run(
            ["ollama", "create", args.model_name, "-f", modelfile_path],
            capture_output=True,
            text=True,
            timeout=600
        )

        if result.returncode != 0:
            print(json.dumps({
                "error": f"Ollama create failed: {result.stderr}"
            }), file=sys.stderr)
            sys.exit(1)

        output = {
            "model_name": args.model_name,
            "status": "deployed",
            "base_model": base_model
        }
        print(json.dumps(output))

    except FileNotFoundError:
        print(json.dumps({"error": "Ollama CLI not found. Please install Ollama."}), file=sys.stderr)
        sys.exit(1)
    except subprocess.TimeoutExpired:
        print(json.dumps({"error": "Model creation timed out after 600 seconds."}), file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
