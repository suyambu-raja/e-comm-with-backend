from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from typing import IO
import uvicorn
import os

app = FastAPI(
    title="OCR Service",
    description="A microservice to perform Optical Character Recognition on product price tags.",
    version="1.0.0"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure appropriately for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Health check endpoint
@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "ocr-service"}

@app.post("/scan/price-tag")
async def scan_price_tag(file: UploadFile = File(...)):
    """
    Accepts an image file, performs OCR, and returns the extracted text.

    - **file**: The image file (e.g., JPEG, PNG) of the price tag.
    """
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File provided is not an image.")

    try:
        # Read image content
        image_bytes = await file.read()

        # --- Placeholder for Google Vision API Call ---
        # In a real implementation, you would send `image_bytes` to the Google Vision API.
        #
        # from google.cloud import vision
        # client = vision.ImageAnnotatorClient()
        # image = vision.Image(content=image_bytes)
        # response = client.text_detection(image=image)
        # texts = response.text_annotations
        #
        # if texts:
        #     extracted_text = texts[0].description
        # else:
        #     extracted_text = ""
        #
        # if response.error.message:
        #      raise HTTPException(status_code=500, detail=response.error.message)
        # ---------------------------------------------

        # Using placeholder text for this example
        extracted_text = "MRP Rs. 150.00\nMFG DATE: 12/24\nBEST BEFORE: 12/25"

        return {"status": "success", "extracted_text": extracted_text}

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An error occurred during OCR processing: {str(e)}")
    finally:
        await file.close()

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)