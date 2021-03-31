# 1. 프로젝트 설명
인공지능 갤러리 정리어플 FaDe의 서버 

# 2. 프로젝트 개발 환경
- MySQL @8.0
- vscode @1.53.2
- Flask Server
- face_recognition 딥러닝 기반 얼굴인식 오픈소스 라이브러리

# 3. 설치 및 사용 방법
1.  <www.mysql.com>에서 MySQL 설치
2. MySQL Connections 생성(root, localhost, 3306)
3. 서버의 database.py파일에서 db정보 바꿔주기 (비밀번호 같은 변동사항)
4. Flask 사용 위한 ORM 설치 
```
pip install sqlAlchemy
```
5. SQL 연동 위한 mysql connector 라이브러리 설치
```
 mysql-connector-python
```
1. initServer 실행하여 database, table 생성 및 초기화(main.py 37번째줄 참고, /init-server)
2. FaDe 프로젝트 내 Server/ConnService.java **(FaDe_Server X)** 에서 서버 url 수정 후 어플리케이션 실행

# 5. 연락처 정보
- email: heryms@naver.com