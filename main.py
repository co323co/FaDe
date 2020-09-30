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
from DB.database import db_session, engine, init_db, clear_db
from DB.models import User, Group, Person, group_person
import FaceDetect, FaceTrain, DeleteEdit
app = Flask(__name__)
api = Api(app)

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}

main_folder = './DATA/'  #최상위 폴더 이름 지정 가능
group_folder = '/group_model/'   #모델 저장되는 폴더 이름 지정 가능
face_folder = '/person_picture/'   #pid별 사진 저장되는 폴더 이름 지정 가능

@app.teardown_appcontext
def shutdown_session(exception = None):
    db_session.remove()

@app.route('/')
def index():
    return "Flask 서버"

#DB를 새로 만듦
@app.route('/initDB')
def createDB():
    clear_db()
    init_db()
    return "서버의 DB를 생성했습니다"

@app.route('/Login/<userEmail>', methods=['PUT'])
def loginUser(userEmail):

    #ignore을 쓰지 않고 굳이 번거롭게 select해서 if문으로 구분하는 이유
    # 1. return 값을 구분하기 쉬움
    # 2. 최근 mysql부터 insert 실패시에도 auto_increment가 작동하여 기본키값이 올라감, 이를 편하게 방지하기 위해서

    #기존에 있는 유저인지 확인하기 위해 select하는 코드
    u = User.query.filter(User.googleEmail==userEmail).first()
    if u:
        return "기존에 등록 된 유저입니다. ID : %s"%userEmail
    else:
        engine.execute('Insert into user(googleEmail) values("%s");'%userEmail)
        return "새로운 User를 등록했습니다. ID : %s"%userEmail

@app.route('/db/GetPerson/<uid>')
def getPerson(uid):
    pass
    
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
    def post(self):                        #json으로 전송해야할 것 : userEmail, pname, 인물썸네일, 사진 리스트
        ts = time.time()
        
        parser = reqparse.RequestParser()
        parser.add_argument('userEmail', type=str)
        parser.add_argument('pname', type=str)
        parser.add_argument('thumbnail', type=str)
        parser.add_argument('pictureList', type=str)
        args = parser.parse_args()
 
        userEmail = args['userEmail']
        pname = args['pname']
        thumbnail = base64.b64decode(args['thumbnail'])
        pictureList=[]

        #인코딩된 사진 리스트들을 가진 Json을 디코딩 하는 과정
        x = json.loads(args['pictureList'])
        for v in x.values():
            pictureList.append(base64.b64decode(v))
        
        #db에 인물 추가함
        user : User = User.query.filter(User.googleEmail==userEmail).first() 
        uid = user.id
        p = Person(uid, pname, thumbnail)
        db_session.add(p)
        db_session.commit()
        pid = p.id

        #만약 유저 디렉터리가 없으면 만든다
        path = main_folder+ 'uid_'+str(uid)+face_folder+str(pid)
        try:
            if not os.path.exists(path):
                os.makedirs(path)
        except:
            print('Error : Creating directory')
        
        for i in range(len(pictureList)):
            f=open(path +'/'+ str(uid) + "_" + str(pid) + "_" + str(i) +".jpeg","wb")
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
        parser.add_argument('userEmail', type=str)
        parser.add_argument('gname', type=str) #그룹이름 
        parser.add_argument('pidList', type=str) #안드로이드 스튜디오에서 ArrayList<Integer>로 pid 담아서 보내면 됨
        args = parser.parse_args()
        
        userEmail = args['userEmail']
        gname = args['gname']
        #문자열로 넘어왔으니 리스트로 파싱해준다.
        pidList = args['pidList'][1:-1].replace(" ","").split(sep=",")
        
        user : User = User.query.filter(User.googleEmail==userEmail).first() 
        uid = user.id

        g= Group(uid, gname)
        db_session.add(g)
        db_session.commit()
        
        gid = g.id

        for pid in pidList:
            group_person.insert().values(gid=gid, pid=pid).execute()

        print("(uid : "+str(userEmail)+", pid_list :"+ str(pidList)+", gname : "+str(gname)+") 수신함.\n")
        model_path = main_folder+'uid_'+str(uid)+group_folder
        path = main_folder+'uid_'+str(uid)+face_folder
        
        #모델 디렉토리가 없으면 새로 생성
        try:
            if not os.path.exists(model_path):
                os.makedirs(model_path)
        except:
            print('Error : Creating directory')
            
        model_face_num = []
        print("Training KNN classifier...")
        classifier = FaceTrain.train(pidList, path, model_save_path=model_path+str(uid)+'_' + str(gid) +'_'+'model.clf', n_neighbors=2)
        model_face_num.append(str(uid)+'_'+ str(gid)+'_'+'model.clf')
        model_face_num.append(classifier)
        model_face_num.append(gname)
        print("================================")

        print(str(classifier)+" faces training complete! (pid_list : "+str(pidList)+")")
        
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
    
    def post(self, uid):
        ts_total = time.time()

        try:
            if not os.path.exists('./DATA'):
                os.makedirs('./DATA')
            if not os.path.exists('./DATA/uid_'+ uid) :
                os.makedirs('./DATA/uid_'+ uid)
            if not os.path.exists('./DATA/uid_'+ uid+'/tmp') :
                os.makedirs('./DATA/uid_'+ uid+'/tmp')
        except:
            print('Error : Creating directory')

        parser = reqparse.RequestParser()
        parser.add_argument('GalleryFiles', type=str)
        args = parser.parse_args()
        GalleryFiles_en=args['GalleryFiles']
        GalleryFiles=[]
        x = json.loads(GalleryFiles_en)

        path = main_folder+ 'uid_'+str(uid)+'/'

        #path = './DATA/uid_'+uid+'/tmp/'

        for k, v in x.items():
            f=open(path+"/tmp/"+k,"wb")
            f.write(base64.b64decode(v))
            f.close()

        print("postGalleryPic")


        elapsed = time.time() - ts_total
        print("사진 받아오는데 걸린 시간: " +str(elapsed))
        print("-------------------------------------------------------")


        reg_group = []
        gid_list = []
    #==========사진 받아온 후 ====================
        f = open(path+'model_face_num.csv', 'r', encoding='utf-8-sig')
        rdr = csv.reader(f)
        for line in rdr:
            reg_group.append(line)
        f.close()

        for image_file in os.listdir(path+'tmp') :
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
                check_group[i[2]] = [i[1], i[3]]
                del i[3]
            print(check_group)
            group = FaceDetect.findBestFitModel(check_group)
            if group == -1:
                print(image_file+"은 적합한 그룹이 없습니다!!")
            else:
                print(image_file+"은 "+group+"그룹에 적합한 사진입니다!")

            gid_list.append(group)
            elapsed = time.time()-ts
            print("얼굴 판별에 걸린 시간: " +str(elapsed))
            print("-------------------------------------------------------")

        try:
            shutil.rmtree(main_folder+'uid_'+ uid+'/tmp', ignore_errors=True)  #폴더 삭제
        except:
            print('Error : Removing tmp directory')    

        print("================================")
        elapsed = time.time()-ts_total
        print("서버 반환 시간: " +str(elapsed))
        print("================================\n")

        print(gid_list)
        return {"gid_list" : gid_list}
    
#그룹 수정 함수 
class EditGroup(Resource):    #json으로 전송해야할 것 : 폴더 이름, 넣을 pid들(리스트로), uid
    def post(self):
        ts = time.time()

        parser = reqparse.RequestParser()


        parser.add_argument('pid', type=str)

        args = parser.parse_args()

        pid = args['pid']

        if pid == -1:
            uid, pid_list, gid = DeleteEdit.editGroup()

        else:
            uid, pid_list, gid = DeleteEdit.editGroup()
            shutil.rmtree(main_folder+'uid_'+ uid+face_folder+'/'+str(pid), ignore_errors=True)  #폴더 삭제
        

        elapsed = time.time()-ts
        print("uid :" + str(uid) +" pid_list : "+str(pid_list)+ " , gid : "+str(gid))

        print("서버 반환 걸린 시간: " +str(elapsed)) 
        print("================================\n")
        
        return {'uid': uid , 'pid' : pid_list, 'gid' : gid}
        

class DeleteGroup(Resource):    #json으로 전송해야할 것 : uid, gid
    def post(self):
        #모델파일 없애고, csv파일 수정
        ts = time.time()

        parser = reqparse.RequestParser()
        parser.add_argument('pid', type=str) 
        args = parser.parse_args()
    
        pid = args['pid']

        if pid == -1:
            uid_, gid = DeleteEdit.deleteGroup()


        else:
            uid_, gid = DeleteEdit.deleteGroup()
            shutil.rmtree(main_folder+'uid_'+ uid_+face_folder+'/'+str(pid), ignore_errors=True)  #폴더 삭제


        elapsed = time.time() - ts
        print("uid :" + str(uid_) +" , gid : "+str(gid))
        print("서버 반환 걸린 시간: " +str(elapsed)) 
        print("================================\n")
        return {'uid': uid_ , 'gid' : gid}

class DeletePerson(Resource):    #json으로 전송해야할 것 : uid, gid
    def post(self):
        #모델파일 없애고, csv파일 수정
        ts = time.time()

        parser = reqparse.RequestParser()
        parser.add_argument('uid', type=str) 
        parser.add_argument('pid', type=str) 

        args = parser.parse_args()
    
        uid = args['uid']
        pid = args['pid']

        shutil.rmtree(main_folder+'uid_'+ uid+face_folder+'/'+str(pid), ignore_errors=True)  #폴더 삭제
        elapsed = time.time() - ts
        print("uid :" + str(uid) +" , pid : "+str(pid))
        print("서버 반환 걸린 시간: " +str(elapsed)) 
        print("================================\n")
        
        return {'uid': uid , 'pid' : pid}



        
    

api.add_resource(RegistPerson, '/reg/person')
api.add_resource(RegistGroup, '/reg/group')
api.add_resource(DetectionPicture, '/det/<uid>')
api.add_resource(EditGroup, '/edit/group')
api.add_resource(DeleteGroup, '/delete/group')
api.add_resource(DeletePerson, '/delete/person')


if __name__ == '__main__':
    app.run(host='0.0.0.0',port=5000)
    

