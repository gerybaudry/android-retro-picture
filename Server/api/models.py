from django.db import models

# Create your models here.

class User(models.Model):
  nickname = models.CharField(max_length=25)
  contacts = models.ManyToManyField("self", symmetrical=False)
  email = models.EmailField(unique=True)
  password = models.TextField()
  authToken = models.TextField()
  aesKey = models.TextField() # encoded with server RSA pub key + base64 (base64encode(rsaencrypt(key)))
  gcmRegistrationToken = models.TextField(null=True, blank=True) # encoded with base64

class Message(models.Model):
  sender = models.ForeignKey(User, related_name="sender")
  recipient = models.ForeignKey(User, related_name="recipient")
  content = models.TextField()
  date = models.DateTimeField()
