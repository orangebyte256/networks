import math
from message import Types
from node import Node
from receiver import Receiver

__author__ = 'morsk'

import socket

import signal
import sys
import select

from sender import Sender

host = ''
port = 50000
size = 2048
sender = Sender()
node = Node(sender)
receiver = Receiver(sender, node)
name = "Dima"

def signal_handler(signal, frame):
        node.killed()
        sys.exit(0)

def catString(str):
    tmp = str.substring(0, size / 2)
    val = size / 2;
    for i in range(math.log(size, 2),0):
        tmp = str.substring(0, val + math.pow(2, i))
        if(len(tmp.encode('utf-8')) > size):
            val -= math.pow(2, i)
        else:
            val += math.pow(2, i)
    return str.substrin(val)

def main():
    parent = ""
    if(len(sys.argv) == 3):
        parent = sys.argv[2]
    name = sys.argv[1]
    signal.signal(signal.SIGINT, signal_handler)
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind((host,port))
    inputs = [sock, sys.stdin]
    outputs = []
    if(parent == ""):
        node.boss()
    else:
        sender.send(Types.CHILD, "", sys.argv[2])
        node.setParent(sys.argv[2])
    while True:
        readable, writable, exceptional = select.select(inputs, outputs, inputs)
        for s in readable:
            if s is sock:
                data = sock.recv(size)
                receiver.process(data, sock.getsockname()[0] + ":" + sock.getsockname()[1])
            else:
                input = sys.stdin.readline()
                input = catString(input)
                sender.send(Types.MSG, name + ":" + input, sock.getsockname()[0] + ":" + sock.getsockname()[1])

if __name__ == '__main__':
    main()
