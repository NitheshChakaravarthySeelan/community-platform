from fastapi import FastAPI

app = FastAPI()

@app.get("/")
async def read_root():
    return {"message": "Hello from Recommendation Model Service"}

# Add a health check endpoint
@app.get("/health")
async def health_check():
    return {"status": "ok"}
