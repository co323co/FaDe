from sqlalchemy import create_engine
from sqlalchemy.orm import scoped_session, sessionmaker
from sqlalchemy.ext.declarative import declarative_base

db = {
    'user'     : 'root',		# 1)
    'password' : '15995144k',		# 2)
    'host'     : 'localhost',	# 3)
    'port'     : 3306,			# 4)
    'database' : 'fade'		# 5)
}
DB_URL = f"mysql+mysqlconnector://{db['user']}:{db['password']}@{db['host']}:{db['port']}/{db['database']}?charset=utf8"

#데이터베이스 엔진(데이터베이스에 접속할 엔진)
engine = create_engine(DB_URL,echo=True, convert_unicode=True)
#세션 객체가 실제 연결을 소유
db_session = scoped_session(sessionmaker(autocommit=False, autoflush=False, bind=engine))
#데이터베이스 테이블 모델이 사용할 기본 선언클래스
Base = declarative_base()
#Base가 어떤 데이터 조작 수행 객체를 사용할 것인지 지정
Base.query = db_session.query_property()

Base.metadata.bind=engine

#테이블 생성
def init_db():
    import DB.models
    #데이터베이스에 테이블을 일괄적으로 생성
    Base.metadata.create_all(bind=engine)

#모든 테이블을 삭제한다
def clear_db():
    Base.metadata.drop_all()
