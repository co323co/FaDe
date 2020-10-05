from sqlalchemy import Column, Integer, String, Text, ForeignKey, Table
from sqlalchemy.dialects.mysql import LONGBLOB
from sqlalchemy.orm import relationship
from DB.database import Base



class User(Base):
    __tablename__ = 'user'
    id = Column(Integer, primary_key=True)
    googleEmail = Column(String(100), unique=True)

    groups = relationship("Group")
    persons = relationship("Person")

    def __init__(self, googleEmail):
        self.googleEmail = googleEmail

class Group(Base):
    __tablename__ = 'group'

    id = Column(Integer, primary_key=True)
    name = Column(Text)
    favorites = Column(Integer, nullable=False)

    uid = Column(Integer, ForeignKey('user.id',ondelete='CASCADE'))
    group_infos = relationship("Group_info")

    users = relationship("User", foreign_keys=[uid])

    def __init__(self, uid, name=None, favorites = 0):
        self.uid = uid
        self.name = name
        self.favorites = favorites

class Person(Base):
    __tablename__ = 'person'

    id = Column(Integer, primary_key=True)
    name = Column(Text)
    thumbnail = Column(LONGBLOB)
    
    uid = Column(Integer, ForeignKey('user.id',ondelete='CASCADE'))

    users = relationship("User", foreign_keys=[uid])


    def __init__(self, uid, name=None, thumbnail=None):
        self.uid = uid
        self.name = name
        self.thumbnail = thumbnail


group_person = Table('group_person', Base.metadata,
    Column('gid', Integer, ForeignKey('group.id', ondelete='CASCADE')),
    Column('pid', Integer, ForeignKey('person.id',ondelete='CASCADE'))

)
"""
class group_person(Base):
    __tablename__ = 'group_person'


    gid = Column(Integer, ForeignKey('Group.id',ondelete='CASCADE',primary_key=True))
    pid = Column(Integer, ForeignKey('Person.id',ondelete='CASCADE',primary_key=True))

    groups = relationship("Group", foreign_keys=[gid])
    persons = relationship("Person", foreign_keys=[pid])

#mname 모델파일 이름, fnum 모델에 학습된 얼굴 개수
    def __init__(self, gid, pid):
        self.gid = gid
        self.pid = pid

"""

#csv파일 대신하는 테이블
class Group_info(Base):
    __tablename__ = 'group_info'

    mname = Column(Text)
    fnum = Column(Integer)
    gid = Column(Integer, ForeignKey('group.id',ondelete='CASCADE'),primary_key = True)

    groups = relationship("Group", foreign_keys=[gid])

#mname 모델 파일 이름, fnum 모델에 학습된 얼굴 개수
    def __init__(self, mname, fnum, gid):
        self.mname = mname
        self.fnum = fnum
        self.gid = gid