from __future__ import print_function
from __future__ import print_function
import os
import re
import subprocess
import gevent
from flask import request, current_app
from werkzeug.utils import secure_filename
from gevent.queue import Queue

from app.models import Keyword
from ..utils import allowed_file, delete_file_folder
from . import api, msg
from . import bind_list, calling_list


@api.route('/upvideo', methods=['POST'])
def upvideo():
    elderid = request.values['elderid']
    familyid = bind_list.get(elderid)
    file = request.files['video']
    if file and allowed_file(file.filename):
        filepath = current_app.config['VIDEO_CACHE']
        file.save(os.path.join(filepath, file.filename))
        if not msg.has_key(familyid):
            msg[familyid] = Queue()
        msg[familyid].put('get_' + file.filename)
    return 'success'


@api.route('/complain', methods=['POST'])
def complain():
    file = request.files['video']
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        filepath = current_app.config['VIDEO_FOLDER'] + '/' + filename.rsplit('.', 1)[0]
        file.save(os.path.join(filepath, filename))
        p = subprocess.Popen(current_app.config['BASH_TEST'] + filename.rsplit('.', 1)[0], shell=True)
        while not p.poll():
            gevent.sleep(1)
        with open(filepath+"/decode_dnn/log/decode.1.log") as f:
            s = f.read()
        r = re.sub("[A-Za-z0-9\[\`\~\!\@\#\$\^\&\*\(\)\=\|\{\}\'\:\;\'\,\[\]\.\<\>\/\?\~\@\#\&\*\%\-\_\"\+]", "", s)
        r = r.strip()
        r = r.lstrip()
        for k in r.split(" "):
            if len(k)>3:
                Keyword.add(k)
    with open(current_app.config['VIDEO_FOLDER']+'/kwsdatadir/raw_keywords.txt','w+') as f:
        for i in Keyword.high_rate_words():
            f.write(i.encode('utf8')+'\n')
    return 'success'


@api.route('/upfile', methods=['POST'])
def upfile():
    file = request.files['video']
    elderid = request.values['elderid']
    familyid = bind_list.get(elderid)
    print(file.filename)
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        filepath = current_app.config['VIDEO_FOLDER'] + '/' + filename.rsplit('.', 1)[0]
        if os.path.exists(filepath):
            delete_file_folder(filepath)
        os.makedirs(filepath + '/video')
        file.save(os.path.join(filepath + '/video', filename))
        p = subprocess.Popen(current_app.config['BASH_TEST'] + filename.rsplit('.', 1)[0], shell=True)
        while not p.poll():
            gevent.sleep(1)
        if os.path.isfile(filepath + '/kwsdir/result.xml'):
            msg[familyid].put('dangercalling')
            return 'find'
        with open(filepath+"/decode_dnn/log/decode.1.log") as f:
            s = f.read()
        r = re.sub("[A-Za-z0-9\[\`\~\!\@\#\$\^\&\*\(\)\=\|\{\}\'\:\;\'\,\[\]\.\<\>\/\?\~\@\#\&\*\%\-\_\"\+]", "", s)
        r = r.strip()
        r = r.lstrip()
        klist = Keyword.high_rate_words()
        for k in r.split(" "):
            if k in klist:
                msg[familyid].put('dangercalling')
                return 'find'
    return 'notfind'


@api.route('/bind', methods=['POST'])
def bind():
    elderid = request.values['elderid']
    familyid = request.values['familyid']
    print(elderid)
    print(familyid)
    if elderid is not None and familyid is not None:
        if bind_list.get(elderid) is not None:
            old_familyid = bind_list.get(elderid)
            if old_familyid == familyid:
                return 'existed'
            bind_list.delete(elderid)
            bind_list.delete(old_familyid)
        if bind_list.get(familyid) is not None:
            old_elderid = bind_list.get(familyid)
            if old_elderid == elderid:
                return 'existed'
            bind_list.delete(familyid)
            bind_list.delete(old_elderid)
        bind_list.set(elderid, familyid)
        bind_list.set(familyid, elderid)
        return 'success'
    return 'fail'


@api.route('/calling', methods=['POST'])
def calling():
    elderid = request.values['elderid']
    status = request.values['status']
    familyid = bind_list.get(elderid)
    print(elderid)
    print(status)
    if elderid is None:
        return 'fail'
    if status.startswith('calling'):
        if not msg.has_key(elderid):
            msg[elderid] = Queue()
        if not msg.has_key(familyid):
            msg[familyid] = Queue()
        if elderid not in calling_list:
            calling_list.add(elderid)
            msg[familyid].put(status)
        else:
            return 'success'
    elif status == 'stop':
        if not msg.has_key(elderid):
            msg[elderid] = Queue()
        if not msg.has_key(familyid):
            msg[familyid] = Queue()
        if elderid in calling_list:
            calling_list.remove(elderid)
            msg[familyid].put(status)
        else:
            return 'success'
    return 'fail'


@api.route('/stopcalling', methods=['POST'])
def stopcalling():
    print("stopcalling")
    familyid = request.values['familyid']
    elderid = bind_list.get(familyid)
    if elderid in calling_list:
        msg[elderid].put('stopcalling')
    return 'success'


@api.route('/startlisten', methods=['POST'])
def startlisten():
    familyid = request.values['familyid']
    elderid = bind_list.get(familyid)
    if elderid in calling_list:
        msg[elderid].put('startlisten')
    return 'success'


@api.route('/stoplisten', methods=['POST'])
def stoplisten():
    familyid = request.values['familyid']
    elderid = bind_list.get(familyid)
    if elderid in calling_list:
        msg[elderid].put('stoplisten')
    return 'success'
