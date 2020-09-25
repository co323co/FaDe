from sklearn import neighbors
import os
import os.path
import pickle
from PIL import Image, ImageDraw
import face_recognition
from face_recognition.face_recognition_cli import image_files_in_folder
import matplotlib.pyplot as plt
import time
from flask import Flask
from flask_restful import Resource, Api
from flask_restful import reqparse
import json
import base64
import csv

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}

#사진 받아오면 어떤 사람의 얼굴인지 판별하는 함수 정의
def predict(X_img_path, knn_clf=None, model_path=None, distance_threshold=0.345):
    
    if not os.path.isfile(X_img_path) or os.path.splitext(X_img_path)[1][1:] not in ALLOWED_EXTENSIONS:
        raise Exception("Invalid image path: {}".format(X_img_path))

    if knn_clf is None and model_path is None:
        raise Exception("Must supply knn classifier either thourgh knn_clf or model_path")

    # Load a trained KNN model (if one was passed in)
    if knn_clf is None:
        with open(model_path, 'rb') as f:
            knn_clf = pickle.load(f)

    # Load image file and find face locations
    X_img = face_recognition.load_image_file(X_img_path)
    X_face_locations = face_recognition.face_locations(X_img)   #얼굴  위치 찾기

    # If no faces are found in the image, return an empty result.
    if len(X_face_locations) == 0:
        return []

    # Find encodings for faces in the test iamge
    faces_encodings = face_recognition.face_encodings(X_img, known_face_locations=X_face_locations) #얼굴위치의 얼굴정보 벡터 구하기
    # Use the KNN model to find the best matches for the test face
    closest_distances = knn_clf.kneighbors(faces_encodings, n_neighbors=1)
    
    #사진에서 찾은 얼굴개수만큼 학습모델의 기록 중 거리가 threshold 보다 작거나 같을 경우 true, 아니면 false 리스트에 삽입
    are_matches = [closest_distances[0][i][0] <= distance_threshold for i in range(len(X_face_locations))] 

    i =0
    for pred, rec in zip(knn_clf.predict(faces_encodings), are_matches):
        if rec:
            i+=1
    #print(recog_list)
    return i
    #받아온 새로운 사진에 누구 얼굴인지 표시
    
#적합모델 판단하는 함수    
#arg : { gid : [사진속 전체 인물 수, 발견된 인물 수]}
def findBestFitModel(arg):
    l = arg.values()
    findNum = [v[1] if v[1] else 0 for v in l]
    if max(findNum) == 0:
        return -1
    result1=[v for v in l if v[1]==max(findNum)]
    b =result1[0]
    for v in result1:
        if (v[1]/int(v[0])) > (b[1]/int(b[0])):
            b=v
    return [k for k, v in arg.items() if v == b][0]