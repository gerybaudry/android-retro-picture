import sys

import http.client
import json
import base64
from Crypto.PublicKey import RSA
from Crypto.Cipher import AES
from Crypto import Random

class Request:
  HOSTNAME = "localhost"
  PORT = 8000

  def __init__(self, route, headers, body):
    self.route = route
    self.headers = headers
    self.body = body

  def send(self):
    httpClient = http.client.HTTPConnection(Request.HOSTNAME, Request.PORT)
    httpClient.request(
      "GET",
      self.route,
      self.body)

    response = httpClient.getresponse()
    if not response.closed:
      data = response.read()
    else:
      data = None
    return (response, data)

class GetServerKey(Request):
  def __init__(self):
    Request.__init__(self, "/server_key", {}, None)

  def decipherKey(self, key):
    return RSA.importKey(base64.b64decode(key))

class SignIn(Request):
  def __init__(self, serverRsaKey, email, password, aesKey):
    emailEncrypted = serverRsaKey.encrypt(email.encode("utf-8"), 0)[0]
    passwordEncrypted = serverRsaKey.encrypt(password.encode("utf-8"), 0)[0]
    keyEncrypted = serverRsaKey.encrypt(aesKey, 0)[0]

    Request.__init__(self, "/sign_in", {}, json.dumps({
      "method": "sign_in",
      "body": {
        "email": base64.b64encode(emailEncrypted).decode("utf-8"),
        "password": base64.b64encode(passwordEncrypted).decode("utf-8"),
        "key": base64.b64encode(keyEncrypted).decode("utf-8")
      }
    }))

class SendMessage(Request):
  def __init__(self, aesKey, userId, authToken, contactId, messageContent):
    Request.__init__(self, "/secure_route/{0}".format(userId), {}, base64.b64encode(aesKey.encrypt(pad(json.dumps({
      "method": "send_message",
      "body": {
        "auth_token": authToken,
        "contact": contactId,
        "content": messageContent
      }
    })))))

def pad(data):
  b = bytearray(data.encode("utf-8"))
  while len(b) % 16:
    b.append(0)
  return b.decode("utf-8").encode("utf-8")

def unpad(data):
  i = len(data) - 1
  while i >= 0:
    if data[i] != 0:
      return data[:i+1]
    i -= 1
  return b""

def generateAESCipher(key):
  return AES.new(key)

def run(email, password, contactId):
  aesKey = Random.new().read(16)

  # 1. get server rsa key
  getServerRSAKey = GetServerKey()
  response, data = getServerRSAKey.send()
  if response.status == 200:
    key = json.loads(data.decode("utf-8"))
    serverRsaKey = getServerRSAKey.decipherKey(key["key"])
    print("Got server RSA public key")
  else:
    print("fail getting server rsa key")
    return -1

  # 2. sign in (with no gcmToken)
  signInRequest = SignIn(serverRsaKey, email, password, aesKey)
  response, data = signInRequest.send()
  if response.status == 200:
    aesCipher = generateAESCipher(aesKey)
    json_body = json.loads(unpad(aesCipher.decrypt(base64.b64decode(data))).decode("utf-8"))
    authToken = json_body["auth_token"]
    userId = json_body["id"]

    print("User ID: {0}\nAuthToken: {1}".format(userId, authToken))

  else:
    print("Sign in fail")
    return -1

  # 3. send messages
  while True:
    message = input("Send: ")
    sendMessageRequest = SendMessage(generateAESCipher(aesKey), userId, authToken, contactId, message)
    response, data = sendMessageRequest.send()
    if response.status == 200:
      aesCipher = generateAESCipher(aesKey)
      json_body = json.loads(unpad(aesCipher.decrypt(base64.b64decode(data))).decode("utf-8"))
      if json_body["success"]:
        print("Success")
      else:
        print("Fail")
        return -1

  return 0

if __name__ == "__main__":
  ac = len(sys.argv)
  av = sys.argv

  if ac != 4:
    print("usage: {0} EMAIL PASSWORD CONTACTID".format(sys.argv[0]))
    email = "yolo@swag.com"
    password = "yolOsw4g"
    contactId = 3
  else:
    email = sys.argv[1]
    password = sys.argv[2]
    contactId = int(sys.argv[3])

  sys.exit(run(email, password, contactId))
