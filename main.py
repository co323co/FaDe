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
from DB.database import db_session, engine, init_db, clear_db, db
from DB.models import User, Group, Person, group_person
from knn_modules import FaceDetect, FaceTrain
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

#서버 초기화 (DB, 테이블 새로만들기, DATA폴더 삭제하기)
@app.route('/initServer')
def initServer():
    #result = [v[0] for v in (engine.execute("SHOW DATABASES;").fetchall())]
    #print(result)
    #None인 경우 DB가 존재하지 않는단 의미

    #if(db['database'] not in result): #DB가 없으면 만들어 준다
    #        engine.execute("create database %s;"%db['database'])
    #else: #있는 경우 테이블 싹 지워줌
    clear_db()

    init_db() #테이블 다시 새로만듦

    #서버에 DATA 디렉토리 삭제
    if os.path.exists(main_folder):
                shutil.rmtree(main_folder, ignore_errors=True) 


    return "서버를 초기화했습니다"

@app.route('/Login/<userEmail>', methods=['PUT', 'GET'])
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

#Get All Groups by favorites
@app.route('/db/GetAllGroups/<userEmail>')
def getAllGroups(userEmail):
    uid = (engine.execute('Select id From %s.user WHERE googleEmail = "%s";'%(db['database'], userEmail))).first()[0]
    result = engine.execute('Select * From %s.group WHERE uid = %d order by favorites desc;'%(db['database'],uid))
    #튜플들의 리스트로 결과 행들 받아옴
    rows = result.fetchall()
    dicList = []
    for v in rows:
        dicList.append({'id' : v[0], 'name' : v[1], 'favorites' : v[2]})
        json.dumps(dicList)
    return json.dumps(dicList)
    

@app.route('/db/GetGroup_Person/<gid>')
def getGroup_Person(gid):
    result = engine.execute('Select pid From %s.group_person WHERE gid = %d;'%(db['database'],int(gid)))
    pidList = [v[0] for v in result.fetchall()]
    print(pidList)
    return pidList

#Get All Persons
@app.route('/db/GetAllPersons/<userEmail>')
def getAllPersons(userEmail):
    uid = (engine.execute('Select id From %s.user WHERE googleEmail = "%s";'%(db['database'], userEmail))).first()[0]
    result = engine.execute('Select * From %s.person WHERE uid = %d;'%(db['database'],uid))
    #튜플들의 리스트로 결과 행들 받아옴
    rows = result.fetchall()
    dicList = []
    for v in rows:
        dicList.append({'id' : v[0], 'name' : v[1], 'thumbnail' : None})
        """if v[2] is None:
            dicList.append({'id' : v[0], 'name' : v[1], 'thumbnail' : v[2]})
        else:    
            dicList.append({'id' : v[0], 'name' : v[1], 'thumbnail' : base64.b64encode(v[2])})"""
    print(dicList)
    return json.dumps(dicList, ensure_ascii=False)

@app.route('/db/GetPersonsByGid/<gid>')
def getPersonsByGid(gid):
    result = engine.execute('Select p.* From %s.person as p, group_person as g_p WHERE p.id = g_p.pid and gid = %d;'%(db['database'],int(gid)))
    #튜플들의 리스트로 결과 행들 받아옴
    rows = result.fetchall()
    dicList = []
    for v in rows:
        if v[2] is None:
            dicList.append({'id' : v[0], 'name' : v[1], 'thumbnail' : v[2]})
        else:    
            dicList.append({'id' : v[0], 'name' : v[1], 'thumbnail' : base64.b64encode(v[2])})
    return json.dumps(dicList)

@app.route('/db/GetPidListByGid/<gid>')
def getPidListByGid(gid):
    result = engine.execute('Select pid From group_person WHERE gid = %d;'%int(gid))
    pidList = [v[0] for v in result.fetchall()]
    return json.dumps(pidList)

@app.route('/db/GetGroupsByPid/<pid>')
def getGroupsByPid(pid):
    
    result = engine.execute('Select g.* From %s.group as g, group_person as g_p WHERE g.id = g_p.gid and pid = %d;'%(db['database'],int(pid)))
    #튜플들의 리스트로 결과 행들 받아옴
    rows = result.fetchall()
    dicList = []
    for v in rows:
        dicList.append({'id' : v[0], 'name' : v[1], 'favorites' : v[2]})
    return json.dumps(dicList, ensure_ascii=False)

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
 
        pname = args['pname']
        if(args['thumbnail']):
            thumbnail = base64.b64decode(args['thumbnail'])
        else:
            thumbnail = None

        pictureList=[]

        #인코딩된 사진 리스트들을 가진 Json을 디코딩 하는 과정
        x = json.loads(args['pictureList'])
        for v in x.values():
            pictureList.append(base64.b64decode(v))
            print(len((base64.b64decode(v))))
        
        #db에 인물 추가함
        user : User = User.query.filter(User.googleEmail==args['userEmail']).first() 
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
        print("여기까지 ok2")
        parser = reqparse.RequestParser()
        parser.add_argument('userEmail', type=str)
        parser.add_argument('gname', type=str) #그룹이름 
        parser.add_argument('pidList', type=str) #안드로이드 스튜디오에서 ArrayList<Integer>로 pid 담아서 보내면 됨
        args = parser.parse_args()
        
        gname = args['gname']
        #문자열로 넘어왔으니 리스트로 파싱해준다.
        pidList = args['pidList'][1:-1].replace(" ","").split(sep=",")
        
        user : User = User.query.filter(User.googleEmail==args['userEmail']).first() 
        uid = user.id

        print("여기까지 ok1")
        #db에 그룹 추가
        g= Group(uid, gname)
        db_session.add(g)
        db_session.commit()
        print("여기까지 ok2")
        
        gid = g.id
        #db에 그룹이 보유한 pid관계 추가(pidList를 반영)
        for pid in pidList:
            group_person.insert().values(gid=gid, pid=pid).execute()

        model_path = main_folder+'uid_'+str(uid)+group_folder
        path = main_folder+'uid_'+str(uid)+face_folder
        
        #모델 디렉토리가 없으면 새로 생성
        try:
            if not os.path.exists(model_path):
                os.makedirs(model_path)
        except:
            print('Error : Creating directory')
            
        print("Training KNN classifier...")
        classifier = FaceTrain.train(pidList, path, model_save_path=model_path+str(uid)+'_' + str(gid) +'_'+'model.clf', n_neighbors=2)
        
        #group_info에 모델명, 모델에 학습시킨 얼굴 개수, gid 삽입
        engine.execute('Insert into %s.group_info values("%s",%d,%d);'%(db['database'],str(uid)+'_' + str(gid) +'_'+'model.clf', classifier,int(gid)))
        
        
        print("================================")

        print(str(classifier)+" faces training complete! (pid_list : "+str(pidList)+")")
        elapsed = time.time()-ts
        print("서버 반환 걸린 시간: " +str(elapsed)) 
        print("================================\n")
        return {'uid': uid , 'pid' : pidList, 'gid' : gid}#<------생성된 모델로 잘 인식하는지 테스트 해보고싶으면 밑에서 테스트해보세요!
    
class DetectionPicture(Resource):
    def post(self, userEmail):
        ts_total = time.time()

        uid = (engine.execute('Select id From %s.user WHERE googleEmail = "%s";'%(db['database'], userEmail))).first()[0]
        
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

        if not os.path.exists(path+"tmp"):
            os.makedirs(path+"tmp")

        for k, v in x.items():
            f=open(path+"tmp/"+k,"wb")
            f.write(base64.b64decode(v))
            f.close()

        print("postGalleryPic")


        elapsed = time.time() - ts_total
        print("사진 받아오는데 걸린 시간: " +str(elapsed))
        print("-------------------------------------------------------")


        reg_group = []
        gnameList = []
    #==========사진 받아온 후 ====================

        #group_info에 있는 데이터 리스트로 가져옴
        result = engine.execute('Select * From %s.group_info;'%(db['database']))
        reg_group = [list(v) for v in result.fetchall()]    

        print(reg_group)

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
            #사진에서 인식된 얼굴의 수 리스트에 추가

            for i in reg_group:
                check_group[i[2]] = [i[1], i[3]]
                del i[3]
            print(check_group)
            #학습시킨 얼굴의 개수와, 인식된 얼굴의 수를 비교하여 해당 사진이 어떤 그룹에 속하는지 반환
            group = FaceDetect.findBestFitModel(check_group)
            print(group)
            if group == -1:
                gnameList.append("None")
                print(image_file+"은 적합한 그룹이 없습니다!!")
            else:
                gnameList.append((engine.execute('Select name From %s.group WHERE id = "%d";'%(db['database'], int(group)))).first()[0])
                print(image_file+"은 "+str(group)+"그룹에 적합한 사진입니다!")
            elapsed = time.time()-ts
            print("얼굴 판별에 걸린 시간: " +str(elapsed))
            print("-------------------------------------------------------")

        try:
            shutil.rmtree(main_folder+'uid_'+ str(uid)+'/tmp', ignore_errors=True)  #폴더 삭제
        except Exception as e:
            print('Error : Removing tmp directory : ', e)

        print("================================")
        elapsed = time.time()-ts_total
        print("서버 반환 시간: " +str(elapsed))
        print("================================\n")

        print(gnameList)
        #return {"gnameList" : gnameList}
        return gnameList
    
class EditGroup(Resource):   
    def post(self):
        ts = time.time()
        parser = reqparse.RequestParser()
        parser.add_argument('gid', type=int) #그룹아이디 
        parser.add_argument('gname', type=str)
        parser.add_argument('pidList', type=str) #안드로이드 스튜디오에서 ArrayList<Integer>로 pid 담아서 보내면 됨
        parser.add_argument('favorites', type=int)

        args = parser.parse_args()
        gid = args['gid']
        gname = args['gname']
        favorites = args['favorites']
        pidList = args['pidList']

        group : Group = Group.query.filter(Group.id==gid).first()
        uid = group.uid

        rStr=""
        #####db###########
        ##################
        # not 은 리스트 안에 값이 있는지 없는지를 확인 ex) p=[]일 경우 not p임. 즉 not으로 비교하면 없는 걸로 침
        # None 은 해당 값 자체가 존재하는지를 혹인 ex) p=[]일 경우 p is not None임. 즉 None으로 비교하면 있는 걸로 침
        
        #빈 리스트를 준게 아니라 아예 값을 주지 않았을 때를 방지하기 위한 체크임
        if gname is not None :
            engine.execute('Update %s.group set name = "%s" where id = %d;'%(db['database'],gname,gid))
            rStr+="gname 수정 됨"
        if favorites is not None:
            engine.execute('Update %s.group set favorites = "%d" where id = %d;'%(db['database'],favorites,gid))
            rStr+="favorites 수정 됨"
        if pidList is not None:
            rStr+="pidList 수정 반영 됨"
            #문자열로 넘어왔으니 리스트로 파싱해준다.
            pidList = pidList[1:-1].replace(" ","").split(sep=",")

            #그룹의 pid관계를 다 삭제하고 변경된 pidList들을 db에 삽입함
            engine.execute('Delete From group_person Where gid = %d;'%gid)
            for pid in pidList:
                group_person.insert().values(gid=gid, pid=pid).execute()

            model_path = main_folder+'uid_'+str(uid)+group_folder
            path = main_folder+'uid_'+str(uid)+face_folder
    
            #비어져있는 pidlist를 받으면 해당 그룹 모델파일을 없애버림 --> 얼굴 삭제 했을 때 그룹에 사람이 없는경우
            if len(pidList) == 0: 
                rStr+="pidList가 비어있어 모델 파일을 삭제함"
                os.remove(model_path+str(uid)+'_'+str(gid)+'_'+'model.clf')
                engine.execute('Delete From %s.group_info Where gid = %d;'%(db['database'],int(gid)))

            


            else:
                #모델 디렉토리가 없으면 새로 생성
                try:
                    if not os.path.exists(model_path):
                        os.makedirs(model_path)
                except:
                    print('Error : Creating directory')
                
                model_face_num = []
                print("Training KNN classifier...")
                classifier = FaceTrain.train(pidList, path, model_save_path=model_path+str(uid)+'_'+str(gid)+'_'+'model.clf', n_neighbors=2)
                #group_info 새로 학습된 정보로 업데이트
                engine.execute('update %s.group_info set fnum = %d where gid = %d;'%(db['database'],classifier,int(gid)))

                print("================================")

                print(str(classifier)+" faces training complete! (pidList : "+str(pidList)+")")
            
 
        elapsed = time.time()-ts

        print("서버 반환 걸린 시간: " +str(elapsed)) 
        print("================================\n")

        return rStr

class DeleteGroup(Resource):    #json으로 전송해야할 것 : uid, gid
    def post(self):
        #모델파일 없애고, csv파일 수정
        ts = time.time()
        parser = reqparse.RequestParser()
        parser.add_argument('userEmail', type=str)
        parser.add_argument('gid', type=str) 
        args = parser.parse_args()
    
        gid = args['gid']
        user : User = User.query.filter(User.googleEmail==args['userEmail']).first() 
        uid = user.id

        print("(uid : "+str(uid)+", gid : "+str(gid)+") 수신함.\n")
        
        #db에서 group 레코드 삭제
        engine.execute('Delete From %s.group Where id = %s;'%(db['database'],gid))

        model_path = main_folder+'uid_'+str(uid) + group_folder

        os.remove(model_path+str(uid)+'_' + str(gid) +'_'+'model.clf')  

        elapsed = time.time() - ts
        print("uid :" + str(uid) +" , gid : "+str(gid))
        print("서버 반환 걸린 시간: " +str(elapsed)) 
        print("================================\n")
        return {'uid': uid , 'gid' : gid}
class DeletePerson(Resource):    #json으로 전송해야할 것 : uid, gid
    def post(self):
        ts = time.time()

        parser = reqparse.RequestParser()
        parser.add_argument('userEmail', type=str) 
        parser.add_argument('pid', type=str) 

        args = parser.parse_args()
        pid = args['pid']
        user : User = User.query.filter(User.googleEmail==args['userEmail']).first() 
        uid = user.id


        result = engine.execute('Select g.* From %s.group as g, group_person as g_p WHERE g.id = g_p.gid and pid = %d;'%(db['database'],int(pid)))
        #지우는 pid가 해당되었던 그룹 리스트 받아오기
        rows = result.fetchall()
        dicList = []
        for v in rows:
            dicList.append(v[0])
        print(dicList)
        #db에서 person 레코드 삭제
        engine.execute('Delete From person Where id = %s;'%pid)

        ######## person을 삭제하게 됨으로써 빈 그룹도 삭제하는 과정
        #person을 하나도 안가지고있는 group 찾아내기 (group_person 테이블에 없는 그룹)
        result = engine.execute('SELECT * From %s.group Where id not in (Select distinct gid From group_person);'%db['database'])
        gidList = str(tuple([v[0] for v in result])).replace(",)",")") #튜플은 원소가 1개인경우 (1,) 이런식으로 표현됨. 그래서 replace 한 것임
        if gidList != '()': #비어있는 그룹이 하나이상 존재한다면 delete한다
            engine.execute('Delete From %s.group Where id in %s;'%(db['database'], gidList))#group_info까지 연쇄삭제됨

        #폴더 삭제
        shutil.rmtree(main_folder+'uid_'+ str(uid) +face_folder+'/'+str(pid), ignore_errors=True)  

        model_path = main_folder+'uid_'+str(uid)+group_folder
        path = main_folder+'uid_'+str(uid)+face_folder

        print(model_path)
        print(path)
        
        for gid in dicList:
            result = engine.execute('Select p.* From %s.person as p, group_person as g_p WHERE p.id = g_p.pid and gid = %d;'%(db['database'],int(gid)))
            #튜플들의 리스트로 결과 행들 받아옴
            if result is None:
                continue
            
            pidList = [str(v[0]) for v in result.fetchall()]

            if not pidList:
                continue
            
            print(pidList)
            print(gid)

            print("Training KNN classifier...")
            classifier = FaceTrain.train(pidList, path, model_save_path=model_path+str(uid)+'_' + str(gid) +'_'+'model.clf', n_neighbors=2)
            
            #group_info에 모델명, 모델에 학습시킨 얼굴 개수, gid 삽입
            engine.execute('update %s.group_info set fnum = %d where gid = %d;'%(db['database'],classifier,int(gid)))
            
            
            print("================================")

            print(str(classifier)+" faces training complete! (pid_list : "+str(pidList)+")")


        elapsed = time.time() - ts
        print("uid :" + str(uid) +" , pid : "+str(pid))
        print("서버 반환 걸린 시간: " +str(elapsed)) 
        print("================================\n")
        
        return {'uid': uid , 'pid' : pid}

api.add_resource(RegistPerson, '/reg/person')
api.add_resource(RegistGroup, '/reg/group')
api.add_resource(DetectionPicture, '/det/<userEmail>')
api.add_resource(EditGroup, '/edit/group')
api.add_resource(DeleteGroup, '/delete/group')
api.add_resource(DeletePerson, '/delete/person')


if __name__ == '__main__':
    app.run(host='0.0.0.0',port=5000)
    

