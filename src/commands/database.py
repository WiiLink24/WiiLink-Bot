from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

import os

SQL_ALCHEMY_URI = f"postgresql://{os.getenv('WL24_BOT_DB_USER')}:{os.getenv('WL24_BOT_PASSWORD')}@127.0.0.1/{os.getenv('WL24_BOT_DATABASE')}"

engine = create_engine(
    SQL_ALCHEMY_URI
)

Session = sessionmaker()
Session.configure(bind=engine)

session = Session()

Base = declarative_base()
