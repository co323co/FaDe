#import문 및 얼굴 학습시켜 모델만드는 함수 정의
from sklearn import neighbors
import os
import os.path
from face_recognition.face_recognition_cli import image_files_in_folder
import time
from flask import Flask
from flask_restful import Resource, Api
from flask_restful import reqparse
import shutil
import json
import base64
import time
import csv

import FaceTrain
import FaceDetect

app = Flask(__name__)
api = Api(app)
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}

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
    #경로에서 읽어옵니다
    for i in range(len(pathList)):
        try:
            f=open(path + pathList[i],"rb")
            dbFiles.append(base64.b64encode(f.read()).decode('utf8'))
            f.close()
        except:
            return {'result':False}

    #JSON으로 바꿔줌 {파일명 : 바이트, ...} 형태
    return { 'db0':dbFiles[0], 'db1':dbFiles[1], 'db2':dbFiles[2], 'result':True }

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
        
    try:
        parser = reqparse.RequestParser()
        parser.add_argument('dbFiles', type=str)
        args = parser.parse_args()
        dbFiles_en=args['dbFiles']
        x = json.loads(dbFiles_en)
        path = './DATA/uid_'+uid+'/databases/'
        for k, v in x.items():
            f=open(path+k,"wb")
            f.write(base64.b64decode(v))
            f.close()
        print("postDB")
        return {'result':True}
        
    except Exception as e:
        print(e)
        return {'result':False}
    
@app.route('/gallery/upload/<uid>',methods = ['POST'])
def postPIC(uid):
    
    try:
        if not os.path.exists(main_folder+'uid_'+ uid+'/tmp') :
            os.makedirs(main_folder+'uid_'+ uid+'/tmp')

    except:
        print('Error : Creating directory')
        
    try:
        parser = reqparse.RequestParser()
        parser.add_argument('GalleryFiles', type=str)
        args = parser.parse_args()
        Files_en=args['GalleryFiles']
        x = json.loads(Files_en)
        path = main_folder+'uid_'+uid+'/tmp/'
        for k, v in x.items():
            f=open(path+k,"wb")
            f.write(base64.b64decode(v))
            f.close()
        print("postPIC")
        return {'result':True}
        
    except Exception as e:
        print(e)
        return {'result':False}

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
        classifier = FaceTrain.train(pid_list, path, model_save_path=model_path+str(uid)+'_'+str(gid)+'_'+'model.clf', n_neighbors=2)
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
                predictions = FaceDetect.predict(full_img_path, model_path=full_model_path)
                for i in reg_group:
                    if i[0] == str(model_file):
                        i.append(predictions)
            print(reg_group)
            check_group = {}
            for i in reg_group:
                check_group[i[0]] = [i[1], i[2]]
                del i[2]

            group = FaceDetect.findBestFitModel(check_group)
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
    

