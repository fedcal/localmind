#!/usr/bin/env python3
"""
Prepare training dataset from document IDs.
Prepara il dataset di training a partire dagli ID dei documenti.

Usage / Utilizzo:
    python prepare_dataset.py --dataset-id <id> --doc-ids <id1,id2,...> --output-dir <dir>
"""

import argparse
import json
import os
import sys


def main():
    parser = argparse.ArgumentParser(description="Prepare training dataset / Prepara dataset di training")
    parser.add_argument("--dataset-id", required=True, help="Dataset ID / ID del dataset")
    parser.add_argument("--doc-ids", required=True, help="Comma-separated document IDs / ID documenti separati da virgola")
    parser.add_argument("--output-dir", required=True, help="Output directory / Directory di output")
    args = parser.parse_args()

    dataset_dir = os.path.join(args.output_dir, args.dataset_id)
    os.makedirs(dataset_dir, exist_ok=True)

    doc_ids = args.doc_ids.split(",")

    # Placeholder: in production, fetch document content from database
    # Placeholder: in produzione, recupera il contenuto dei documenti dal database
    samples = []
    for doc_id in doc_ids:
        sample = {
            "messages": [
                {"role": "system", "content": "You are a helpful assistant trained on local documents."},
                {"role": "user", "content": f"Document {doc_id} content placeholder"},
                {"role": "assistant", "content": f"Response based on document {doc_id}"}
            ]
        }
        samples.append(sample)

    dataset_path = os.path.join(dataset_dir, "dataset.jsonl")
    with open(dataset_path, "w", encoding="utf-8") as f:
        for sample in samples:
            f.write(json.dumps(sample, ensure_ascii=False) + "\n")

    result = {
        "path": dataset_path,
        "sample_count": len(samples),
        "format": "JSONL"
    }
    print(json.dumps(result))


if __name__ == "__main__":
    main()
