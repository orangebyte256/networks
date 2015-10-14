import sender

__author__ = 'morsk'

from message import Types

class Node:
    def __init__(self, sender_):
        self.sender = sender_
        self.childs = []
        self.parent = ""
        self.boss = False;
    def killed(self):
        if(self.boss):
            tmp = self.childs[1:]
            sender.send(Types.PARENT, self.childs[0], tmp)
            sender.send(Types.BOSS, "", self.childs[0])
            sender.send(Types.DEAD, "", self.childs)
        else:
            sender.send(Types.PARENT, self.parent, self.childs)
            tmp = self.childs[:]
            tmp.append(self.parent)
            sender.send(Types.DEAD, "", tmp)

    def setParent(self, parent):
        self.parent = parent

    def addChild(self, child):
        self.childs.append(child)

    def eraseNode(self, node):
        if(self.parent == node):
            self.parent = ""
        else:
            self.childs.remove(node)

    def setBoss(self):
        self.boss = True;

    def printMessage(self, msg):
        print msg

    def getReceivers(self, waste):
        result = []
        if(self.parent == waste):
            result = self.childs
        else:
            result = self.childs[:]
            result.remove(waste)
            result.add(self.parent)
        return result