FROM python:3.13-slim

WORKDIR /app

COPY . .

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 2005

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "2005"]



