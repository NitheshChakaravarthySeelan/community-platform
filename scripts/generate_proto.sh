#!/bin/bash

# This script generates Protobuf code for different languages.
# Usage: ./scripts/generate_proto.sh <language> <service_path> <proto_file>

LANGUAGE=$1
SERVICE_PATH=$2
PROTO_FILE=$3
PROTO_DIR="shared/proto"

if [ -z "$LANGUAGE" ] || [ -z "$SERVICE_PATH" ] || [ -z "$PROTO_FILE" ]; then
  echo "Usage: $0 <language> <service_path> <proto_file>"
  exit 1
fi

echo "Generating Protobuf for $LANGUAGE in $SERVICE_PATH from $PROTO_FILE"

case "$LANGUAGE" in
  java) 
    # Java generation is handled by Maven plugin
    echo "Java generation is handled by Maven plugin. Skipping direct protoc call."
    ;;
  python)
    # Python generation
    python3 -m grpc_tools.protoc \
      -I"$PROTO_DIR" \
      --python_out="$SERVICE_PATH" \
      --grpc_python_out="$SERVICE_PATH" \
      "$PROTO_DIR/$PROTO_FILE"
    ;;
  go)
    # Go generation
    # Ensure Go binaries are in PATH
    export PATH=$PATH:$HOME/go/bin
    protoc \
      -I"$PROTO_DIR" \
      --go_out="$SERVICE_PATH" \
      --go_opt=paths=source_relative \
      --go-grpc_out="$SERVICE_PATH" \
      --go-grpc_opt=paths=source_relative \
      "$PROTO_DIR/$PROTO_FILE"
    ;;
  typescript)
    # TypeScript generation
    grpc_tools_node_protoc \
      -I "$PROTO_DIR" \
      --plugin=protoc-gen-ts=./node_modules/.bin/protoc-gen-ts \
      --js_out=import_style=commonjs,binary:"$SERVICE_PATH" \
      --ts_out=grpc_js:"$SERVICE_PATH" \
      "$PROTO_DIR/$PROTO_FILE"
    ;;
  *)
    echo "Unsupported language: $LANGUAGE"
    exit 1
    ;;
esac

echo "Protobuf generation for $LANGUAGE completed."
