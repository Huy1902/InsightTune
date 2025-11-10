FROM python:3.11-slim


ENV PIP_DEFAULT_TIMEOUT=180 \
    PIP_DISABLE_PIP_VERSION_CHECK=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PYTHONPATH=/app


WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir --prefer-binary -r requirements.txt

COPY dataset ./dataset
COPY rl_agent ./rl_agent
COPY output ./output
COPY app.py .
COPY build_actor.py .
COPY inference.py .

EXPOSE 4007

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "4007"]
