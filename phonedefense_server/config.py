class Config:
    VIDEO_FOLDER = '/media/huzhebin/Newdisk/kaldi/egs/thchs30/s5/video_folder'
    VIDEO_CACHE = '/media/huzhebin/Newdisk/kaldi/egs/thchs30/s5/video_cache'
    ALLOWED_EXTENSIONS = set(['wav'])
    BASH_TEST = 'cd /media/huzhebin/Newdisk/kaldi/egs/thchs30/s5 && bash test.sh '
    SQLALCHEMY_DATABASE_URI = 'mysql+pymysql://root:123@127.0.0.1:3306/phonedefense?charset=utf8'
    SQLALCHEMY_TRACK_MODIFICATIONS = True
    CACHE_TYPE = 'redis'
    CACHE_REDIS_HOST = '127.0.0.1'
    CACHE_REDIS_PORT = 6379
    CACHE_REDIS_DB = ''
    CACHE_REDIS_PASSWORD = ''