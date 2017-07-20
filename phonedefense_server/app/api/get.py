import gevent
from flask import request, send_from_directory, current_app
from gevent.queue import Empty

from app.api import api, msg, bind_list, calling_list, ip_list


@api.route('/elderpolling', methods=['GET'])
def familypolling():
    elderid = request.args.get('elderid')
    elderaddr = request.remote_addr
    ip_list.set(elderaddr,elderid)
    print('familypolling')
    result = 'fail'
    if msg.has_key(elderid):
        try:
            result = msg[elderid].get(timeout=1)
        except Empty:
            result = 'fail'
        return result
    gevent.sleep(1)
    return result


@api.route('/familypolling', methods=['GET'])
def polling():
    familyid = request.values['familyid']
    elderid = bind_list.get(familyid)
    familyaddr = request.remote_addr
    ip_list.set(familyaddr,familyid)
    if msg.has_key(familyid):
        try:
            result = msg[familyid].get(timeout=50)
            if result == 'calling' and elderid not in calling_list:
                result = 'fail'
        except Empty:
            result = 'fail'
        return result
    gevent.sleep(1)
    return 'fail'


@api.route('/download/<filename>', methods=['GET'])
def download(filename):
    return send_from_directory(current_app.config['VIDEO_CACHE'],filename)
