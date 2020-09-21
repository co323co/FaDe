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

main_folder = './DATA/'  #최상위 폴더 이름 지정 가능
group_folder = '/group_model/'   #모델 저장되는 폴더 이름 지정 가능
face_folder = '/person_picture/'   #pid별 사진 저장되는 폴더 이름 지정 가능

def deleteGroup():
    parser = reqparse.RequestParser()
    parser.add_argument('uid', type=str)
    parser.add_argument('gid', type=str) 
    args = parser.parse_args()
    
    uid = args['uid']
    gid = args['gid']
    
    print("(uid : "+str(uid)+", gid : "+str(gid)+") 수신함.\n")
    model_path = main_folder+'uid_'+uid+group_folder

    os.remove(model_path+str(uid)+'_'+str(gid)+'_'+'model.clf')

    csvfile= open(main_folder+'uid_'+str(uid)+'/model_face_num.csv', 'r',encoding='utf-8-sig', newline='')
    rd = csv.reader(csvfile)
    lines = []
    for line in rd:
        if line[2] == str(gid):
            continue
        lines.append(line)
        
    print(str(lines)+"삭제 후 csv 파일 정보")

    csvfile= open(main_folder+'uid_'+str(uid)+'/model_face_num.csv', 'w+',encoding='utf-8-sig', newline='')

    wr = csv.writer(csvfile)
    
    for line in lines:
        wr.writerow(line)
    csvfile.close()
    print("csv 파일 수정 완료")

    return uid, gid
    

def editGroup():
    parser = reqparse.RequestParser()
    parser.add_argument('uid', type=str)
    parser.add_argument('pidList', type=str) #안드로이드 스튜디오에서 ArrayList<String>로 pid 담아서 보내면 됨니다
    parser.add_argument('gid', type=str) #그룹아이디 

    args = parser.parse_args()
        
    uid = args['uid']
    pid_list = args['pidList']
    gid = args['gid']

    print("(uid : "+str(uid)+", pid_list :"+str(pid_list)+", gid : "+str(gid)+") 수신함.\n")

    
    model_path = main_folder+'uid_'+uid+group_folder
    path = main_folder+'uid_'+uid+face_folder
    
    if pid_list is None: #비어져있는 pidlist를 받으면 해당 그룹 모델파일을 없애버림
        os.remove(model_path+str(uid)+'_'+str(gid)+'_'+'model.clf')

        csvfile= open(main_folder+'uid_'+str(uid)+'/model_face_num.csv', 'r',encoding='utf-8-sig', newline='')
        rd = csv.reader(csvfile)
        lines = []
        for line in rd:
            if line[2] == str(gid):
                continue
            lines.append(line)
            
        print(str(lines)+"삭제 후 csv 파일 정보")

        csvfile= open(main_folder+'uid_'+str(uid)+'/model_face_num.csv', 'w+',encoding='utf-8-sig', newline='')

        wr = csv.writer(csvfile)
        
        for line in lines:
            wr.writerow(line)
        csvfile.close()
        print("csv 파일 수정 완료")


        return uid, pid_list, gid


    else:
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
        model_face_num.append(gid)
        print("================================")

        print(str(classifier)+" faces training complete! (pid_list : "+str(pid_list)+")")
        
        #학습모델의 학습된 얼굴 개수 csv파일로 저장 (리스트로 받아오기 가능)
        csvfile= open(main_folder+'uid_'+str(uid)+'/model_face_num.csv', 'r',encoding='utf-8-sig', newline='')
        rd = csv.reader(csvfile)
        lines = []
        for line in rd:
            if line[0] == model_face_num[0]:
                line[1] = model_face_num[1]
            lines.append(line)
            
        print(str(lines)+"수정할 그룹 정보")

        csvfile= open(main_folder+'uid_'+str(uid)+'/model_face_num.csv', 'w+',encoding='utf-8-sig', newline='')

        wr = csv.writer(csvfile)
        
        for line in lines:
            wr.writerow(line)
        csvfile.close()
        print("csv 파일 수정 완료")
        
        return uid, pid_list, gid