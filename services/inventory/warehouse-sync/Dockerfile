FROM python:3.12-slim-bookworm as builder

WORKDIR /app

# Install Poetry
RUN pip install poetry

# Copy project files
COPY pyproject.toml poetry.lock ./

# Install dependencies
RUN poetry install --no-root --no-interaction --no-ansi

# Copy application code
COPY . .

FROM python:3.12-slim-bookworm

WORKDIR /app

# Copy only the installed dependencies and application code from the builder stage
COPY --from=builder /app/.venv /app/.venv
COPY --from=builder /app /app

# Add .venv to PATH
ENV PATH="/app/.venv/bin:$PATH"

# Expose port (default for FastAPI/Uvicorn)
EXPOSE 8000

# Command to run the application
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]