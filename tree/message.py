__author__ = 'morsk'

import uuid


class Message:
    def __init__(self, type_, arg_, address_):
        self.uuid = uuid.uuid1()
        self.msg = uuid + ":" + type_ + (":" + arg_ if arg_ != "" else "")
        self.address = address_
        self.ttl = 5

    def decr(self):
        self.ttl -= 1
        return self.ttl > 0

    def __eq__(self, other):
        return self.uuid == other.uuid


from enum import Enum

Types = Enum('MSG', 'ACK', 'PARENT', 'CHILD', 'DEAD', 'BOSS')
