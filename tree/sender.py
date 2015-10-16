__author__ = 'morsk'

import socket
from random import randint

from message import Message, Types

loss_count = 80

class Sender:
    def __init__(self):
        self.messages = []
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    def sendAck(self, uuid, receiver):
        socket.sendto(uuid + ":" + Types.ACK, receiver.split(":"))

    def send_with_loss(self, msg):
        if (randint(0,99) < loss_count):
            self.socket.sendto(msg.message, msg.address.split(":"))

    def send(self, type, msg, receivers):
        for receiver in receivers:
            self.messages.append(Message(type, msg, receiver))
            msg = self.messages[-1]
            self.send_with_loss(msg)

    def getAck(self, uuid):
        tmp = Message(Types.ARR, "", "")
        tmp.uuid = uuid
        self.messages.remove(tmp)

    def cleaning(self):
        tmp = self.messages[:]
        for message in self.messages:
            self.send_with_loss(message)
            if(message.decr()):
                tmp.remove(message)
        self.messages = tmp
