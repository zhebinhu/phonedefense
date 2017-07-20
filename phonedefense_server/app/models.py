from app import db


class Keyword(db.Model):
    __tablename__ = 'keyword'
    keyword = db.Column(db.String(64),primary_key=True)
    count = db.Column(db.Integer,default=1)

    @staticmethod
    def add(keyword):
        k = Keyword.query.filter_by(keyword=keyword).first()
        if k is not None:
            k.count += 1
        else:
            k = Keyword(keyword=keyword,count=1)
        db.session.add(k)
        db.session.commit()

    @staticmethod
    def high_rate_words():
        k = Keyword.query.order_by(Keyword.count.desc()).filter(Keyword.count>3).all()
        return [i.keyword for i in k]
