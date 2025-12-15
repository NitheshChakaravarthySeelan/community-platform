use std::time::Duration;
use rdkafka::consumer::{Consumer, StreamConsumer};
use rdkafka::producer::{FutureProducer, FutureRecord};
use rdkafka::message::Message;
use rdkafka::ClientConfig;
use serde::{Deserialize, Serialize};
use sqlx::PgPool;

#[derive(Deserialize)]
struct ReserveInventoryCommand {
    saga_id: String,
    cart_id: String,
    user_id: String,
    items: Vec<Item>,
}

#[derive(Deserialize)]
struct Item {
    product_id: String,
    quantity: i32,
}

#[derive(Serialize)]
struct InventoryReservedEvent {
    saga_id: String,
    cart_id: String,
    user_id: String,
    r#type: String,
}

#[derive(Serialize)]
struct InventoryReservationFailedEvent {
    saga_id: String,
    cart_id: String,
    user_id: String,
    reason: String,
    r#type: String,
}

async fn process_message(msg: &[u8], producer: &FutureProducer, pool: &PgPool) {
    let command: ReserveInventoryCommand = match serde_json::from_slice(msg) {
        Ok(cmd) => cmd,
        Err(e) => {
            eprintln!("Failed to deserialize message: {}", e);
            return;
        }
    };

    // Clone saga_id, cart_id, and user_id once if they are used multiple times
    let saga_id_clone = command.saga_id.clone();
    let cart_id_clone = command.cart_id.clone();
    let user_id_clone = command.user_id.clone();

    let mut transaction = match pool.begin().await {
        Ok(tx) => tx,
        Err(e) => {
            eprintln!("Failed to begin transaction: {}", e);
            return;
        }
    };

    let mut sufficient_inventory = true;
    for item in &command.items {
        let row: (i32,) = match sqlx::query_as("SELECT quantity FROM inventory WHERE product_id = $1")
            .bind(item.product_id.clone())
            .fetch_one(&mut *transaction)
            .await
        {
            Ok(row) => row,
            Err(_) => {
                sufficient_inventory = false;
                break;
            }
        };

        if row.0 < item.quantity {
            sufficient_inventory = false;
            break;
        }
    }

    if sufficient_inventory {
        for item in &command.items {
            if let Err(e) = sqlx::query("UPDATE inventory SET quantity = quantity - $1 WHERE product_id = $2")
                .bind(item.quantity)
                .bind(item.product_id.clone())
                .execute(&mut *transaction)
                .await
            {
                eprintln!("Failed to update inventory: {}", e);
                let _ = transaction.rollback().await;
                // TODO: publish failure event
                return;
            }
        }

        if let Err(e) = transaction.commit().await {
            eprintln!("Failed to commit transaction: {}", e);
            // TODO: publish failure event
            return;
        }

        let event = InventoryReservedEvent {
            saga_id: saga_id_clone.clone(),
            cart_id: cart_id_clone.clone(),
            user_id: user_id_clone.clone(),
            r#type: "InventoryReserved".to_string(),
        };
        let payload = serde_json::to_string(&event).unwrap();
        let record = FutureRecord::to("checkout.checkout-events")
            .payload(&payload)
            .key(&saga_id_clone);

        if let Err(e) = producer.send(record, Duration::from_secs(0)).await {
            eprintln!("Failed to send message: {:?}", e);
        }
    } else {
        let _ = transaction.rollback().await;
        let event = InventoryReservationFailedEvent {
            saga_id: saga_id_clone.clone(),
            cart_id: cart_id_clone.clone(),
            user_id: user_id_clone.clone(),
            reason: "Insufficient inventory".to_string(),
            r#type: "InventoryReservationFailed".to_string(),
        };
        let payload = serde_json::to_string(&event).unwrap();
        let record = FutureRecord::to("checkout.checkout-events")
            .payload(&payload)
            .key(&saga_id_clone);

        if let Err(e) = producer.send(record, Duration::from_secs(0)).await {
            eprintln!("Failed to send message: {:?}", e);
        }
    }
}


pub async fn run_kafka_consumer(pool: PgPool) {
    let consumer: StreamConsumer = ClientConfig::new()
        .set("bootstrap.servers", "localhost:9092")
        .set("group.id", "inventory-write-group")
        .set("auto.offset.reset", "earliest")
        .create()
        .expect("Consumer creation failed");

    consumer
        .subscribe(&["checkout.inventory-command"])
        .expect("Can't subscribe to specified topics");

    let producer: FutureProducer = ClientConfig::new()
        .set("bootstrap.servers", "localhost:9092")
        .create()
        .expect("Producer creation failed");

    loop {
        match consumer.recv().await {
            Ok(msg) => {
                let payload = msg.payload().unwrap_or_default();
                process_message(payload, &producer, &pool).await;
            }
            Err(e) => {
                eprintln!("Kafka error: {}", e);
            }
        }
    }
}
