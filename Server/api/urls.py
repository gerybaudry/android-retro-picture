from django.conf.urls import url

from . import views

urlpatterns = [
  url(r'^server_key', views.server_key),
  url(r'^sign_up', views.sign_up),
  url(r'^sign_in', views.sign_in),
  url(r'^secure_route/([0-9]+)$', views.secure_route)
]
