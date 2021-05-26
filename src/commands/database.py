from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

SQL_ALCHEMY_URI = "postgresql://username:password@127.0.0.1/wl24bot"

engine = create_engine(
    SQL_ALCHEMY_URI
)

Session = sessionmaker()
Session.configure(bind=engine)

session = Session()

Base = declarative_base()
