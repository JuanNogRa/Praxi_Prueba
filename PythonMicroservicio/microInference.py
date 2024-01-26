from flask import Flask, request
# YOLOv5 PyTorch HUB Inference (DetectionModels only)
import torch
# Python code to read image
import cv2
import base64
import numpy as np
import pickle

model = torch.hub.load('yolov5', 'custom', path='best.pt', source='local') # or yolov5n - yolov5x6 or custom
app = Flask(__name__)

@app.route("/MicroInference", methods=['GET'])
def microservice():
    # To read image from disk, we use
    # cv2.imread function, in below method,
    img = cv2.imread("Room.png", cv2.IMREAD_COLOR)
    # Inference
    results = model(img)
    return results.pandas().xyxy[0].to_json()

from PIL import Image
import json
from io import BytesIO
@app.route("/Json2Mat", methods=['POST'])
def microserviceJSON2Mat():
    data = request.json
    data_dump = json.dumps(data)
    data_des = json.loads(data_dump)
    imdata = base64.b64decode(data_des['data'])
    print(imdata)
    return data

if __name__ == "__main__":
    app.run(port=8000)