import math
from sklearn import neighbors
import os
import os.path
import pickle
from PIL import Image, ImageDraw
import face_recognition
from face_recognition.face_recognition_cli import image_files_in_folder
import time

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}

def train(pid_list, train_dir, model_save_path=None, n_neighbors=None, knn_algo='ball_tree', verbose=False):
    X = []
    y = []
    i = 0
    # Loop through each person in the training set
    for class_dir in os.listdir(train_dir):   #현재 디렉토리에 있는 모든 파일(디렉토리) 리스트를 가져온다.
        if not os.path.isdir(os.path.join(train_dir, class_dir)):   #넘겨준 path들을 묶어 하나의 경로로 만들어줍니다.->디렉토리가 생성되었는지 확인
            continue
        if class_dir not in pid_list:
            continue
            
        # Loop through each training image for the current person
        for img_path in image_files_in_folder(os.path.join(train_dir, class_dir)):#해당 경로의 이미지를 하나씩 img_path로 가져옴
            image = face_recognition.load_image_file(img_path) #이미지 파일을 numpy 배열로 로드한다.
            face_bounding_boxes = face_recognition.face_locations(image)#CSS (위, 오른쪽, 아래, 왼쪽) 순서로 찾은 얼굴 위치의 튜플 목록

            if len(face_bounding_boxes) != 1:
                # If there are no people (or too many people) in a training image, skip the image.
                if verbose:
                    print("Image {} not suitable for training: {}".format(img_path, "Didn't find a face" if len(face_bounding_boxes) < 1 else "Found more than one face"))
            else:
                # Add face encoding for current image to the training set
                
                X.append(face_recognition.face_encodings(image, known_face_locations=face_bounding_boxes)[0])
                y.append(class_dir)   #x는 사진의 얼굴 정보, y는 학습한 얼굴들에 해당하는 폴더이름
                print(class_dir+"에 대한 얼굴 정보를 "+img_path+"에서 학습 중")
            print("학습x방지")
            
        i+=1
        print(class_dir+" 얼굴 학습 완료!")
        print("-------------------------------------------------------")
        

    # Determine how many neighbors to use for weighting in the KNN classifier
    if n_neighbors is None:
        n_neighbors = int(round(math.sqrt(len(X))))
        if verbose:
            print("Chose n_neighbors automatically:", n_neighbors)

    # Create and train the KNN classifier
    knn_clf = neighbors.KNeighborsClassifier(n_neighbors=n_neighbors, algorithm=knn_algo, weights='distance')
    knn_clf.fit(X, y)

    # Save the trained KNN classifier
    if model_save_path is not None:
        with open(model_save_path, 'wb') as f:
            pickle.dump(knn_clf, f)
            
    return i