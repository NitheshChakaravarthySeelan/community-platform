import sys
import os
import uuid
import time

# Add the orchestrator directory to path
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))

from checkout_orchestrator import common_pb2
from checkout_orchestrator import order_service_pb2
from checkout_orchestrator import payment_service_pb2
from checkout_orchestrator import wallet_service_pb2
from checkout_orchestrator import invoice_service_pb2

def simulate_saga():
    saga_id = str(uuid.uuid4())
    user_id = str(uuid.uuid4())
    amount_cents = 5000 # $50.00

    print(f"--- Starting Saga E2E Simulation for Saga ID: {saga_id} ---")

    # Step 1: Payment Command
    print("\n[Orchestrator] Sending ProcessPaymentCommand...")
    payment_cmd = payment_service_pb2.ProcessPaymentCommand()
    payment_cmd.metadata.saga_id = saga_id
    payment_cmd.user_id = user_id
    payment_cmd.amount_cents = amount_cents
    payment_cmd.currency = "USD"
    # Verify we can serialize/deserialize
    data = payment_cmd.SerializeToString()
    print(f"Serialized Payment Command Size: {len(data)} bytes")

    # Step 2: Payment Processed Event (Simulated from Payment Service)
    print("\n[Payment Service] Simulating PaymentProcessedEvent...")
    payment_event = payment_service_pb2.PaymentProcessedEvent()
    payment_event.metadata.saga_id = saga_id
    payment_event.transaction_id = str(uuid.uuid4())
    payment_event.order_id = saga_id
    payment_event.user_id = user_id
    payment_event.amount_cents = amount_cents
    payment_event.status = "SUCCESS"
    
    # Step 3: Wallet Command
    print("\n[Orchestrator] Received Payment Success. Sending DebitWalletCommand...")
    wallet_cmd = wallet_service_pb2.DebitWalletCommand()
    wallet_cmd.metadata.saga_id = saga_id
    wallet_cmd.user_id = user_id
    wallet_cmd.amount_cents = amount_cents
    
    # Step 4: Wallet Debited Event (Simulated from Wallet Service)
    print("\n[Wallet Service] Simulating WalletDebitedEvent...")
    wallet_event = wallet_service_pb2.WalletDebitedEvent()
    wallet_event.metadata.saga_id = saga_id
    wallet_event.user_id = user_id
    wallet_event.debited_amount_cents = amount_cents
    wallet_event.current_balance_cents = 10000
    
    # Step 5: Create Order Command
    print("\n[Orchestrator] Received Wallet Debit Success. Sending CreateOrderCommand...")
    order_cmd = order_service_pb2.CreateOrderCommand()
    order_cmd.metadata.saga_id = saga_id
    order_cmd.user_id = user_id
    order_cmd.total_cents = amount_cents
    order_cmd.payment_transaction_id = payment_event.transaction_id
    
    # Step 6: Order Created Event (Simulated from Order Service)
    order_id = str(uuid.uuid4())
    print(f"\n[Order Service] Simulating OrderCreatedEvent for Order ID: {order_id}...")
    order_event = order_service_pb2.OrderCreatedEvent()
    order_event.metadata.saga_id = saga_id
    order_event.order_id = order_id
    order_event.user_id = user_id
    order_event.total_cents = amount_cents
    
    # Step 7: Generate Invoice Command
    print("\n[Orchestrator] Received Order Created. Sending GenerateInvoiceCommand...")
    invoice_cmd = invoice_service_pb2.GenerateInvoiceCommand()
    invoice_cmd.metadata.saga_id = saga_id
    invoice_cmd.order_id = order_id
    invoice_cmd.user_id = user_id
    invoice_cmd.total_cents = amount_cents
    
    # Step 8: Invoice Generated Event (Simulated from Invoice Service)
    print("\n[Invoice Service] Simulating InvoiceGeneratedEvent...")
    invoice_event = invoice_service_pb2.InvoiceGeneratedEvent()
    invoice_event.metadata.saga_id = saga_id
    invoice_event.invoice_id = str(uuid.uuid4())
    invoice_event.order_id = order_id
    invoice_event.invoice_pdf_url = f"http://cdn.community.com/invoices/{invoice_event.invoice_id}.pdf"

    print("\n--- Saga E2E Simulation COMPLETE ---")
    print(f"Final Invoice URL: {invoice_event.invoice_pdf_url}")

if __name__ == "__main__":
    simulate_saga()
