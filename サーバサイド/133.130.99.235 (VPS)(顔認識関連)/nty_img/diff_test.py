#coding: utf-8
import sys
import os
import cv2
import get_parts_pos as gp

#画像から顔を取得
image_path = "/var/www/html/nty_img/abe.jpg"
face_margin = 10;
face_image_size = 500
if os.path.exists(image_path):
  image = cv2.imread(image_path, cv2.IMREAD_COLOR)
else:
  print("error/file not found")
  sys.exit()
faceCascade = cv2.CascadeClassifier('/var/www/html/haarcascade_frontalface_default.xml')
gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
face = faceCascade.detectMultiScale(gray_image, 1.1, 3)
if len(face) > 0:
  for rect in face:
    trim_face_img = image[rect[1]:rect[1]+rect[3]+face_margin,rect[0]:rect[0]+rect[2]+face_margin]
    trim_face_img = cv2.resize(trim_face_img, (face_image_size, face_image_size))
    ret_pp = gp.get_parts_pos(trim_face_img)
    print(ret_pp)

