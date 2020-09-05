#안녕혜림아


#import문 및 얼굴 학습시켜 모델만드는 함수 정의
import math
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
import shutil
import json
import base64
import os
import time
import csv

app = Flask(__name__)
api = Api(app)
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}

def train(pid_list, train_dir, model_save_path=None, n_neighbors=None, knn_algo='ball_tree', verbose=False):
    X = []
    y = []
    z = []
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


#사진 받아오면 어떤 사람의 얼굴인지 판별하는 함수 정의
def predict(X_img_path, knn_clf=None, model_path=None, distance_threshold=0.375):
    
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
def findBestFitModel(arg):
    l = arg.values()
    findNum = [v[1] for v in l]
    result1=[v for v in l if v[1]==max(findNum)]
    b =result1[0]
    for v in result1:
        if (v[1]/int(v[0])) > (b[1]/int(b[0])):
            b=v
    return [k for k, v in arg.items() if v == b][0]


main_folder = './DATA/'  #최상위 폴더 이름 지정 가능
group_folder = '/group_model/'   #모델 저장되는 폴더 이름 지정 가능
face_folder = '/person_picture/'   #pid별 사진 저장되는 폴더 이름 지정 가능


@app.route('/')
def index():
    return "Flask 서버"
    
@app.route('/<username>')
def show_user(username):
    #print(username)
    print("get")
    return {'uid': username , 'pid' : 77}

@app.route('/db/download/<uid>')
def getDB(uid):
    print("getDB")
    try:
        if not os.path.exists('./DATA'):
            os.makedirs('./DATA')
        if not os.path.exists('./DATA/uid_'+ uid) :
            os.makedirs('./DATA/uid_'+ uid)
        if not os.path.exists('./DATA/uid_'+ uid+'/databases') :
            os.makedirs('./DATA/uid_'+ uid+'/databases')
    except:
        print('Error : Creating directory')
        
    #db파일들이 있는 폴더
    path = './DATA/uid_'+uid+'/databases/'
    #db파일들 경로리스트
    pathList=['App.db','App.db-shm','App.db-wal']
    dbFiles=[]
    returnData=[]
    #경로에서 읽어옵니다
    for i in range(len(pathList)):
        f=open(path + pathList[i],"rb")
        dbFiles.append(base64.b64encode(f.read()).decode('utf8'))
        f.close()
    #JSON으로 바꿔줌 {파일명 : 바이트, ...} 형태
    return { 'db0':dbFiles[0], 'db1':dbFiles[1], 'db2':dbFiles[2] }

@app.route('/db/upload/<uid>',methods = ['POST'])
def postDB(uid):
    try:
        if not os.path.exists('./DATA'):
            os.makedirs('./DATA')
        if not os.path.exists('./DATA/uid_'+ uid) :
            os.makedirs('./DATA/uid_'+ uid)
        if not os.path.exists('./DATA/uid_'+ uid+'/databases') :
            os.makedirs('./DATA/uid_'+ uid+'/databases')
    except:
        print('Error : Creating directory')
        
    parser = reqparse.RequestParser()
    parser.add_argument('dbFiles', type=str)
    args = parser.parse_args()
    dbFiles_en=args['dbFiles']
    dbFiles=[]
    x = json.loads(dbFiles_en)
    path = './DATA/uid_'+uid+'/databases/'
    for k, v in x.items():
        f=open(path+k,"wb")
        f.write(base64.b64decode(v))
        f.close()
    print("postDB")
    return {'result':True}


class RegistPerson(Resource): #얼굴등록할 때 모델 만들 필요가 없으므로 detection으로 학습기능 빼냄(즉, 사진 폴더별로 저장만)
    def post(self):                        #json으로 전송해야할 것 : uid, pid, 사진 리스트
        ts = time.time()
        
        parser = reqparse.RequestParser()
        parser.add_argument('uid', type=str)
        parser.add_argument('pid', type=str)
        parser.add_argument('pictureList', type=str)
        args = parser.parse_args()
 
        uid = args['uid']
        pid = args['pid']
        pictureList_en = args['pictureList']
        pictureList=[]

        #인코딩된 사진 리스트들을 가진 Json을 디코딩 하는 과정
        x = json.loads(pictureList_en)
        for v in x.values():
            print(len(v))
            pictureList.append(base64.b64decode(v))
        
        #만약 유저 디렉터리가 없으면 만든다
        path = main_folder+ 'uid_'+str(uid)+face_folder+str(pid)
        try:
            if not os.path.exists(path):
                os.makedirs(path)
        except:
            print('Error : Creating directory')
        
        for i in range(len(pictureList)):
            f=open(path +'/'+ uid + "_" + str(pid) + "_" + str(i) +".jpeg","wb")
            f.write(pictureList[i])
            f.close()

        elapsed = time.time()-ts
        print("================================")
        print(str(i+1)+"장의 (pid : "+str(pid)+") 사진 저장 완료")
        print("서버 반환 걸린 시간: " +str(elapsed))
        print("================================\n")
        return {'uid': uid , 'pid' : pid}

class RegistGroup(Resource):    #json으로 전송해야할 것 : 폴더 이름, 넣을 pid들(리스트로), uid
    def post(self):
        ts = time.time()

        parser = reqparse.RequestParser()
        parser.add_argument('uid', type=str)
        parser.add_argument('pidList', type=str) #안드로이드 스튜디오에서 ArrayList<String>로 pid 담아서 보내면 됨니다
        parser.add_argument('gid', type=str) #그룹이름 
        args = parser.parse_args()
        
        uid = args['uid']
        pid_list = args['pidList']
        gid = args['gid']
        
        print("(uid : "+str(uid)+", pid_list :"+str(pid_list)+", gid : "+str(gid)+") 수신함.\n")
        model_path = main_folder+'uid_'+uid+group_folder
        path = main_folder+'uid_'+uid+face_folder
        
        #모델 디렉토리가 없으면 새로 생성
        try:
            if not os.path.exists(model_path):
                os.makedirs(model_path)
        except:
            print('Error : Creating directory')
            
        model_face_num = []
        print("Training KNN classifier...")
        classifier = train(pid_list, path, model_save_path=model_path+str(uid)+'_'+str(gid)+'_'+'model.clf', n_neighbors=2)
        model_face_num.append(str(uid)+'_'+str(gid)+'_'+'model.clf')
        model_face_num.append(classifier)
        print("================================")

        print(str(classifier)+" faces training complete! (pid_list : "+str(pid_list)+")")
        
        #학습모델의 학습된 얼굴 개수 csv파일로 저장 (리스트로 받아오기 가능)
        csvfile= open(main_folder+'uid_'+str(uid)+'/model_face_num.csv', 'a',encoding='utf-8-sig', newline='')
        wr = csv.writer(csvfile)
        wr.writerow(model_face_num)
        csvfile.close()
        

        elapsed = time.time()-ts
        print("서버 반환 걸린 시간: " +str(elapsed)) 
        print("================================\n")
        return {'uid': uid , 'pid' : pid_list, 'gid' : gid}#<------생성된 모델로 잘 인식하는지 테스트 해보고싶으면 밑에서 테스트해보세요!
    
class DetectionPicture(Resource):
    
    def post(self):
        ts_total = time.time()
        
        parser = reqparse.RequestParser()
        parser.add_argument('uid', type=str)
        parser.add_argument('pictureList', type=str)
        args = parser.parse_args()
 
        uid = args['uid']
        pictureList_en = args['pictureList']
        pictureList=[]
        

        #인코딩된 사진 리스트들을 가진 Json을 디코딩 하는 과정
        x = json.loads(pictureList_en)
        for v in x.values():
            print(len(v))
            pictureList.append(base64.b64decode(v))
        
        path = main_folder+ 'uid_'+str(uid)+'/'
        
        try:
            if not os.path.exists(path+'tmp/'):
                os.makedirs(path+'tmp/')
        except:
            print('Error : Creating directory')
            
        for i in range(len(pictureList)):
            f=open(path+ 'tmp/'+uid + "_" + str(i) +".jpeg","wb")
            f.write(pictureList[i])
            f.close()

        elapsed = time.time()-ts_total
        print("사진 받아오는데 걸린 시간: " +str(elapsed))
        print("-------------------------------------------------------")

        reg_group = []

#==========사진 받아온 후 ====================
        f = open(path+'model_face_num.csv', 'r', encoding='utf-8-sig')
        rdr = csv.reader(f)
        for line in rdr:
            reg_group.append(line)
        f.close()

        for image_file in os.listdir(path+'tmp'):
            full_img_path = os.path.join(path+'tmp', image_file)
            print("Looking for faces in {}".format(image_file))
            ts = time.time()
            # Find all people in the image using a trained classifier model
            # Note: You can pass in either a classifier file name or a classifier model instance
            
            for model_file in os.listdir(path+'group_model'):
                full_model_path = os.path.join(path+'group_model', model_file)
                predictions = predict(full_img_path, model_path=full_model_path)
                for i in reg_group:
                    if i[0] == str(model_file):
                        i.append(predictions)
            print(reg_group)
            check_group = {}
            for i in reg_group:
                check_group[i[0]] = [i[1], i[2]]
                del i[2]

            group = findBestFitModel(check_group)
            print(image_file+"은 "+group+"그룹에 적합한 사진입니다!")
            elapsed = time.time()-ts
            print("얼굴 판별에 걸린 시간: " +str(elapsed))
            print("-------------------------------------------------------")
        #shutil.rmtree('./DATA/uid_20171108/tmp', ignore_errors=True)  #폴더 삭제
        
        print("================================")
        elapsed = time.time()-ts_total
        print("서버 반환 시간: " +str(elapsed))
        print("================================\n")
        return {'gid': str(group)}
    
api.add_resource(RegistPerson, '/reg/person')
api.add_resource(RegistGroup, '/reg/group')
api.add_resource(DetectionPicture, '/det')



if __name__ == '__main__':
    app.run(host='0.0.0.0',port=5000)
    


# In[ ]:





# In[ ]:




