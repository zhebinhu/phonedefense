from flask import Blueprint
import redis

bind_list = redis.Redis(host='localhost', port=6379, db=0)
ip_list = redis.Redis(host='localhost', port=6379, db=1)
calling_list = set()
msg = {}

api = Blueprint('api', __name__)

from . import post
from . import get

