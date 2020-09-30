from sqlalchemy import Column, Integer, String, Text, ForeignKey, Table
from sqlalchemy.dialects.mysql import LONGBLOB
from sqlalchemy.orm import relationship
from DB.database import Base



class User(Base):
    __tablename__ = 'User'
    id = Column(Integer, primary_key=True)
    googleEmail = Column(String(100), unique=True)

    groups = relationship("Group")
    persons = relationship("Person")

    def __init__(self, googleEmail):
        self.googleEmail = googleEmail

class Group(Base):
    __tablename__ = 'Group'

    id = Column(Integer, primary_key=True)
    name = Column(Text)
    favorites = Column(Integer, nullable=False)

    uid = Column(Integer, ForeignKey('User.id'))
    def __init__(self, uid, name=None, favorites = 0):
        self.uid = uid
        self.name = name
        self.favorites = favorites

class Person(Base):
    __tablename__ = 'Person'

    id = Column(Integer, primary_key=True)
    name = Column(Text)
    thumbnail = Column(LONGBLOB)
    
    uid = Column(Integer, ForeignKey('User.id'))

    def __init__(self, uid, name=None, thumbnail=None):
        self.uid = uid
        self.name = name
        self.thumbnail = thumbnail


group_person = Table('group_person', Base.metadata,
    Column('gid', Integer, ForeignKey('Group.id')),
    Column('pid', Integer, ForeignKey('Person.id'))
)