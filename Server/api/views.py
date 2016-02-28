from django.http import HttpResponse, JsonResponse

import base64
import hashlib
import uuid
import traceback
import json
import datetime
import http.client
from Crypto.PublicKey import RSA
from Crypto.Cipher import AES

from .models import User, Message

def server_key(request):
  # create key if does not exist
  try:
    # load keys
    with open("publickey.bin", "rb") as f:
      publickey = RSA.importKey(f.read())
  except:
    # if problem loading keys
    # create new keys
    keys = RSA.generate(2048, e=65537)
    publickey = keys.publickey()
    with open("privatekey.bin", "wb") as f:
      f.write(keys.exportKey("DER"))
    with open("publickey.bin", "wb") as f:
      f.write(publickey.exportKey("DER"))

  publickeyB64 = base64.b64encode(publickey.exportKey("DER")).decode("utf-8")
  # return json response
  return JsonResponse({
    "key": publickeyB64
  })

def sign_up(request):
  # get server RSA key
  try:
    with open("privatekey.bin", "rb") as f:
      privatekey = RSA.importKey(f.read())
  except:
    return JsonResponse({
      "success": False,
      "reason": "No RSA key"
    })

  # get arguments
  try:
    body = json.loads(request.body.decode("utf-8"))["body"]
    nickname = privatekey.decrypt(base64.b64decode(body["nickname"])).decode("utf-8")
    email = privatekey.decrypt(base64.b64decode(body["email"])).decode("utf-8")
    password = privatekey.decrypt(base64.b64decode(body["password"])).decode("utf-8")
    gcmToken = privatekey.decrypt(base64.b64decode(body["gcmToken"])).decode("utf-8") if "gcmToken" in body else None
    key = body["key"]
  except:
    return JsonResponse({
      "success": False,
      "reason": "Missing argument"
    })

  # decipher the key
  try:
    aeskeyBytes = privatekey.decrypt(base64.b64decode(key))
    aesKey = AES.new(aeskeyBytes)
  except:
    traceback.print_exc()
    return JsonResponse({
      "success": False,
      "reason": "Could not decipher the key"
    })

  # save user
  try:
    # generate auth token
    auth_token = base64.b64encode(uuid.uuid4().bytes).decode("utf-8")
    user = User(nickname=nickname, email=email, password=hashlib.sha512(password.encode("utf-8")).hexdigest(), authToken=auth_token, aesKey=key, gcmRegistrationToken=gcmToken)
    user.save()
    return encode_response({
              "success": True,
              "id": user.id,
              "auth_token": auth_token
            }, aesKey)

  except:
    traceback.print_exc()
    return encode_response({
              "success": False,
              "reason": "Could not save the user"
            }, aesKey)

def sign_in(request):
  # get server RSA key
  try:
    with open("privatekey.bin", "rb") as f:
      privatekey = RSA.importKey(f.read())
  except:
    return JsonResponse({
      "success": False,
      "reason": "No RSA key"
    })

  # get arguments
  try:
    body = json.loads(request.body.decode("utf-8"))["body"]
    email = privatekey.decrypt(base64.b64decode(body["email"])).decode("utf-8")
    password = privatekey.decrypt(base64.b64decode(body["password"])).decode("utf-8")
    gcmToken = privatekey.decrypt(base64.b64decode(body["gcmToken"])).decode("utf-8") if "gcmToken" in body else None
    key = body["key"]
  except:
    return JsonResponse({
      "success": False,
      "reason": "Missing argument"
    })

  # decipher the key
  try:
    aeskeyBytes = privatekey.decrypt(base64.b64decode(key))
    aesKey = AES.new(aeskeyBytes)
  except:
    return JsonResponse({
      "success": False,
      "reason": "Could not decipher the key"
    })

  # save user
  try:
    # generate auth token
    auth_token = base64.b64encode(uuid.uuid4().bytes).decode("utf-8")
    user = User.objects.get(email=email, password=hashlib.sha512(password.encode("utf-8")).hexdigest())
    user.authToken = auth_token
    user.aesKey = key
    user.gcmRegistrationToken = gcmToken
    user.save()

    return encode_response({
              "success": True,
              "id": user.id,
              "nickname": user.nickname,
              "auth_token": auth_token
            }, aesKey)

  except User.DoesNotExist:
    return encode_response({
              "success": False,
              "reason": "Invalid email/password pair"
            }, aesKey)

  except:
    traceback.print_exc()
    return encode_response({
              "success": False,
              "reason": "Could not update the user"
            }, aesKey)

def encode_response(response, key):
  return HttpResponse(
            base64.b64encode(
              key.encrypt(
                pad(json.dumps(response), 16))),
            content_type="text/plain")

def pad(data, length):
  b = bytearray(data.encode("utf-8"))
  while len(b) % length:
    b.append(0)
  return b.decode("utf-8").encode("utf-8")

def unpad(data):
  i = len(data) - 1
  while i >= 0:
    if data[i] != 0:
      return data[:i+1]
    i -= 1
  return b""

def secure_route(request, uid):
  # get server RSA key
  try:
    with open("privatekey.bin", "rb") as f:
      privatekey = RSA.importKey(f.read())
  except:
    return JsonResponse({
      "success": False,
      "reason": "No RSA key"
    })

  try:
    user = User.objects.get(pk=uid)
  except User.DoesNotExist:
    return JsonResponse({
      "success": False,
      "reason": "Unknown user"
    })

  # decrypt request body
  try:
    aeskeyBytes = privatekey.decrypt(base64.b64decode(user.aesKey))
    aesKey = AES.new(aeskeyBytes)
    json_body = unpad(aesKey.decrypt(base64.b64decode(request.body))).decode("utf-8")
  except:
    traceback.print_exc()
    return JsonResponse({
      "success": False,
      "reason": "Could not decipher request"
    })

  try:
    json_obj = json.loads(json_body)
    method = json_obj["method"]
    body = json_obj["body"]
    authToken = json_obj["body"]["auth_token"]
  except:
    traceback.print_exc()
    return encode_response({
      "success": False,
      "reason": "Request syntax error"
    }, aesKey)

  if authToken != user.authToken:
    return encode_response({
      "success": False,
      "reason": "Wrong authentication token"
    }, aesKey)

  functions = {
    "edit_profile": edit_profile,
    "search_contacts": search_users,
    "contact_list": contact_list,
    "add_contact": add_contact,
    "delete_contact": delete_contact,
    "send_message": send_message,
    "get_pending_messages": get_pending_messages,
    "get_user_data": get_user_data,
    "update_registration_token": update_registration_token,
  }

  try:
    f = functions[method]
  except:
    return encode_response({
      "success": False,
      "reason": "Invalid request method"
    }, aesKey)

  try:
    return encode_response(f(body, user), aesKey)
  except:
    traceback.print_exc()
    return encode_response({
      "success": False,
      "reason": "Unexpected error"
    }, aesKey)

def edit_profile(body, user):
  pass

def search_users(body, user):
  try:
    searchPattern = body["search_pattern"]
  except:
    return {
      "success": False,
      "reason": "missing argument"
    }

  userContacts = user.contacts.all()

  return {
    "success": True,
    "users": [
      {
        "id": x.id,
        "nickname": x.nickname
      }
      for x in User.objects.filter(nickname__icontains=searchPattern).all() if x not in userContacts and x is not user
    ]
  }

def contact_list(body, user):
  return {
    "success": True,
    "users": [
      {
        "id": x.id,
        "nickname": x.nickname
      }
      for x in user.contacts.all()
    ]
  }

def update_registration_token(body, user):
  try:
    gcmToken = body["gcmToken"]
  except:
    return {
      "success": False,
      "reason": "Missing argument"
    }

  try:
    user.gcmRegistrationToken = gcmToken
    user.save()
    return {
      "success": True
    }
  except:
    return {
      "success": False,
      "reason": "Could not save the token"
    }

def add_contact(body, user):
  try:
    contactId = body["contact"]
  except:
    return {
      "success": False,
      "reason": "missing argument"
    }

  try:
    contact = User.objects.get(pk=contactId)

    user.contacts.add(contact)
    user.save()

    return {
      "success": True,
      "id": contact.id,
      "nickname": contact.nickname
    }
  except User.DoesNotExist:
    return {
      "success": False,
      "reason": "this user does not exist"
    }
  except:
    return {
      "success": False,
      "reason": "could not add contact"
    }

def delete_contact(body, user):
  try:
    contactId = body["contact"]
  except:
    return {
      "success": False,
      "reason": "missing argument"
    }

  try:
    contact = User.objects.get(pk=contactId)

    user.contacts.remove(contact)
    user.save()

    return {
      "success": True,
      "id": contact.id
    }
  except User.DoesNotExist:
    return {
      "success": False,
      "reason": "this user does not exist"
    }
  except:
    return {
      "success": False,
      "reason": "could not remove contact"
    }

def send_message(body, user):
  try:
    contactId = body["contact"]
    content = body["content"]
  except:
    return {
      "success": False,
      "reason": "missing argument"
    }

  try:
    contact = User.objects.get(pk=contactId)

    message = Message(sender=user, recipient=contact, content=content, date=datetime.datetime.now())
    message.save()

    if contact.gcmRegistrationToken:
      try:
        apiKey = "AIzaSyAU-iFkuTPjbwfgdE1pvBPEJyni9hokmX4"
        contentType = "application/json"
        authorization = "key=" + apiKey
        body = json.dumps({
          "to": contact.gcmRegistrationToken,
          "data": {
            "newmessage": True
          }
        })

        httpClient = http.client.HTTPSConnection("gcm-http.googleapis.com")
        httpClient.request(
          "POST",
          "/gcm/send",
          body,
          {"Content-Type": contentType, "Authorization": authorization})

        response = httpClient.getresponse()
        print("Sent push notification: {0}".format(response.status))
      except:
        traceback.print_exc()
        print("Sent push notification: http request failed")

    return {
      "success": True
    }
  except User.DoesNotExist:
    return {
      "success": False,
      "reason": "this user does not exist"
    }
  except:
    return {
      "success": False,
      "reason": "could not remove contact"
    }

def get_pending_messages(body, user):
  pendingMessages = Message.objects.filter(recipient=user).order_by("date").all()

  resp = {
    "success": True,
    "messages": [
      {
        "contact": x.sender.id,
        "content": x.content,
        "date": "{:%H:%M}".format(x.date)
      }
      for x in pendingMessages
    ]
  }

  pendingMessages.delete()

  return resp

def get_user_data(body, user):
  try:
    contactId = body["user"]
  except:
    return {
      "success": False,
      "reason": "missing argument"
    }

  try:
    contact = User.objects.get(pk=contactId)

    return {
      "success": True,
      "id": contact.id,
      "nickname": contact.nickname
    }
  except User.DoesNotExist:
    return {
      "success": False,
      "reason": "this user does not exist"
    }
