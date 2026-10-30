#!/usr/bin/env bash
# Cine Java — one-command run (Linux / macOS)
set -e
cd "$(dirname "$0")"
echo "Compiling..."
javac -d out $(find src -name "*.java")
echo "Running... (demo login: demo / demo123)"
echo
java -cp out cine.Main
