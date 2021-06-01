import sqlalchemy as db
from src.commands.database import Base


class UserInfo(Base):
    __tablename__ = "userinfo"

    userid = db.Column(db.BigInteger, primary_key=True)
    strikes = db.Column(db.Integer)
    is_afk = db.Column(db.Boolean)
    afk_reason = db.Column(db.String)
