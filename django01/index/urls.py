from django.urls import path, re_path
from index import views


urlpatterns = [
    path('', views.index),
    path('new/', views.new),
    path('<int:year>/<int:month>/<slug:day>', views.test),
    re_path('(?P<year>[0-9]{4})/(?P<month>[0-9]{2})/(?P<day>[0-9]{2})', views.re_test),
]
