from app import create_app
from gevent.pywsgi import WSGIServer

from app.api import ip_list, bind_list

app = create_app()

@app.before_first_request
def before_request():
    from app import db
    db.create_all()


from Queue import Queue, Empty
from socket import *
import threading

UDP_HOST = ''
UDP_PORT = 4567
BUFSIZ = 2048 * 10
UDP_ADDR = (UDP_HOST, UDP_PORT)

udpSerSock = socket(AF_INET, SOCK_DGRAM)
udpSerSock.bind(UDP_ADDR)

msgqueue = {}

def udp():
    while True:
        data, addr = udpSerSock.recvfrom(BUFSIZ)
        print ('getudp'+str(len(data)))
        elderid = ip_list.get(addr[0])
        if not msgqueue.has_key(elderid):
            msgqueue[elderid] = Queue(32)
        msgqueue[elderid].put(data)


TCP_HOST = ''
TCP_PORT = 12345
TCP_ADDR = (TCP_HOST, TCP_PORT)

tcpSerSock = socket(AF_INET, SOCK_STREAM)
tcpSerSock.bind(TCP_ADDR)
tcpSerSock.listen(5)


def tcp():
    while True:
        tcpCliSock, addr = tcpSerSock.accept()
        familyid = ip_list.get(addr[0])
        elderid = bind_list.get(familyid)
        t = threading.Thread(target=tcplink, args=(tcpCliSock, elderid))
        t.start()


def tcplink(sock, elderid):
    while True:
        try:
            if not msgqueue.has_key(elderid):
                msgqueue[elderid] = Queue(32)
            data = msgqueue[elderid].get(timeout=5)
            print('tcpsend'+str(len(data)))
            sock.send(data)
        except Empty:
            break
    print('close')
    sock.close()


if __name__ == '__main__':
    threading.Thread(target=udp).start()
    threading.Thread(target=tcp).start()
    http_server = WSGIServer(('0.0.0.0', 8080), app)
    http_server.serve_forever()
