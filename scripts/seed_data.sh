#!/bin/bash

# Database connection details
DB_URL="postgresql://admin:secret@localhost:5432/community_platform"

echo "🌱 Seeding CheckoutX Data..."

# Function to run psql
run_psql() {
    if command -v psql &> /dev/null; then
        psql "$DB_URL" "$@"
    else
        # Try running via docker exec
        sudo docker exec -i postgres_dev psql "postgresql://admin:secret@localhost:5432/community_platform" "$@"
    fi
}

# 1. Create a Test User
USER_ID="00000000-0000-0000-0000-000000000001"
echo "  -> Seeding user: testuser ($USER_ID)"
run_psql -c "INSERT INTO users (id, username, roles) VALUES ('$USER_ID', 'testuser', ARRAY['CUSTOMER']) ON CONFLICT (id) DO NOTHING;"

# 2. Create Test Products
PROD1_ID="11111111-1111-1111-1111-111111111111"
PROD2_ID="22222222-2222-2222-2222-222222222222"
PROD3_ID="33333333-3333-3333-3333-333333333333"

echo "  -> Seeding products..."
run_psql -c "INSERT INTO products (id, name, description, price, image_url) VALUES 
('$PROD1_ID', 'Gamer Keyboard', 'Mechanical keyboard with RGB', 99.99, 'https://example.com/keyboard.jpg'),
('$PROD2_ID', 'Gaming Mouse', 'Wireless mouse with high DPI', 59.99, 'https://example.com/mouse.jpg'),
('$PROD3_ID', '4K Monitor', '32-inch IPS monitor', 399.99, 'https://example.com/monitor.jpg')
ON CONFLICT (id) DO NOTHING;"

# 3. Initialize Inventory
echo "  -> Seeding inventory..."
# Check if inventory already exists for these products to avoid duplicates since there's no unique constraint in init.sql
for pid in $PROD1_ID $PROD2_ID $PROD3_ID; do
    EXISTS=$(run_psql -t -A -c "SELECT count(*) FROM inventory_items WHERE product_id = '$pid';")
    if [ "$EXISTS" -eq "0" ]; then
        if [ "$pid" == "$PROD1_ID" ]; then QTY=100; elif [ "$pid" == "$PROD2_ID" ]; then QTY=50; else QTY=10; fi
        run_psql -c "INSERT INTO inventory_items (product_id, quantity) VALUES ('$pid', $QTY);"
    fi
done

echo ""
echo "✅ Seeding Complete!"
echo "User ID: $USER_ID"
echo "Products: Keyboard ($PROD1_ID), Mouse ($PROD2_ID), Monitor ($PROD3_ID)"
echo "Next step: You can now use these IDs to test the Checkout Saga."
