from django.shortcuts import render
from django.http import HttpResponse

# Create your views here.


def index(request):
    value = "this is test"
    print(value)
    return render(request, 'index.html')


def new(request):
    return HttpResponse("这是Django1.0的路由协议")


def test(request, year, month, day):
    print("这是路由变量生成的结果！")
    return HttpResponse(str(year)+"/"+str(month)+"/"+str(day))


def re_test(request, year, month, day):
    return HttpResponse(str(year) + "/" + str(month) + "/" + str(day))

