from fastapi import FastAPI

app = FastAPI()

@app.get("/")
async def read_root():
    return {"message": "Hello from Checkout Orchestrator Service"}

# Add a health check endpoint
@app.get("/health")
async def health_check():
    return {"status": "ok"}
