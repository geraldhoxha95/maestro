#!/usr/bin/env python3
"""
Simple Python script that writes to a file.
This script demonstrates a basic task that can be executed in a Maestro workflow.
"""

import os
import sys
from datetime import datetime

def main():
    # Get output file path from command line argument or use default
    output_file = sys.argv[1] if len(sys.argv) > 1 else "output.txt"
    
    # Create output directory if it doesn't exist
    output_dir = os.path.dirname(output_file)
    if output_dir and not os.path.exists(output_dir):
        os.makedirs(output_dir, exist_ok=True)
    
    # Write some content to the file
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    message = f"Hello from Maestro!\nScript executed at: {timestamp}\n"
    
    with open(output_file, 'w') as f:
        f.write(message)
        f.write("This file was created by a Python script in a Maestro workflow.\n")
        f.write("Workflow execution completed successfully!\n")
    
    print(f"Successfully wrote to {output_file}")
    print(f"File location: {os.path.abspath(output_file)}")

if __name__ == "__main__":
    main()