#!/usr/bin/env python3
import os
import sys
import time
import urllib.request
import sqlite3
import json
import re

SOURCE_URL = "https://huggingface.co/datasets/cstr/de-wiktionary-sqlite/resolve/main/de_wiktionary.db"
TEMP_DB_PATH = "/tmp/de_wiktionary.db"
OUTPUT_DB_PATH = "app/src/main/assets/deutschar.db"

def download_source():
    if os.path.exists(TEMP_DB_PATH) and os.path.getsize(TEMP_DB_PATH) > 1000000000:
        print(f"Source file already exists at {TEMP_DB_PATH} ({os.path.getsize(TEMP_DB_PATH)/(1024*1024):.1f} MB)")
        return TEMP_DB_PATH

    print(f"Downloading de_wiktionary.db from {SOURCE_URL}...")
    req = urllib.request.Request(SOURCE_URL, headers={"User-Agent": "Mozilla/5.0 (AI Studio Agent)"})
    t0 = time.time()
    with urllib.request.urlopen(req) as resp, open(TEMP_DB_PATH, "wb") as out_f:
        total = 0
        last_log = time.time()
        while True:
            chunk = resp.read(1024 * 1024 * 8) # 8MB chunks
            if not chunk:
                break
            out_f.write(chunk)
            total += len(chunk)
            if time.time() - last_log > 5:
                elapsed = time.time() - t0
                speed = (total / (1024 * 1024)) / elapsed if elapsed > 0 else 0
                print(f"Downloaded {total / (1024 * 1024):.1f} MB ({speed:.1f} MB/s)...")
                last_log = time.time()
    
    elapsed = time.time() - t0
    print(f"Download complete: {os.path.getsize(TEMP_DB_PATH)/(1024*1024):.1f} MB in {elapsed:.1f}s")
    return TEMP_DB_PATH

if __name__ == "__main__":
    download_source()
