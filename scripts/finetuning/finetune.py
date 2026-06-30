#!/usr/bin/env python3
"""
Fine-tune a model using the prepared dataset.
Esegui il fine-tuning di un modello usando il dataset preparato.

Usage / Utilizzo:
    python finetune.py --job-id <id> --base-model <model> --technique <LORA|QLORA|FULL> \
                       --dataset-path <path> --output-dir <dir>
"""

import argparse
import json
import os
import sys
import time


def update_status(status_dir, status, progress, error=None):
    """Update the status file for the job / Aggiorna il file di stato del job."""
    os.makedirs(status_dir, exist_ok=True)
    status_data = {
        "status": status,
        "progress": progress
    }
    if error:
        status_data["error"] = error
    status_path = os.path.join(status_dir, "status.json")
    with open(status_path, "w", encoding="utf-8") as f:
        json.dump(status_data, f)


def main():
    parser = argparse.ArgumentParser(description="Fine-tune model / Fine-tuning del modello")
    parser.add_argument("--job-id", required=True, help="Job ID / ID del job")
    parser.add_argument("--base-model", required=True, help="Base model name / Nome modello base")
    parser.add_argument("--technique", required=True, choices=["LORA", "QLORA", "FULL"],
                        help="Training technique / Tecnica di training")
    parser.add_argument("--dataset-path", required=True, help="Path to dataset JSONL / Percorso del dataset JSONL")
    parser.add_argument("--output-dir", required=True, help="Output directory / Directory di output")
    args = parser.parse_args()

    status_dir = os.path.join(args.output_dir, args.job_id)

    try:
        # Verify dataset exists / Verifica che il dataset esista
        if not os.path.exists(args.dataset_path):
            update_status(status_dir, "FAILED", 0, f"Dataset not found: {args.dataset_path}")
            sys.exit(1)

        update_status(status_dir, "PREPARING", 0)

        # Placeholder: in production, use transformers/peft for actual fine-tuning
        # Placeholder: in produzione, usare transformers/peft per il fine-tuning reale
        update_status(status_dir, "TRAINING", 10)

        # Simulate training progress / Simula il progresso del training
        for progress in range(20, 100, 10):
            time.sleep(1)
            update_status(status_dir, "TRAINING", progress)

        # Save output model info / Salva le info del modello di output
        output_model_dir = os.path.join(status_dir, "model")
        os.makedirs(output_model_dir, exist_ok=True)

        model_info = {
            "base_model": args.base_model,
            "technique": args.technique,
            "adapter_path": output_model_dir
        }
        with open(os.path.join(output_model_dir, "model_info.json"), "w", encoding="utf-8") as f:
            json.dump(model_info, f)

        update_status(status_dir, "COMPLETED", 100)

        result = {
            "status": "COMPLETED",
            "model_path": output_model_dir
        }
        print(json.dumps(result))

    except Exception as e:
        update_status(status_dir, "FAILED", 0, str(e))
        print(json.dumps({"status": "FAILED", "error": str(e)}), file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
