__author__ = 'morsk'

from message import Types


class Receiver:
    def __init__(self, sender_, node_):
        uuids = []
        options = {
            Types.MSG: self.msg_func,
            Types.ACK: self.ack_func,
            Types.PARENT: self.parent_func,
            Types.CHILD: self.child_func,
            Types.DEAD: self.dead_func,
            Types.BOSS: self.boss_func,
        }
        sender = sender_
        node = node_

    def __parce(self, message, node):
        uuid = message[0..message.find(":")]
        message = message[(message.find(":") + 1):]
        pos = message.find(":")
        rest = ""
        if pos != -1:
            type = message[0..pos]
            rest = message[pos + 1:]
        else:
            type = message
        if type == "ACK":
            rest = uuid;
        return uuid, Types(type), rest

    def msg_func(self, arg, adress):
        self.node.printMessage(arg)
        self.sender.send(arg, self.node.getReceivers(adress))

    def ack_func(self, arg, adress):
        self.sender.getAck(arg)

    def parent_func(self, arg, adress):
        self.node.setParent(arg)
        self.sender.send(Types.CHILD, "", arg)

    def child_func(self, arg, adress):
        self.node.addChild(adress)

    def dead_func(self, arg, adress):
        self.node.eraseNode(adress)

    def boss_func(self, arg, adress):
        self.node.setBoss()

    def process(self, message, adress):
        uuid, type, message = self.__parce(message)
        if (not (uuid in self.uuids)):
            self.uuids.append(uuid)
            self.options[type](message, adress)
        if (type != Types.ACK):
            self.sender.sendAck(uuid, adress)
