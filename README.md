# 1. 프로젝트명
인공지능 갤러리 정리어플 FaDe

IT기술의 발달에 따른 스마트폰 저장공간 증가로 인한 원하는 사람의 사진을 쉽게 찾지 못하는 곤란한 상황을 해결을 목표로 하는 머신러닝을 통한 얼굴 인식을 이용하여 갤러리의 사진을 사용자가 원하는 얼굴 별로 자동 정리 해주는 어플 개발 프로젝트

[프로젝트 소개 링크](https://www.notion.so/co323co/FaDe-776a106927b74a39a4724fb20b0d2eab)

# 2. 프로젝트 개발 환경
- MySQL @8.0
- Android Strudio @10.0
- Flask Server
- face_recognition 딥러닝 기반 얼굴인식 오픈소스 라이브러리

# 3. 안드로이드 애플리케이션 버전
- minSdkVersion 24
- targetSdkVersion 30

# 4. 설치 및 사용 방법
1. <www.mysql.com>에서 MySQL 설치
2. MySQL Connections 생성(root, localhost, 3306)
3. 서버의 database.py파일에서 db정보 바꿔주기 (비밀번호 같은 변동사항)
4. Flask 사용 위한 ORM 설치 
```
pip install sqlAlchemy
```
5. SQL 연동 위한 mysql connector 라이브러리 설치
```
pip install mysql-connector-python
```
6. face_recognition 라이브러리 사용 위한 설치
```
pip install cmake
pip install dlib
pip install face_recognition
```

7. initServer 실행하여 database, table 생성 및 초기화(main.py 37번째줄 참고, /init-server)
8. FaDe 프로젝트 내 Server/ConnService.java **(FaDe_Server X)** 에서 서버 url 수정 후 어플리케이션 실행
9. FaDe apk 파일 실행 후 사용

# 5. 연락처 정보
- email: co323co@gmail.com​
