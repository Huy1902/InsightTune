import pandas as pd
import subprocess
import argparse
import shlex
import os

def main():
    # Parse command-line arguments
    parser = argparse.ArgumentParser(description="Download Spotify tracks with metadata using spotdl.")
    parser.add_argument("csv_file", help="Path to the CSV file with 'track_name' and 'artist_name' columns.")
    parser.add_argument("--output_dir", default="downloads", help="Directory to save downloaded tracks (default: downloads).")
    args = parser.parse_args()

    # Load the DataFrame from CSV
    try:
        df = pd.read_csv(args.csv_file)
    except Exception as e:
        raise ValueError(f"Failed to read CSV file: {e}")

    # Ensure required columns exist
    if 'track_name' not in df.columns or 'artist_name' not in df.columns:
        raise ValueError("CSV must contain 'track_name' and 'artist_name' columns.")

    # Create output directory if it doesn't exist
    os.makedirs(args.output_dir, exist_ok=True)

    # Iterate over each row and download
    for index, row in df.iterrows():
        track = row['track_name']
        artist = row['artist_name']
        query = f"{artist} {track}"
        print(f"Downloading: {query}")

        # Use shlex.quote to safely escape the query
        safe_query = shlex.quote(query)
        # Construct spotdl command
        cmd = f"spotdl download {safe_query} --output {shlex.quote(args.output_dir)} --format mp3"
        try:
            result = subprocess.run(shlex.split(cmd), capture_output=True, text=True, check=True)
            print(f"Success: {query} downloaded with metadata.")
            print(result.stdout)
        except subprocess.CalledProcessError as e:
            print(f"Error downloading {query}: {e.stderr}")
            continue
        except ValueError as e:
            print(f"Error parsing command for {query}: {e}")
            continue

if __name__ == "__main__":
    main()