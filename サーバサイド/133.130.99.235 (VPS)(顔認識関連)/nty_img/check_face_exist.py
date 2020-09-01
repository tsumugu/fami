#coding: utf-8
import cv2
import numpy as np
import sys
import json

args = sys.argv

faceCascade = cv2.CascadeClassifier('/var/www/html/haarcascade_frontalface_default.xml')
img_path = args[1]
file_name = (img_path.split("/")[6]).replace(".jpeg", "")
img = cv2.imread(img_path)
img_gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
face = faceCascade.detectMultiScale(img_gray, 1.11, 3)
face_margin = 10;
face_image_size = 500

return_json_dic = {}
if len(face) > 0:
    return_json_dic["status"] = "OK"
    count = 0
    face_img_path_list = []
    for rect in face:
        trim_face_img = img[rect[1]:rect[1]+rect[3]+face_margin,rect[0]:rect[0]+rect[2]+face_margin]
        trim_face_img = cv2.resize(trim_face_img, (face_image_size, face_image_size))
        trim_face_file_apth = '/var/www/html/nty_img/tmp/'+file_name+'_'+str(count)+'.jpeg'
        cv2.imwrite(trim_face_file_apth, trim_face_img)
        face_img_path_list.append(trim_face_file_apth)
        count+=1
    return_json_dic["faces_path"] = face_img_path_list
else:
    return_json_dic["status"] = "NG"
print(json.dumps(return_json_dic))
